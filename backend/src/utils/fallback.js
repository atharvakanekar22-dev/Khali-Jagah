/**
 * Khali Jagah — Fallback Analysis Engine
 * 
 * Deterministic rule-based crowd analysis used when Gemma/Gemini API
 * is unavailable. Ensures the demo remains functional offline.
 */

const THRESHOLDS = {
  LOW: [0, 30],
  MODERATE: [31, 55],
  HIGH: [56, 75],
  VERY_HIGH: [76, 100],
};

function getLevel(score) {
  if (score <= 30) return "LOW";
  if (score <= 55) return "MODERATE";
  if (score <= 75) return "HIGH";
  return "VERY_HIGH";
}

function generateFallbackAnalysis(trainData, crowdRecord, allTrains, allCrowd) {
  const level = getLevel(crowdRecord.overallCrowdScore);
  const score = crowdRecord.overallCrowdScore;

  // Build reasoning
  const reasoning = [];

  if (crowdRecord.period === "peak") {
    reasoning.push("Peak-hour travel period typically sees higher passenger volume");
  } else {
    reasoning.push("Off-peak travel period — generally lower passenger density");
  }

  if (score >= 76) {
    reasoning.push(`High overall crowd score of ${score}/100`);
  } else if (score >= 56) {
    reasoning.push(`Elevated crowd score of ${score}/100`);
  } else {
    reasoning.push(`Moderate-to-low crowd score of ${score}/100`);
  }

  // Find crowded coaches
  const coaches = crowdRecord.coaches;
  const highCoaches = Object.entries(coaches)
    .filter(([, v]) => v >= 76)
    .map(([k]) => k);
  const lowCoaches = Object.entries(coaches)
    .filter(([, v]) => v <= 30)
    .sort((a, b) => a[1] - b[1])
    .map(([k]) => k);

  if (highCoaches.length > 0) {
    reasoning.push(
      `${highCoaches.length} coach(es) show very high density: ${highCoaches.join(", ")}`
    );
  }

  if (lowCoaches.length > 0) {
    reasoning.push(
      `${lowCoaches.length} coach(es) have low occupancy: ${lowCoaches.join(", ")}`
    );
  }

  if (crowdRecord.trend === "increasing") {
    reasoning.push("Crowd trend is increasing compared to historical data");
  } else if (crowdRecord.trend === "decreasing") {
    reasoning.push("Crowd trend is decreasing compared to historical data");
  }

  // Build recommendation
  let recommendation;
  if (score >= 76) {
    recommendation =
      "Consider waiting for a later train if your schedule allows, or board a less crowded coach.";
  } else if (score >= 56) {
    recommendation =
      "Crowding is notable. Choose a less crowded coach for a more comfortable ride.";
  } else if (score >= 31) {
    recommendation =
      "Moderate crowd levels — a reasonable time to travel.";
  } else {
    recommendation =
      "Low crowd levels — comfortable travel conditions expected.";
  }

  // Find best coach
  const bestCoach = Object.entries(coaches).sort((a, b) => a[1] - b[1])[0];

  // Find alternative train with lower crowd
  let alternative = null;
  if (allTrains && allCrowd) {
    const candidateTrains = allTrains.filter((t) => {
      if (t.id === trainData.id) return false;
      return t.stops.includes(trainData.source || trainData.stops[0]) &&
        t.stops.includes(trainData.destination || trainData.stops[trainData.stops.length - 1]);
    });

    for (const candidate of candidateTrains) {
      const candidateCrowd = allCrowd.find((c) => c.trainId === candidate.id);
      if (candidateCrowd && candidateCrowd.overallCrowdScore < score - 10) {
        const diff = score - candidateCrowd.overallCrowdScore;
        alternative = {
          trainId: candidate.id,
          trainName: candidate.name,
          departure: candidate.departure,
          crowdScore: candidateCrowd.overallCrowdScore,
          crowdLevel: getLevel(candidateCrowd.overallCrowdScore),
          difference: diff,
          reason: `${diff}% lower estimated crowd score`,
        };
        break;
      }
    }
  }

  return {
    crowdLevel: level,
    crowdScore: score,
    confidence: 0.7,
    summary: `Crowding is estimated to be ${level.replace("_", " ").toLowerCase()} for this ${crowdRecord.period} train.`,
    reasoning,
    recommendation,
    bestCoach: bestCoach
      ? { coach: bestCoach[0], score: bestCoach[1], level: getLevel(bestCoach[1]) }
      : null,
    alternative,
    source: "rule-based-fallback",
    disclaimer:
      "AI service temporarily unavailable. Showing prototype rule-based analysis.",
  };
}

module.exports = { generateFallbackAnalysis, getLevel, THRESHOLDS };
