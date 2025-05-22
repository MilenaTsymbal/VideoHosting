import { useState } from "react";
import axios from "axios";
import "./ShareModal.css";

function ShareModal({ videoId, onClose }) {
  const [search, setSearch] = useState("");
  const [selectedUser, setSelectedUser] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleShare = async () => {
    setError("");
    setSuccess("");
    if (!selectedUser) return;
    try {
      const token = localStorage.getItem("token");
      await axios.post(
        `/api/videos/${videoId}/share`,
        { toUserId: selectedUser },
        { headers: { Authorization: token } }
      );
      setSuccess(`Video shared with ${selectedUser}!`);
      setTimeout(onClose, 1200);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to share video");
    }
  };

  return (
    <div
      className="shareModal__overlay"
      role="dialog"
      aria-modal="true"
      aria-labelledby="shareModal__title"
      onClick={onClose}
    >
      <div
        className="shareModal__container"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          className="shareModal__closeBtn"
          onClick={onClose}
          aria-label="Close"
          type="button"
        >
          ×
        </button>
        <p className="shareModal__title" id="shareModal__title">
          Share this video with a user:
        </p>
        <div className="shareModal__inputRow">
          <input
            className="shareModal__input"
            type="text"
            placeholder="Type a username..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setSelectedUser(e.target.value);
              setError("");
              setSuccess("");
            }}
          />
          <button
            className="shareModal__shareBtn"
            onClick={handleShare}
            disabled={!selectedUser}
          >
            Share
          </button>
        </div>
        {localStorage.getItem("token") ? null : (
          <div className="shareModal__authWarning">
            Only authorized users can share videos.
          </div>
        )}
        {error && <div className="shareModal__error">{error}</div>}
        {success && <div className="shareModal__success">{success}</div>}
      </div>
    </div>
  );
}

export default ShareModal;
