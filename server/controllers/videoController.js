const Video = require("../models/Video");
const User = require("../models/User");
const Notification = require("../models/Notification");
const cache = require("../utils/cache");

exports.uploadVideo = async (req, res) => {
  try {
    const { title } = req.body;
    const videoPath = req.files.video?.[0]?.path.replace(/\\/g, "/");
    const posterPath = req.files.poster?.[0]?.path.replace(/\\/g, "/");

    const newVideo = new Video({
      title,
      videoUrl: videoPath,
      posterUrl: posterPath,
      author: req.user._id,
    });

    await newVideo.save();

    const author = await User.findById(req.user._id);
    const followers = author.followers || [];
    for (const followerId of followers) {
      await Notification.create({
        user: followerId,
        type: "new_video",
        fromUser: author._id,
        video: newVideo._id,
        text: `${author.username} uploaded a new video: ${title}`,
      });
    }
    res.json({ message: "Video uploaded", video: newVideo });
  } catch (err) {
    res.status(500).json({ message: "Upload failed", error: err.message });
  }
};

exports.updateVideo = async (req, res) => {
  try {
    const video = await Video.findById(req.params.id);
    if (!video) return res.status(404).json({ message: "Video not found" });
    if (video.author.toString() !== req.user._id.toString())
      return res.status(403).json({ message: "No permission" });

    if (req.body.title) video.title = req.body.title;
    if (req.files && req.files.poster && req.files.poster[0]) {
      video.posterUrl = req.files.poster[0].path.replace(/\\/g, "/");
    }
    await video.save();
    const populatedVideo = await Video.findById(video._id).populate(
      "author",
      "username avatarUrl"
    );
    res.json({ message: "Video updated", video: populatedVideo });
  } catch (err) {
    res.status(500).json({ message: "Update failed", error: err.message });
  }
};

exports.deleteVideo = async (req, res) => {
  try {
    const video = await Video.findById(req.params.id);
    if (!video) return res.status(404).json({ message: "Video not found" });

    if (
      video.author.toString() !== req.user._id.toString() &&
      req.user.role !== "admin"
    ) {
      return res.status(403).json({ message: "No permission" });
    }

    await video.deleteOne();
    res.json({ message: "Video deleted" });
  } catch (err) {
    res
      .status(500)
      .json({ message: "Failed to delete video", error: err.message });
  }
};

exports.list = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 9;
    const skip = (page - 1) * limit;
    const cacheKey = `videos_page_${page}_limit_${limit}`;

    const cached = cache.get(cacheKey);
    if (cached) {
      return res.json(cached);
    }

    const [videos, total] = await Promise.all([
      Video.find()
        .populate("author", "username avatarUrl")
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(limit)
        .lean(),
      Video.countDocuments(),
    ]);

    const result = {
      videos,
      total,
      page,
      pages: Math.ceil(total / limit),
    };

    cache.set(cacheKey, result);
    res.json(result);
  } catch (err) {
    console.error("VIDEO LIST ERROR:", err);
    res
      .status(500)
      .json({ message: "Error by getting video", error: err.message });
  }
};

exports.getVideoById = async (req, res) => {
  try {
    const video = await Video.findById(req.params.id).populate(
      "author",
      "username avatarUrl"
    );
    if (!video) return res.status(404).json({ message: "Video not found" });
    res.json(video);
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

exports.getVideosByUser = async (req, res) => {
  try {
    const videos = await Video.find({ uploadedBy: req.params.userId });
    res.json(videos);
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

exports.shareVideo = async (req, res) => {
  try {
    const { toUserId } = req.body;
    const videoId = req.params.id;

    if (!toUserId || typeof toUserId !== "string") {
      return res.status(400).json({ message: "Username is required" });
    }
    if (toUserId === req.user.username) {
      return res
        .status(400)
        .json({ message: "Cannot share video with yourself" });
    }

    const user = await User.findOne({ username: toUserId });
    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }

    const video = await Video.findById(videoId);
    if (!video) {
      return res.status(404).json({ message: "Video not found" });
    }

    await Notification.create({
      user: user._id,
      type: "share",
      fromUser: req.user._id,
      video: video._id,
      text: `${req.user.username} shared a video with you: ${video.title}`,
    });

    res.json({ message: "Video shared successfully" });
  } catch (err) {
    console.error("SHARE VIDEO ERROR:", err);
    res
      .status(500)
      .json({ message: "Failed to share video", error: err.message });
  }
};

exports.likeVideo = async (req, res) => {
  try {
    const video = await Video.findById(req.params.id);
    if (!video) return res.status(404).json({ message: "Video not found" });

    video.dislikes = video.dislikes.filter(
      (userId) => userId.toString() !== req.user._id.toString()
    );

    if (video.likes.includes(req.user._id)) {
      video.likes = video.likes.filter(
        (userId) => userId.toString() !== req.user._id.toString()
      );
    } else {
      video.likes.push(req.user._id);
    }
    await video.save();
    res.json({ likes: video.likes.length, dislikes: video.dislikes.length, liked: video.likes.includes(req.user._id) });
  } catch (err) {
    res.status(500).json({ message: "Like failed", error: err.message });
  }
};

exports.dislikeVideo = async (req, res) => {
  try {
    const video = await Video.findById(req.params.id);
    if (!video) return res.status(404).json({ message: "Video not found" });

    video.likes = video.likes.filter(
      (userId) => userId.toString() !== req.user._id.toString()
    );

    if (video.dislikes.includes(req.user._id)) {
      video.dislikes = video.dislikes.filter(
        (userId) => userId.toString() !== req.user._id.toString()
      );
    } else {
      video.dislikes.push(req.user._id);
    }
    await video.save();
    res.json({ likes: video.likes.length, dislikes: video.dislikes.length, disliked: video.dislikes.includes(req.user._id) });
  } catch (err) {
    res.status(500).json({ message: "Dislike failed", error: err.message });
  }
};
