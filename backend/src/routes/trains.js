/**
 * Khali Jagah — Trains Route
 * GET /api/trains?source=DR&destination=TN — returns matching trains
 */

const express = require("express");
const router = express.Router();
const trainsData = require("../data/trains.json");
const crowdData = require("../data/crowdData.json");
const { getLevel } = require("../utils/fallback");

router.get("/", (req, res) => {
  try {
    const { source, destination } = req.query;

    if (!source || !destination) {
      return res.status(400).json({
        success: false,
        error: "Both 'source' and 'destination' query parameters are required",
      });
    }

    if (source === destination) {
      return res.status(400).json({
        success: false,
        error: "Source and destination cannot be the same",
      });
    }

    // Find trains that stop at both source and destination (in correct order)
    const matchingTrains = trainsData.trains.filter((train) => {
      const srcIdx = train.stops.indexOf(source.toUpperCase());
      const destIdx = train.stops.indexOf(destination.toUpperCase());
      return srcIdx !== -1 && destIdx !== -1 && srcIdx < destIdx;
    });

    // Enrich with crowd data
    const enriched = matchingTrains.map((train) => {
      const crowd = crowdData.crowdData.find((c) => c.trainId === train.id);
      return {
        id: train.id,
        name: train.name,
        type: train.type,
        departure: train.departure,
        arrival: train.arrival,
        journeyMinutes: train.journeyMinutes,
        stops: train.stops,
        crowdScore: crowd ? crowd.overallCrowdScore : null,
        crowdLevel: crowd ? getLevel(crowd.overallCrowdScore) : "UNKNOWN",
        period: crowd ? crowd.period : null,
      };
    });

    // Sort by departure time
    enriched.sort((a, b) => a.departure.localeCompare(b.departure));

    res.json({
      success: true,
      source: source.toUpperCase(),
      destination: destination.toUpperCase(),
      count: enriched.length,
      dataDisclaimer: "Prototype demonstration data — not official railway information",
      data: enriched,
    });
  } catch (err) {
    console.error("[Trains] Error:", err.message);
    res.status(500).json({ success: false, error: "Failed to fetch trains" });
  }
});

module.exports = router;
