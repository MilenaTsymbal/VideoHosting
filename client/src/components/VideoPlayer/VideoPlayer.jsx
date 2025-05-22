import React, { useState } from "react";
import "./VideoPlayer.css";
import { Link } from "react-router-dom";
import ShareModal from "../ShareModal/ShareModal.jsx";
import DeleteIcon from "../../assets/deleteIcon.png";
import formatVideoDate from "../../../../server/utils/formatDate.js";

function VideoPlayer({ video, isOwner, isAdmin, onDeleteVideo }) {
  const [isModalOpen, setModalOpen] = useState(false);

  if (!video) return null;

  const toggleModal = () => setModalOpen(!isModalOpen);

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
            aria-label="Share video"
            title="Share"
            type="button"
            onClick={toggleModal}
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
