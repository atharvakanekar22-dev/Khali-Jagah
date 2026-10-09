/**
 * Khali Jagah — Stations Route
 * GET /api/stations — returns all stations
 * GET /api/stations?line=central — filter by line
 */

const express = require("express");
const router = express.Router();
const stationsData = require("../data/stations.json");

router.get("/", (req, res) => {
  try {
    let stations = stationsData.stations;

    // Optional line filter
    const { line } = req.query;
    if (line) {
      stations = stations.filter(
        (s) => s.line.toLowerCase() === line.toLowerCase()
      );
    }

    // Sort by line then order
    stations.sort((a, b) => {
      if (a.line !== b.line) return a.line.localeCompare(b.line);
      return a.order - b.order;
    });

    res.json({
      success: true,
      count: stations.length,
      data: stations,
    });
  } catch (err) {
    console.error("[Stations] Error:", err.message);
    res.status(500).json({ success: false, error: "Failed to fetch stations" });
  }
});

module.exports = router;
