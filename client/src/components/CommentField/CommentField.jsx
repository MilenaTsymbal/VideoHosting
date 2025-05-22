import "./CommentField.css";
import { useState } from "react";

function CommentField({ onAddComment }) {
  const [text, setText] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!text.trim()) return;
    onAddComment(text);
    setText("");
  };

  return (
    <form className="commentField__container" onSubmit={handleSubmit}>
      <label htmlFor="commentField__textarea" className="commentField__title">
        Comments
      </label>
      <textarea
        id="commentField__textarea"
        className="commentField__textarea"
        value={text}
        onChange={(e) => setText(e.target.value)}
        placeholder="Add a comment..."
        autoFocus
      />
      <div className="commentField__buttonWrapper">
        <button
          className="commentField__postButton"
          type="submit"
          disabled={!text.trim()}
        >
          Post
        </button>
      </div>
    </form>
  );
}

export default CommentField;
