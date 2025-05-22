const Comment = require("../models/Comment");

exports.addComment = async (req, res) => {
  try {
    const { text } = req.body;
    if (!text) return res.status(400).json({ message: "Text required" });

    const comment = await Comment.create({
      video: req.params.id,
      author: req.user._id,
      text,
    });
    await comment.populate("author", "username avatarUrl");
    res.status(201).json(comment);
  } catch (err) {
    res
      .status(500)
      .json({ message: "Failed to add comment", error: err.message });
  }
};

exports.getComments = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    const [comments, total] = await Promise.all([
      Comment.find({ video: req.params.id })
        .populate("author", "username avatarUrl")
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(limit),
      Comment.countDocuments({ video: req.params.id }),
    ]);

    res.json({
      comments,
      total,
      page,
      pages: Math.ceil(total / limit),
    });
  } catch (err) {
    res
      .status(500)
      .json({ message: "Error by getting comments", error: err.message });
  }
};

exports.updateComment = async (req, res) => {
  try {
    const comment = await Comment.findById(req.params.commentId);
    if (!comment) return res.status(404).json({ message: "Comment not found" });
    if (comment.author.toString() !== req.user._id.toString())
      return res.status(403).json({ message: "No permission" });

    comment.text = req.body.text;
    await comment.save();
    await comment.populate("author", "username avatarUrl");
    res.json(comment);
  } catch (err) {
    res
      .status(500)
      .json({ message: "Failed to update comment", error: err.message });
  }
};

exports.deleteComment = async (req, res) => {
  try {
    const { commentId } = req.params;
    const comment = await Comment.findById(commentId);
    if (!comment) return res.status(404).json({ message: "Comment not found" });
    if (
      comment.author.toString() !== req.user._id.toString() &&
      req.user.role !== "admin"
    )
      return res.status(403).json({ message: "No permission" });

    await comment.deleteOne();
    res.json({ message: "Comment deleted", commentId });
  } catch (err) {
    console.error("COMMENT DELETE ERROR:", err);
    res
      .status(500)
      .json({ message: "Failed to delete comment", error: err.message });
  }
};
