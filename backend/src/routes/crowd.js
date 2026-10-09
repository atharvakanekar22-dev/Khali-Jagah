/**
 * Khali Jagah — Crowd Route
 * GET /api/crowd/:trainId — returns coach-wise crowd data for a train
 */

const express = require("express");
const router = express.Router();
const crowdData = require("../data/crowdData.json");
const { getLevel } = require("../utils/fallback");

router.get("/:trainId", (req, res) => {
  try {
    const { trainId } = req.params;

    if (!trainId) {
      return res.status(400).json({
        success: false,
        error: "Train ID is required",
      });
    }

    const record = crowdData.crowdData.find(
      (c) => c.trainId === trainId.toUpperCase()
    );

    if (!record) {
      return res.status(404).json({
        success: false,
        error: `No crowd data found for train ${trainId}`,
      });
    }

    // Enrich coaches with levels
    const coaches = Object.entries(record.coaches).map(([id, score]) => ({
      id,
      score,
      level: getLevel(score),
    }));

    // Sort coaches by ID
    coaches.sort((a, b) => {
      const numA = parseInt(a.id.replace("C", ""));
      const numB = parseInt(b.id.replace("C", ""));
      return numA - numB;
    });

    // Find best (least crowded) coach
    const bestCoach = coaches.reduce((min, c) =>
      c.score < min.score ? c : min
    );

    // Find worst (most crowded) coach
    const worstCoach = coaches.reduce((max, c) =>
      c.score > max.score ? c : max
    );

    res.json({
      success: true,
      trainId: record.trainId,
      overallScore: record.overallCrowdScore,
      overallLevel: getLevel(record.overallCrowdScore),
      period: record.period,
      historicalAvg: record.historicalAvg,
      trend: record.trend,
      coachCount: coaches.length,
      coaches,
      bestCoach,
      worstCoach,
      dataDisclaimer: "Prototype demonstration data — not official railway measurements",
    });
  } catch (err) {
    console.error("[Crowd] Error:", err.message);
    res.status(500).json({ success: false, error: "Failed to fetch crowd data" });
  }
});

module.exports = router;
