import React, { useState } from "react";
import "./VideoPlayer.css";
import { Link } from "react-router-dom";
import ShareModal from "../ShareModal/ShareModal.jsx";
import DeleteIcon from "../../assets/deleteIcon.png";
import formatVideoDate from "../../../../server/utils/formatDate.js";
import likeIcon from "../../assets/likeIcon.png";
import likeIconActive from "../../assets/likeIconActive.png";
import dislikeIcon from "../../assets/dislikeIcon.png";
import dislikeIconActive from "../../assets/dislikeIconActive.png";
import axios from "axios";

function VideoPlayer({
  video,
  isOwner,
  isAdmin,
  onDeleteVideo,
  currentUserId,
}) {
  const [isModalOpen, setModalOpen] = useState(false);

  const [likeState, setLikeState] = useState({
    likes: video.likes?.length || 0,
    dislikes: video.dislikes?.length || 0,
    liked: video.likes?.includes(currentUserId),
    disliked: video.dislikes?.includes(currentUserId),
  });

  if (!video) return null;

  const toggleModal = () => setModalOpen(!isModalOpen);

  const handleLike = async () => {
    try {
      const res = await axios.post(
        `/api/videos/${video._id}/like`,
        {},
        { headers: { Authorization: localStorage.getItem("token") } }
      );
      setLikeState({
        likes: res.data.likes,
        dislikes: res.data.dislikes,
        liked: res.data.liked,
        disliked: false,
      });
    } catch (e) {}
  };

  const handleDislike = async () => {
    try {
      const res = await axios.post(
        `/api/videos/${video._id}/dislike`,
        {},
        { headers: { Authorization: localStorage.getItem("token") } }
      );
      setLikeState({
        likes: res.data.likes,
        dislikes: res.data.dislikes,
        liked: false,
        disliked: res.data.disliked,
      });
    } catch (e) {}
  };

  return (
    <div className="videoPlayer__wrapper">
      {isModalOpen && <ShareModal videoId={video._id} onClose={toggleModal} />}
      <video
        className="videoPlayer__video"
        src={`http://localhost:5000/${video.videoUrl.replace(/\\/g, "/")}`}
        controls
        poster={
          video.posterUrl
            ? `http://localhost:5000/${video.posterUrl.replace(/\\/g, "/")}`
            : ""
        }
      ></video>
      <p className="videoPlayer__title">{video.title}</p>
      <div className="videoPlayer__metaRow">
        <div className="videoPlayer__authorBlock">
          <Link to={`/channel/${video.author?.username}`}>
            <img
              className="videoPlayer__avatar"
              src={
                video.author?.avatarUrl ||
                "https://upload.wikimedia.org/wikipedia/commons/9/99/Sample_User_Icon.png"
              }
              alt="Channel avatar"
            />
          </Link>
          <Link
            to={`/channel/${video.author?.username}`}
            className="videoPlayer__nickname"
          >
            {video.author?.username}
          </Link>
        </div>
        <div className="videoPlayer__actions">
          <button
            className="videoPlayer__actionBtn"
            aria-label="Like"
            title="Like"
            type="button"
            onClick={handleLike}
            tabIndex={0}
          >
            <img src={likeState.liked ? likeIconActive : likeIcon} alt="Like" />
            <span>{likeState.likes}</span>
          </button>
          <button
            className="videoPlayer__actionBtn"
            aria-label="Dislike"
            title="Dislike"
            type="button"
            onClick={handleDislike}
            tabIndex={0}
          >
            <img
              src={likeState.disliked ? dislikeIconActive : dislikeIcon}
              alt="Dislike"
            />
            <span>{likeState.dislikes}</span>
          </button>
          <button
            className="videoPlayer__actionBtn"
            aria-label="Share video"
            title="Share"
            type="button"
            onClick={toggleModal}
            tabIndex={0}
          >
            <img
              className="videoPlayer__shareIcon"
              src="https://img.icons8.com/?size=35&id=11504&format=png&color=000000"
              alt=""
              aria-hidden="true"
            />
          </button>
          {(isOwner || isAdmin) && (
            <button
              className="videoPlayer__actionBtn"
              aria-label="Delete video"
              title="Delete"
              type="button"
              onClick={() => {
                if (
                  window.confirm("Are you sure you want to delete this video?")
                ) {
                  onDeleteVideo(video._id);
                }
              }}
              tabIndex={0}
            >
              <img
                className="videoPlayer__deleteIcon"
                src={DeleteIcon}
                alt=""
                aria-hidden="true"
              />
            </button>
          )}
        </div>
      </div>
      <div className="videoPlayer__published">
        Published: {formatVideoDate(video.createdAt)}
      </div>
    </div>
  );
}

export default VideoPlayer;
