const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const User = require("../models/User");

const DEFAULT_AVATAR =
  "https://upload.wikimedia.org/wikipedia/commons/9/99/Sample_User_Icon.png";

exports.register = async (req, res) => {
  try {
    const { username, email, password, confirmPassword } = req.body;

    if (!username || !email || !password || !confirmPassword) {
      return res.status(400).json({ message: "All fields are required" });
    }
    if (password !== confirmPassword) {
      return res.status(400).json({ message: "Passwords do not match" });
    }
    if (password.length < 6) {
      return res
        .status(400)
        .json({ message: "Password must be at least 6 characters" });
    }

    const existingUserByEmail = await User.findOne({ email });
    if (existingUserByEmail)
      return res.status(400).json({ message: "User already exists" });

    const existingUserByUsername = await User.findOne({ username });
    if (existingUserByUsername)
      return res.status(400).json({ message: "Username already exists" });

    const hashed = await bcrypt.hash(password, 10);
    const user = new User({
      username,
      email,
      password: hashed,
      avatarUrl: DEFAULT_AVATAR,
    });

    await user.save();

    res.json({ message: "User registered successfully" });
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};

exports.login = async (req, res) => {
  try {
    const { email, password } = req.body;
    const user = await User.findOne({ email });
    if (!user) return res.status(400).json({ message: "Invalid credentials" });
    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch)
      return res.status(400).json({ message: "Invalid credentials" });

    const token = jwt.sign(
      {
        _id: user._id,
        role: user.role,
        username: user.username,
      },
      process.env.JWT_SECRET || "my_jwt_secret_key",
      { expiresIn: "1d" }
    );

    res.json({
      token,
      user: {
        _id: user._id,
        username: user.username,
        email: user.email,
        avatarUrl: user.avatarUrl,
      },
    });
  } catch (err) {
    res.status(500).json({ message: "Server error", error: err.message });
  }
};
