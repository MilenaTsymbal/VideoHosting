const express = require("express");
const router = express.Router();
const commentController = require("../controllers/commentController");
const authMiddleware = require("../middleware/authMiddleware");

router.get("/:id", commentController.getComments);
router.post("/:id", authMiddleware, commentController.addComment);
router.put("/:commentId", authMiddleware, commentController.updateComment);
router.delete("/:commentId", authMiddleware, commentController.deleteComment);

module.exports = router;
