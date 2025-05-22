const User = require("../models/User");
const Video = require("../models/Video");
const Notification = require("../models/Notification");

exports.getMe = async (req, res) => {
  try {
    const user = await User.findById(req.user._id).select("-password");
    res.json(user);
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

exports.getUserByUsername = async (req, res) => {
  try {
    const user = await User.findOne({ username: req.params.username }).select(
      "-password"
    );
    if (!user) return res.status(404).json({ message: "User not found" });

    const videos = await Video.find({ author: user._id }).populate(
      "author",
      "username avatarUrl"
    );

    res.json({
      user: {
        _id: user._id,
        username: user.username,
        avatarUrl: user.avatarUrl,
        subscriptionsCount: user.subscriptions.length,
        followersCount: user.followers.length,
        subscriptions: user.subscriptions,
        followers: user.followers,
      },
      videos,
    });
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

exports.updateMe = async (req, res) => {
  try {
    const { username, avatarUrl } = req.body;
    const updateData = {};

    if (username && username.trim() !== "") {
      const existing = await User.findOne({ username });
      if (existing && existing._id.toString() !== req.user._id.toString()) {
        return res.status(400).json({ message: "Username already taken" });
      }
      updateData.username = username.trim();
    }

    if (avatarUrl) {
      updateData.avatarUrl = avatarUrl;
    }

    const updatedUser = await User.findByIdAndUpdate(req.user._id, updateData, {
      new: true,
    }).select("-password");

    res.json(updatedUser);
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

exports.changeRole = async (req, res) => {
  try {
    if (req.user.role !== "admin") {
      return res.status(403).json({ message: "No permission" });
    }
    const { userId, role } = req.body;
    if (!["user", "admin"].includes(role)) {
      return res.status(400).json({ message: "Invalid role" });
    }
    const user = await User.findByIdAndUpdate(userId, { role }, { new: true });
    res.json(user);
  } catch (err) {
    res
      .status(500)
      .json({ message: "Failed to change role", error: err.message });
  }
};

exports.deleteUser = async (req, res) => {
  try {
    await User.findByIdAndDelete(req.params.id);
    res.json({ message: "User deleted" });
  } catch (err) {
    res
      .status(500)
      .json({ message: "Failed to delete user", error: err.message });
  }
};

exports.subscribe = async (req, res) => {
  try {
    const targetUser = await User.findById(req.params.id);
    const currentUser = await User.findById(req.user._id);

    if (!targetUser) return res.status(404).json({ message: "User not found" });
    if (targetUser._id.equals(currentUser._id)) {
      return res.status(400).json({ message: "Cannot subscribe to yourself" });
    }

    if (!currentUser.subscriptions.includes(targetUser._id)) {
      currentUser.subscriptions.push(targetUser._id);
      await currentUser.save();
    }
    if (!targetUser.followers.includes(currentUser._id)) {
      targetUser.followers.push(currentUser._id);
      await targetUser.save();

      await Notification.create({
        user: targetUser._id,
        type: "subscribe",
        fromUser: currentUser._id,
        text: `${currentUser.username} subscribed to you`,
      });
    }

    res.json({ message: "Subscribed" });
  } catch (err) {
    res.status(500).json({ message: "Subscribe error", error: err.message });
  }
};

exports.unsubscribe = async (req, res) => {
  try {
    const targetUser = await User.findById(req.params.id);
    const currentUser = await User.findById(req.user._id);

    if (!targetUser) return res.status(404).json({ message: "User not found" });

    currentUser.subscriptions = currentUser.subscriptions.filter(
      (id) => !id.equals(targetUser._id)
    );
    await currentUser.save();

    targetUser.followers = targetUser.followers.filter(
      (id) => !id.equals(currentUser._id)
    );
    await targetUser.save();

    res.json({ message: "Unsubscribed" });
  } catch (err) {
    res.status(500).json({ message: "Unsubscribe error", error: err.message });
  }
};

exports.getNotifications = async (req, res) => {
  try {
    const notifications = await Notification.find({ user: req.user._id })
      .populate("fromUser", "username avatarUrl")
      .populate("video", "title")
      .sort({ createdAt: -1 });
    res.json(notifications);
  } catch (err) {
    res
      .status(500)
      .json({ message: "Failed to get notifications", error: err.message });
  }
};
