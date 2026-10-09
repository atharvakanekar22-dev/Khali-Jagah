/**
 * Khali Jagah — Backend API Server
 *
 * Mumbai Local Train Crowd Intelligence
 * Powered by Gemma 4 via Google Gemini API
 */

require("dotenv").config();

const express = require("express");
const cors = require("cors");

const stationsRouter = require("./routes/stations");
const trainsRouter = require("./routes/trains");
const crowdRouter = require("./routes/crowd");
const analyzeRouter = require("./routes/analyze");
const { getModel } = require("./services/gemmaService");

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use((req, res, next) => {
  if (req.headers["content-type"]) {
    req.headers["content-type"] = req.headers["content-type"].replace(/charset=UTF8/i, "charset=utf-8");
  }
  next();
});
app.use(express.json({ limit: "5mb" }));

// Request logging
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
  next();
});

// Health check
app.get("/api/health", (req, res) => {
  res.json({
    status: "ok",
    app: "Khali Jagah",
    version: "1.0.0",
    gemmaModel: getModel(),
    apiKeyConfigured: !!process.env.GEMINI_API_KEY,
    timestamp: new Date().toISOString(),
  });
});

// Routes
app.use("/api/stations", stationsRouter);
app.use("/api/trains", trainsRouter);
app.use("/api/crowd", crowdRouter);
app.use("/api/analyze-crowd", analyzeRouter);

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    success: false,
    error: `Route ${req.method} ${req.url} not found`,
    availableEndpoints: [
      "GET  /api/health",
      "GET  /api/stations",
      "GET  /api/trains?source=DR&destination=TN",
      "GET  /api/crowd/:trainId",
      "POST /api/analyze-crowd",
    ],
  });
});

// Error handler
app.use((err, req, res, next) => {
  console.error("[Server] Unhandled error:", err.message);
  res.status(500).json({
    success: false,
    error: "Internal server error",
  });
});

// Start server
app.listen(PORT, () => {
  console.log(`
╔════════════════════════════════════════════════╗
║          🚂  KHALI JAGAH  🚂                  ║
║     Mumbai Local Train Crowd Intelligence      ║
║                                                ║
║  Server running on port ${String(PORT).padEnd(25)}║
║  Gemma model: ${getModel().padEnd(33)}║
║  API key: ${process.env.GEMINI_API_KEY ? "configured ✓" : "NOT SET ✗ (will use fallback)"}${" ".repeat(Math.max(0, 22 - (process.env.GEMINI_API_KEY ? 14 : 30)))}║
╚════════════════════════════════════════════════╝
  `);
});

module.exports = app;
