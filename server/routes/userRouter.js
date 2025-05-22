const express = require("express");
const router = express.Router();
const userController = require("../controllers/userController");
const authMiddleware = require("../middleware/authMiddleware");
const roleMiddleware = require("../middleware/roleMiddleware");

router.get("/me", authMiddleware, userController.getMe);
router.get("/channel/:username", userController.getUserByUsername);
router.put("/me", authMiddleware, userController.updateMe);
router.post(
  "/change-role",
  authMiddleware,
  roleMiddleware,
  userController.changeRole
);
router.delete(
  "/:id",
  authMiddleware,
  roleMiddleware,
  userController.deleteUser
);
router.post("/:id/subscribe", authMiddleware, userController.subscribe);
router.post("/:id/unsubscribe", authMiddleware, userController.unsubscribe);
router.get(
  "/me/notifications",
  authMiddleware,
  userController.getNotifications
);

module.exports = router;
