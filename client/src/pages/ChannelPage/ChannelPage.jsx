import "./ChannelPage.css";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import VideoCard from "../../components/VideoCard/VideoCard.jsx";
import RightsModal from "../../components/RightsModal/RightsModal.jsx";

function ChannelPage() {
  const [channelUser, setChannelUser] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);
  const [videos, setVideos] = useState([]);
  const [error, setError] = useState("");
  const [editMode, setEditMode] = useState(false);
  const [newUsername, setNewUsername] = useState("");
  const [newAvatarUrl, setNewAvatarUrl] = useState("");
  const { username } = useParams();
  const [editVideoModal, setEditVideoModal] = useState(false);
  const [editingVideo, setEditingVideo] = useState(null);
  const [editTitle, setEditTitle] = useState("");
  const [editPoster, setEditPoster] = useState(null);
  const [rightsModalOpen, setRightsModalOpen] = useState(false);
  const [isSubscribed, setIsSubscribed] = useState(false);

  const token = localStorage.getItem("token");

  useEffect(() => {
    setError("");

    axios
      .get(`/api/users/channel/${username}`)
      .then((res) => {
        setChannelUser(res.data.user);
        setVideos(res.data.videos);
        const token = localStorage.getItem("token");
        if (token) {
          axios
            .get("/api/users/me", {
              headers: { Authorization: token },
            })
            .then((meRes) => {
              setCurrentUser(meRes.data);
              setIsSubscribed(
                res.data.user.followers?.includes(meRes.data._id)
              );
            })
            .catch(() => setCurrentUser(null));
        } else {
          setCurrentUser(null);
        }
      })
      .catch(() => setError("Failed to fetch channel user"));
  }, [username]);

  const handleSave = () => {
    axios
      .put(
        "/api/users/me",
        { username: newUsername, avatarUrl: newAvatarUrl },
        {
          headers: { Authorization: token },
        }
      )
      .then((res) => {
        setChannelUser(res.data);
        setEditMode(false);
      })
      .catch((err) => alert(err.response?.data?.message || "Update failed"));
  };

  const handleEdit = (video) => {
    setEditingVideo(video);
    setEditTitle(video.title);
    setEditPoster(null);
    setEditVideoModal(true);
  };

  const handleSaveEdit = async () => {
    const token = localStorage.getItem("token");
    const formData = new FormData();
    formData.append("title", editTitle);
    if (editPoster) formData.append("poster", editPoster);

    try {
      const res = await axios.put(`/api/videos/${editingVideo._id}`, formData, {
        headers: {
          Authorization: token,
          "Content-Type": "multipart/form-data",
        },
      });
      setVideos((prev) =>
        prev.map((v) => (v._id === editingVideo._id ? res.data.video : v))
      );
      setEditVideoModal(false);
      setEditingVideo(null);
    } catch (err) {
      alert(err.response?.data?.message || "Update failed");
    }
  };

  const handleDelete = async (video) => {
    if (!window.confirm("Are you sure you want to delete this video?")) return;
    const token = localStorage.getItem("token");
    try {
      await axios.delete(`/api/videos/${video._id}`, {
        headers: { Authorization: token },
      });
      setVideos((prev) => prev.filter((v) => v._id !== video._id));
    } catch (err) {
      alert(err.response?.data?.message || "Delete failed");
    }
  };

  const handleRoleChange = async (userId, role) => {
    await axios.post(
      `http://localhost:5000/api/users/change-role`,
      { userId, role },
      { headers: { Authorization: token } }
    );
    setChannelUser((prev) => ({ ...prev, role }));
  };

  const handleDeleteUser = async (userId) => {
    await axios.delete(`http://localhost:5000/api/users/${userId}`, {
      headers: { Authorization: token },
    });
  };

  const handleSubscribe = async () => {
    try {
      await axios.post(
        `/api/users/${channelUser._id}/subscribe`,
        {},
        { headers: { Authorization: token } }
      );
      setIsSubscribed(true);
      setChannelUser((prev) => ({
        ...prev,
        followersCount: prev.followersCount + 1,
      }));
    } catch (e) {
      alert("Failed to subscribe");
    }
  };

  const handleUnsubscribe = async () => {
    try {
      await axios.post(
        `/api/users/${channelUser._id}/unsubscribe`,
        {},
        { headers: { Authorization: token } }
      );
      setIsSubscribed(false);
      setChannelUser((prev) => ({
        ...prev,
        followersCount: prev.followersCount - 1,
      }));
    } catch (e) {
      alert("Failed to unsubscribe");
    }
  };

  if (error) return <p>{error}</p>;
  if (!channelUser) return <p>Loading...</p>;

  const isOwner = currentUser && channelUser._id === currentUser._id;
  const isAdmin = currentUser?.role === "admin";

  return (
    <div>
      <div className="channelPage__infoContainer">
        {channelUser.avatarUrl && channelUser.avatarUrl.trim() !== "" ? (
          <img
            className="channelPage__photo"
            src={channelUser.avatarUrl}
            alt="Channel Avatar"
          />
        ) : (
          <span className="material-symbols-outlined channelPage__iconPhoto">
            account_circle
          </span>
        )}

        <h2>{channelUser.username}</h2>
        <div style={{ marginLeft: 24 }}>
          <div className="channelPage__stats">
            <span>
              <b>{channelUser.followersCount}</b> followers
            </span>
            {" | "}
            <span>
              <b>{channelUser.subscriptionsCount}</b> subscriptions
            </span>
          </div>
          {!isOwner &&
            currentUser &&
            (isSubscribed ? (
              <button
                className="channelPage__followButton"
                onClick={handleUnsubscribe}
              >
                Unsubscribe
              </button>
            ) : (
              <button
                className="channelPage__followButton"
                onClick={handleSubscribe}
              >
                Subscribe
              </button>
            ))}
        </div>
        {isOwner && (
          <button
            className="channelPage__followButton"
            onClick={() => setEditMode(true)}
          >
            Edit Profile
          </button>
        )}
        {isAdmin && !isOwner && (
          <button
            className="channelPage__followButton channelPage__rightsButton"
            onClick={() => setRightsModalOpen(true)}
          >
            Rights management
          </button>
        )}
        {editMode && (
          <div
            className="channelPage__modalBackdrop"
            role="dialog"
            aria-modal="true"
            aria-labelledby="editProfileTitle"
            tabIndex={-1}
            onClick={() => setEditMode(false)}
          >
            <div
              className="channelPage__modalContent"
              onClick={(e) => e.stopPropagation()}
            >
              <h3>Edit Profile</h3>
              <input
                type="text"
                value={newUsername}
                onChange={(e) => setNewUsername(e.target.value)}
                placeholder="New username"
              />
              <input
                type="text"
                value={newAvatarUrl}
                onChange={(e) => setNewAvatarUrl(e.target.value)}
                placeholder="New avatar URL"
              />
              <div className="channelPage__modalActions">
                <button onClick={handleSave}>Save</button>
                <button
                  className="channelPage__cancelButton"
                  onClick={() => setEditMode(false)}
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        )}
        {editVideoModal && (
          <div
            className="channelPage__modalBackdrop"
            role="dialog"
            aria-modal="true"
            aria-labelledby="editVideoTitle"
            tabIndex={-1}
            onClick={() => setEditVideoModal(false)}
          >
            <div
              className="channelPage__modalContent"
              onClick={(e) => e.stopPropagation()}
            >
              <h3 id="editVideoTitle">Edit Video</h3>
              <input
                type="text"
                value={editTitle}
                onChange={(e) => setEditTitle(e.target.value)}
                placeholder="Video title"
              />
              <input
                type="file"
                accept="image/*"
                onChange={(e) => setEditPoster(e.target.files[0])}
              />
              <div className="channelPage__modalActions">
                <button onClick={handleSaveEdit}>Save</button>
                <button
                  className="channelPage__cancelButton"
                  onClick={() => setEditVideoModal(false)}
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
      <div className="channelPage__videosContainer">
        {videos.length > 0 ? (
          [...videos]
            .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
            .map((video) => {
              const isOwner =
                currentUser &&
                video.author &&
                video.author._id === currentUser._id;
              return (
                <VideoCard
                  key={video._id}
                  video={video}
                  currentUserRole={currentUser?.role}
                  currentUserId={currentUser?._id}
                  onEdit={handleEdit}
                  onDelete={handleDelete}
                />
              );
            })
        ) : (
          <p>No videos yet</p>
        )}
        {rightsModalOpen && (
          <RightsModal
            user={channelUser}
            onClose={() => setRightsModalOpen(false)}
            onRoleChange={handleRoleChange}
            onDeleteUser={handleDeleteUser}
          />
        )}
      </div>
    </div>
  );
}

export default ChannelPage;
