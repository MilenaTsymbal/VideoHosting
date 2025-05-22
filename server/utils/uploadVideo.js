const multer = require("multer");
const fs = require("fs");

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const isVideo = file.mimetype.startsWith("video/");
    const folder = isVideo ? "uploads/videos" : "uploads/posters";
    fs.mkdirSync(folder, { recursive: true });
    cb(null, folder);
  },

  filename: (req, file, cb) => {
    const ext = file.originalname.split(".").pop();
    cb(null, Date.now() + "." + ext);
  },
});

const uploadVideo = multer({ storage });

module.exports = uploadVideo;
