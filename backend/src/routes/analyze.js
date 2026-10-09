/**
 * Khali Jagah — Analyze Route
 * POST /api/analyze-crowd — AI-powered crowd analysis via Gemma 4
 *
 * Request body: { trainId: "MUM-0842" }
 * Optional: { trainId: "MUM-0842", source: "DR", destination: "TN" }
 */

const express = require("express");
const router = express.Router();
const trainsData = require("../data/trains.json");
const crowdData = require("../data/crowdData.json");
const { analyzeCrowd } = require("../services/gemmaService");

router.post("/", async (req, res) => {
  try {
    const { trainId } = req.body;

    // Validate input
    if (!trainId) {
      return res.status(400).json({
        success: false,
        error: "trainId is required in the request body",
      });
    }

    // Find train
    const train = trainsData.trains.find(
      (t) => t.id === trainId.toUpperCase()
    );
    if (!train) {
      return res.status(404).json({
        success: false,
        error: `Train ${trainId} not found`,
      });
    }

    // Find crowd data
    const crowd = crowdData.crowdData.find(
      (c) => c.trainId === trainId.toUpperCase()
    );
    if (!crowd) {
      return res.status(404).json({
        success: false,
        error: `No crowd data available for train ${trainId}`,
      });
    }

    console.log(
      `[Analyze] Processing analysis for ${train.id} (${train.name}, ${train.departure})`
    );

    // Call AI service
    const analysis = await analyzeCrowd(
      train,
      crowd,
      trainsData.trains,
      crowdData.crowdData
    );

    res.json({
      success: true,
      trainId: train.id,
      trainName: train.name,
      departure: train.departure,
      arrival: train.arrival,
      analysis,
      dataDisclaimer:
        "AI-generated estimate based on prototype dataset. Not official railway data.",
    });
  } catch (err) {
    console.error("[Analyze] Error:", err.message);
    res.status(500).json({
      success: false,
      error: "Analysis failed. Please try again.",
    });
  }
});

module.exports = router;
