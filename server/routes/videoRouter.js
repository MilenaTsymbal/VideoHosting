const express = require("express");
const router = express.Router();
const videoController = require("../controllers/videoController");
const authMiddleware = require("../middleware/authMiddleware");
const uploadVideo = require("../utils/uploadVideo");

router.post(
  "/",
  authMiddleware,
  uploadVideo.fields([
    { name: "video", maxCount: 1 },
    { name: "poster", maxCount: 1 },
  ]),
  videoController.uploadVideo
);
router.put(
  "/:id",
  authMiddleware,
  uploadVideo.fields([{ name: "poster", maxCount: 1 }]),
  videoController.updateVideo
);
router.delete("/:id", authMiddleware, videoController.deleteVideo);
router.get("/", videoController.list);
router.get("/:id", videoController.getVideoById);
router.post("/:id/share", authMiddleware, videoController.shareVideo);

module.exports = router;
