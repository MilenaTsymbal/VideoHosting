import "./VideoPage.css";
import { useParams } from "react-router-dom";
import { useEffect, useState, useRef, useCallback } from "react";
import axios from "axios";
import VideoPlayer from "../../components/VideoPlayer/VideoPlayer.jsx";
import CommentField from "../../components/CommentField/CommentField.jsx";
import CommentCard from "../../components/CommentCard/CommentCard.jsx";

import "../../components/CommentField/CommentField.css";
import "../../components/CommentCard/CommentCard.css";
import "./VideoPage.css";
import { jwtDecode } from "jwt-decode";
import { getCurrentUser } from "../../../../server/utils/getCurrentUser.js";

const COMMENT_LIMIT = 10;

function VideoPage() {
  const { id } = useParams();
  const [video, setVideo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [comments, setComments] = useState([]);
  const [commentPage, setCommentPage] = useState(1);
  const [commentsHasMore, setCommentsHasMore] = useState(true);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [currentUser, setCurrentUser] = useState(null);

  const commentObserver = useRef();

  useEffect(() => {
    const user = getCurrentUser();
    setCurrentUser(user);
  }, []);

  const lastCommentRef = useCallback(
    (node) => {
      if (commentsLoading) return;
      if (commentObserver.current) commentObserver.current.disconnect();
      commentObserver.current = new window.IntersectionObserver((entries) => {
        if (
          entries[0].isIntersecting &&
          commentsHasMore &&
          !commentsLoading &&
          comments.length >= COMMENT_LIMIT
        ) {
          setCommentPage((prev) => prev + 1);
        }
      });
      if (node) commentObserver.current.observe(node);
    },
    [commentsLoading, commentsHasMore, comments.length]
  );

  useEffect(() => {
    setLoading(true);
    axios
      .get(`http://localhost:5000/api/videos/${id}`)
      .then((res) => {
        setVideo(res.data);
        setLoading(false);
      })
      .catch(() => {
        setError("Video not found");
        setLoading(false);
      });
  }, [id]);

  useEffect(() => {
    if (!video?._id) return;
    setCommentsLoading(true);
    axios
      .get(`/api/comments/${video._id}`, {
        params: { page: commentPage, limit: COMMENT_LIMIT },
      })
      .then((res) => {
        if (commentPage === 1) {
          setComments(res.data.comments);
        } else {
          setComments((prev) => [...prev, ...res.data.comments]);
        }
        setCommentsHasMore(res.data.comments.length === COMMENT_LIMIT);
        setCommentsLoading(false);
      })
      .catch(() => setCommentsLoading(false));
  }, [video?._id, commentPage]);

  const token = localStorage.getItem("token");
  let currentUserId = null;
  if (token) {
    try {
      const decoded = jwtDecode(token);
      currentUserId = decoded._id || decoded.id || decoded.userId;
    } catch (e) {
      currentUserId = null;
    }
  }

  const handleAddComment = async (text) => {
    const res = await axios.post(
      `http://localhost:5000/api/comments/${id}`,
      { text },
      { headers: { Authorization: token } }
    );
    setComments((prev) => [res.data, ...prev]);
  };

  const handleEditComment = async (commentId, newText) => {
    const res = await axios.put(
      `http://localhost:5000/api/comments/${commentId}`,
      { text: newText },
      { headers: { Authorization: token } }
    );
    setComments((prev) =>
      prev.map((c) => (c._id === commentId ? res.data : c))
    );
  };

  const handleDeleteComment = async (commentId) => {
    if (!window.confirm("Delete this comment?")) return;
    await axios.delete(`http://localhost:5000/api/comments/${commentId}`, {
      headers: { Authorization: token },
    });
    setComments((prev) => prev.filter((c) => c._id !== commentId));
  };

  const handleDeleteVideo = async (videoId) => {
    if (!window.confirm("Are you sure you want to delete this video?")) return;
    try {
      await axios.delete(`http://localhost:5000/api/videos/${videoId}`, {
        headers: { Authorization: token },
      });
      window.location.href = "/";
    } catch (e) {
      alert("Failed to delete video");
    }
  };

  const isOwner =
    currentUser &&
    video &&
    video.author &&
    video.author._id === currentUser._id;

  const isAdmin = currentUser?.role === "admin";

  if (loading) return <p className="videoPage__commentsLoading">Loading...</p>;
  if (!video)
    return <p className="videoPage__commentsLoading">Video not found</p>;

  return (
    <div className="videoPage__container">
      <VideoPlayer
        video={video}
        isOwner={isOwner}
        isAdmin={isAdmin}
        onDeleteVideo={handleDeleteVideo}
      />
      <div className="videoPage__commentsSection">
        {currentUser ? (
          <CommentField onAddComment={handleAddComment} />
        ) : (
          <p className="videoPage__authWarning">
            Only authorized users can comment.
          </p>
        )}
        <div className="videoPage__commentsList">
          {comments.map((comment, idx) =>
            idx === comments.length - 1 && commentsHasMore ? (
              <div ref={lastCommentRef} key={comment._id}>
                <CommentCard
                  comment={comment}
                  currentUserId={currentUserId}
                  onEdit={handleEditComment}
                  onDelete={handleDeleteComment}
                />
              </div>
            ) : (
              <CommentCard
                key={comment._id}
                comment={comment}
                currentUserId={currentUserId}
                onEdit={handleEditComment}
                onDelete={handleDeleteComment}
              />
            )
          )}
          {commentsLoading && (
            <p className="videoPage__commentsLoading">Loading comments...</p>
          )}
        </div>
      </div>
    </div>
  );
}

export default VideoPage;
