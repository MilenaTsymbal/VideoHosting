import "./CommentCard.css";
import { useState } from "react";
import EditIcon from "../../assets/editIcon.png";
import DeleteIcon from "../../assets/deleteIcon.png";
import formatTimeAgo from "../../../../server/utils/formatTimeAgo";

function CommentCard({ comment, currentUserId, onEdit, onDelete }) {
  const [isEditing, setIsEditing] = useState(false);
  const [editText, setEditText] = useState(comment.text);

  const avatar =
    comment.author?.avatarUrl ||
    "https://upload.wikimedia.org/wikipedia/commons/9/99/Sample_User_Icon.png";
  const username = comment.author?.username || "Unknown";
  const isOwner =
    currentUserId &&
    comment.author &&
    String(comment.author._id) === String(currentUserId);

  const handleEditSubmit = (e) => {
    e.preventDefault();
    onEdit(comment._id, editText);
    setIsEditing(false);
  };

  return (
    <div className="commentCard__container">
      <img className="commentCard__avatar" src={avatar} alt="avatar" />
      <div className="commentCard__textContainer">
        <div className="commentCard__headerRow">
          <span className="commentCard__author">{username}</span>
          <span className="commentCard__date">
            {formatTimeAgo(comment.createdAt)}
          </span>
        </div>
        {isEditing ? (
          <form onSubmit={handleEditSubmit}>
            <textarea
              value={editText}
              onChange={(e) => setEditText(e.target.value)}
              rows={2}
              className="commentCard__editTextarea"
            />
            <div className="commentCard__editActions">
              <button type="submit" className="commentCard__editButton">
                Save
              </button>
              <button
                type="button"
                className="commentCard__cancelButton"
                onClick={() => setIsEditing(false)}
              >
                Cancel
              </button>
            </div>
          </form>
        ) : (
          <div className="commentCard__bottomRow">
            <div className="commentCard__text">{comment.text}</div>
            {isOwner && (
              <div className="commentCard__actions">
                <button
                  type="button"
                  className="commentCard__actionButton"
                  aria-label="Edit comment"
                  onClick={() => setIsEditing(true)}
                >
                  <img
                    src={EditIcon}
                    alt=""
                    className="commentCard__actionIcon"
                    aria-hidden="true"
                  />
                </button>
                <button
                  type="button"
                  className="commentCard__actionButton"
                  aria-label="Delete comment"
                  onClick={() => onDelete(comment._id)}
                >
                  <img
                    src={DeleteIcon}
                    alt=""
                    className="commentCard__actionIcon"
                    aria-hidden="true"
                  />
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default CommentCard;
