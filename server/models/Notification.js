const mongoose = require("mongoose");

const notificationSchema = new mongoose.Schema(
  {
    user: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    type: {
      type: String,
      enum: ["subscribe", "new_video", "share"],
      required: true,
    },
    fromUser: { type: mongoose.Schema.Types.ObjectId, ref: "User" },
    video: { type: mongoose.Schema.Types.ObjectId, ref: "Video" },
    text: String,
  },
  { timestamps: true }
);

module.exports = mongoose.model("Notification", notificationSchema);
