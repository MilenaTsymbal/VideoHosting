import { useState } from "react";
import axios from "axios";
import "./AddVideoPage.css";
import { useNavigate } from "react-router-dom";

function AddVideoPage() {
  const [title, setTitle] = useState("");
  const [videoFile, setVideoFile] = useState(null);
  const [posterFile, setPosterFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    setLoading(true);

    const formData = new FormData();
    formData.append("title", title);
    formData.append("video", videoFile);
    formData.append("poster", posterFile);

    try {
      await axios.post("/api/videos", formData, {
        headers: {
          Authorization: token,
          "Content-Type": "multipart/form-data",
        },
      });
      const meRes = await axios.get("/api/users/me", {
        headers: { Authorization: token },
      });
      const username = meRes.data.username;
      alert("Video uploaded successfully!");
      navigate(`/channel/${username}`);
    } catch (err) {
      alert(err.response?.data?.message || "Upload failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="addVideo__container">
      <form className="addVideo__form" onSubmit={handleSubmit}>
        <h3 className="addVideo__title">Upload New Video</h3>

        <label className="addVideo__label" htmlFor="video-title">
          Video name:
        </label>
        <input
          className="addVideo__input"
          id="video-title"
          type="text"
          value={title}
          placeholder="Enter video name"
          onChange={(e) => setTitle(e.target.value)}
          required
        />

        <label className="addVideo__label">Upload video:</label>
        <input
          className="addVideo__input"
          type="file"
          accept="video/*"
          onChange={(e) => setVideoFile(e.target.files[0])}
          required
        />

        <label className="addVideo__label">Upload poster:</label>
        <input
          className="addVideo__input"
          type="file"
          accept="image/*"
          onChange={(e) => setPosterFile(e.target.files[0])}
        />

        <button className="addVideo__button" type="submit" disabled={loading}>
          {loading ? "Uploading..." : "Add Video"}
        </button>
      </form>
    </div>
  );
}

export default AddVideoPage;
