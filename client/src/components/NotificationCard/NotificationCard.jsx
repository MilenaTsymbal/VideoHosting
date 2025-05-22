import "./NotificationCard.css";
import { Link } from "react-router-dom";

function NotificationCard({ notification }) {
  if (!notification) return null;
  const { type, fromUser, video, text } = notification;

  return (
    <div className="notificationCard__container">
      <div className="notificationCard__mainInfo">
        <img
          className="notificationCard__avatar"
          src={
            fromUser?.avatarUrl ||
            "https://upload.wikimedia.org/wikipedia/commons/9/99/Sample_User_Icon.png"
          }
          alt={
            fromUser?.username ? `${fromUser.username} avatar` : "User avatar"
          }
        />
        <Link
          to={fromUser?.username ? `/channel/${fromUser.username}` : "#"}
          className="notificationCard__author"
        >
          {fromUser?.username || "System"}
        </Link>
        <div className="notificationCard__textContainer">
          <p className="notificationCard__text">
            {type === "subscribe" && "subscribed to you"}
            {type === "new_video" && (
              <>
                uploaded a new video:{" "}
                <Link to={`/video/${video?._id}`}>{video?.title}</Link>
              </>
            )}
            {type === "share" && (
              <>
                shared a video with you:{" "}
                <Link to={`/video/${video?._id}`}>{video?.title}</Link>
              </>
            )}
            {!["subscribe", "new_video", "share"].includes(type) && text}
          </p>
        </div>
      </div>
    </div>
  );
}

export default NotificationCard;
