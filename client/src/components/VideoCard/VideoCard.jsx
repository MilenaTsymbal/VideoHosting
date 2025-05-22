import "./VideoCard.css";
import { Link } from "react-router-dom";
import DeleteIcon from "../../assets/deleteIcon.png";
import EditIcon from "../../assets/editIcon.png";
import formatTimeAgo from "../../../../server/utils/formatTimeAgo";

function VideoCard({
  video,
  currentUserRole,
  onDelete,
  onEdit,
  currentUserId,
  showDeleteForAdminOnly = false,
}) {
  if (!video) return null;

  const { _id, title, posterUrl, author, createdAt } = video;
  const avatar =
    author?.avatarUrl ||
    "https://upload.wikimedia.org/wikipedia/commons/9/99/Sample_User_Icon.png";
  const username = author?.username || "Unknown";

  const isOwner =
    currentUserId && author && String(author._id) === String(currentUserId);

  return (
    <div className="videoCard__container">
      <Link to={`/video/${_id}`}>
        <img
          className="videoCard__poster"
          src={`http://localhost:5000/${posterUrl.replace(/\\/g, "/")}`}
          alt="Video poster"
        />
      </Link>
      <div className="videoCard__meta">
        <Link to={`/channel/${username}`}>
          <img
            className="videoCard__avatar"
            src={avatar}
            alt="Channel avatar"
          />
        </Link>
        <div className="videoCard__info">
          <Link to={`/video/${_id}`} className="videoCard__title">
            {title}
          </Link>
          <Link to={`/channel/${username}`} className="videoCard__channel">
            {username}
          </Link>
          <span className="videoCard__date">{formatTimeAgo(createdAt)}</span>
        </div>
        {onDelete &&
          (showDeleteForAdminOnly
            ? currentUserRole === "admin"
            : currentUserRole === "admin" || isOwner) && (
            <div className="videoCard__actions">
              {onEdit && isOwner && (
                <button
                  className="videoCard__actionBtn"
                  aria-label="Edit video"
                  title="Edit"
                  onClick={() => onEdit(video)}
                  type="button"
                >
                  <img
                    src={EditIcon}
                    alt=""
                    className="videoCard__actionIcon"
                    aria-hidden="true"
                  />
                </button>
              )}
              <button
                className="videoCard__actionBtn"
                aria-label="Delete video"
                title="Delete"
                onClick={() => {
                  if (
                    window.confirm(
                      "Are you sure you want to delete this video?"
                    )
                  ) {
                    onDelete(video);
                  }
                }}
                type="button"
              >
                <img
                  src={DeleteIcon}
                  alt=""
                  className="videoCard__actionIcon"
                  aria-hidden="true"
                />
              </button>
            </div>
          )}
      </div>
    </div>
  );
}

export default VideoCard;
