const mongoose = require("mongoose");

const videoSchema = new mongoose.Schema({
  title: String,
  videoUrl: String,
  posterUrl: String,
  author: { type: mongoose.Schema.Types.ObjectId, ref: "User" },
  createdAt: { type: Date, default: Date.now },
});

module.exports = mongoose.model("Video", videoSchema);
