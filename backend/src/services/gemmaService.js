/**
 * Khali Jagah — Gemma 4 AI Service
 *
 * Handles all communication with Google's Gemini API using the Gemma 4 model.
 * Uses @google/genai SDK with structured JSON output.
 */

const { GoogleGenAI, Type } = require("@google/genai");
const { generateFallbackAnalysis } = require("../utils/fallback");

// Initialized lazily on first call
let ai = null;

function getAI() {
  if (!ai) {
    const apiKey = process.env.GEMINI_API_KEY;
    if (!apiKey) {
      throw new Error("GEMINI_API_KEY environment variable is not set");
    }
    ai = new GoogleGenAI({ apiKey });
  }
  return ai;
}

function getModel() {
  return process.env.GEMMA_MODEL || "gemma-4-31b-it";
}

const SYSTEM_PROMPT = `You are an urban transit crowd-analysis assistant for the Khali Jagah application.

Your role:
- Analyze PROVIDED prototype train crowd data and generate cautious, actionable travel insights for Mumbai suburban railway passengers.
- Help passengers make safer, more informed travel decisions.

Critical rules:
1. Use ONLY the data supplied in the user message. Never invent live information.
2. All crowd data is PROTOTYPE/SAMPLE data — NOT live, official, sensor-derived, or railway-authority data.
3. Distinguish predictions from facts. Use language like "estimated", "expected", "based on prototype data".
4. Never make guaranteed safety claims. Use cautious language.
5. Provide concise, practical explanations.
6. Recommend alternatives ONLY when the supplied data supports them.
7. Your response must be valid JSON matching the required schema exactly.

Crowd score interpretation (0–100):
- 0–30: LOW — comfortable travel conditions
- 31–55: MODERATE — manageable crowding
- 56–75: HIGH — significant crowding expected
- 76–100: VERY_HIGH — extreme crowding expected`;

// JSON schema for structured output
const RESPONSE_SCHEMA = {
  type: Type.OBJECT,
  properties: {
    crowdLevel: {
      type: Type.STRING,
      description: "Overall crowd level: LOW, MODERATE, HIGH, or VERY_HIGH",
    },
    crowdScore: {
      type: Type.INTEGER,
      description: "Overall crowd score 0-100",
    },
    confidence: {
      type: Type.NUMBER,
      description: "Confidence in the analysis from 0.0 to 1.0",
    },
    summary: {
      type: Type.STRING,
      description:
        "One or two sentence summary of the crowd situation for this train",
    },
    reasoning: {
      type: Type.ARRAY,
      items: { type: Type.STRING },
      description:
        "Array of 2-5 concise reasons explaining the crowd assessment",
    },
    recommendation: {
      type: Type.STRING,
      description:
        "A single actionable recommendation for the passenger",
    },
    bestCoach: {
      type: Type.OBJECT,
      properties: {
        coach: {
          type: Type.STRING,
          description: "The coach identifier with lowest estimated crowd",
        },
        score: {
          type: Type.INTEGER,
          description: "The crowd score for that coach",
        },
        level: {
          type: Type.STRING,
          description: "Crowd level for that coach",
        },
      },
      description: "The least crowded coach recommendation",
    },
    alternative: {
      type: Type.OBJECT,
      properties: {
        trainId: { type: Type.STRING },
        trainName: { type: Type.STRING },
        departure: { type: Type.STRING },
        crowdScore: { type: Type.INTEGER },
        crowdLevel: { type: Type.STRING },
        difference: { type: Type.INTEGER },
        reason: { type: Type.STRING },
      },
      description:
        "An alternative train with lower crowd, or null if none available",
    },
  },
  propertyOrdering: [
    "crowdLevel",
    "crowdScore",
    "confidence",
    "summary",
    "reasoning",
    "recommendation",
    "bestCoach",
    "alternative",
  ],
};

/**
 * Build the user prompt with all relevant crowd data
 */
function buildUserPrompt(trainData, crowdRecord, alternativeTrain) {
  let prompt = `Analyze the following prototype crowd data for a Mumbai suburban train and provide travel insights.

TRAIN INFORMATION:
- Train ID: ${trainData.id}
- Train Name: ${trainData.name}
- Type: ${trainData.type}
- Source: ${trainData.source}
- Destination: ${trainData.destination}
- Departure: ${trainData.departure}
- Arrival: ${trainData.arrival}
- Journey Time: ${trainData.journeyMinutes} minutes
- Stops: ${trainData.stops.join(" → ")}

CROWD DATA (prototype/sample — NOT official railway data):
- Overall Crowd Score: ${crowdRecord.overallCrowdScore}/100
- Period: ${crowdRecord.period}
- Historical Average: ${crowdRecord.historicalAvg}/100
- Trend: ${crowdRecord.trend}

COACH-WISE CROWD SCORES:
${Object.entries(crowdRecord.coaches)
  .map(([coach, score]) => `  ${coach}: ${score}/100`)
  .join("\n")}
`;

  if (alternativeTrain) {
    prompt += `
ALTERNATIVE TRAIN AVAILABLE:
- Train ID: ${alternativeTrain.train.id}
- Name: ${alternativeTrain.train.name}
- Departure: ${alternativeTrain.train.departure}
- Overall Crowd Score: ${alternativeTrain.crowd.overallCrowdScore}/100
- Crowd Difference: ${crowdRecord.overallCrowdScore - alternativeTrain.crowd.overallCrowdScore}% lower
`;
  } else {
    prompt += `
No alternative train with significantly lower crowd is available in the dataset.
`;
  }

  prompt += `
Provide your analysis as structured JSON. Remember: this is prototype data, not live railway information.`;

  return prompt;
}

/**
 * Helper to sleep for ms
 */
function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * Main analysis function — calls Gemma 4 via Gemini API with retry logic
 */
async function analyzeCrowd(trainData, crowdRecord, allTrains, allCrowd) {
  // Find a lower-crowd alternative
  const source = trainData.stops[0];
  const destination = trainData.stops[trainData.stops.length - 1];

  let alternativeTrain = null;
  for (const t of allTrains) {
    if (t.id === trainData.id) continue;
    if (!t.stops.includes(source) || !t.stops.includes(destination)) continue;
    const tCrowd = allCrowd.find((c) => c.trainId === t.id);
    if (
      tCrowd &&
      tCrowd.overallCrowdScore < crowdRecord.overallCrowdScore - 10
    ) {
      alternativeTrain = { train: t, crowd: tCrowd };
      break;
    }
  }

  const maxRetries = 2;
  const genAI = getAI();
  const model = getModel();
  const userPrompt = buildUserPrompt(trainData, crowdRecord, alternativeTrain);

  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      console.log(`[GemmaService] Calling model: ${model} (attempt ${attempt}/${maxRetries})`);

      const response = await genAI.models.generateContent({
        model,
        contents: userPrompt,
        config: {
          systemInstruction: SYSTEM_PROMPT,
          responseMimeType: "application/json",
          responseJsonSchema: RESPONSE_SCHEMA,
          temperature: 0.3,
          maxOutputTokens: 1024,
        },
      });

      const text = response.text;
      console.log(`[GemmaService] Response received (${text.length} chars)`);

      // Parse and validate
      let parsed;
      try {
        parsed = JSON.parse(text);
      } catch (parseErr) {
        console.error("[GemmaService] Failed to parse JSON response:", parseErr.message);
        const retryResponse = await genAI.models.generateContent({
          model,
          contents: userPrompt + "\n\nIMPORTANT: Respond ONLY with valid JSON, no markdown, no extra text.",
          config: {
            systemInstruction: SYSTEM_PROMPT,
            responseMimeType: "application/json",
            responseJsonSchema: RESPONSE_SCHEMA,
            temperature: 0.1,
            maxOutputTokens: 1024,
          },
        });
        parsed = JSON.parse(retryResponse.text);
      }

      // Validate required fields
      if (!parsed.crowdLevel || !parsed.summary || !parsed.reasoning) {
        throw new Error("Response missing required fields");
      }

      return {
        ...parsed,
        source: "gemma-4",
        model,
        disclaimer:
          "AI-generated estimate based on prototype dataset. Not official railway data.",
      };
    } catch (err) {
      console.error(`[GemmaService] Attempt ${attempt} failed:`, err.message);
      if (attempt < maxRetries) {
        console.log(`[GemmaService] Retrying in 1500ms...`);
        await sleep(1500);
      } else {
        console.log("[GemmaService] All AI attempts failed, falling back to deterministic rule-based analysis");
        return generateFallbackAnalysis(
          trainData,
          crowdRecord,
          allTrains,
          allCrowd
        );
      }
    }
  }
}

module.exports = { analyzeCrowd, getModel };
