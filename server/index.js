require("dotenv").config();
const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
const path = require("path");

const authRouter = require("./routes/authRouter");
const userRouter = require("./routes/userRouter");
const videoRouter = require("./routes/videoRouter");
const commentRouter = require("./routes/commentRouter");

const app = express();

app.use(cors());
app.use(express.json());
app.use("/uploads", express.static(path.join(__dirname, "uploads")));

app.use("/api/auth", authRouter);
app.use("/api/users", userRouter);
app.use("/api/videos", videoRouter);
app.use("/api/comments", commentRouter);

mongoose
  .connect(
    "mongodb+srv://yaroslavivanov2005:e9tfhlkuP6M4ItKa@videohosting.sfz2axf.mongodb.net/"
  )
  .then(() =>
    app.listen(5000, () => {
      console.log("Server is running on port 5000");
    })
  )
  .catch((err) => console.error(err));
