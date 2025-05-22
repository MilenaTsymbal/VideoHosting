import { useEffect, useState, useRef, useCallback } from "react";
import { jwtDecode } from "jwt-decode";
import axios from "axios";
import "./HomePage.css";
import VideoCard from "../../components/VideoCard/VideoCard";

const LIMIT = 15;

const HomePage = () => {
  const [videos, setVideos] = useState([]);
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [currentUser, setCurrentUser] = useState(null);

  const observer = useRef();

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      try {
        const decoded = jwtDecode(token);
        setCurrentUser({
          _id: decoded._id || decoded.id || decoded.userId,
          role: decoded.role,
        });
      } catch {
        setCurrentUser(null);
      }
    } else {
      setCurrentUser(null);
    }
  }, []);

  const lastVideoRef = useCallback(
    (node) => {
      if (loading) return;
      if (observer.current) observer.current.disconnect();
      observer.current = new window.IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && hasMore) {
          setPage((prev) => prev + 1);
        }
      });
      if (node) observer.current.observe(node);
    },
    [loading, hasMore, videos.length]
  );

  useEffect(() => {
    setLoading(true);
    axios
      .get("/api/videos", {
        params: { page, limit: LIMIT },
      })
      .then((res) => {
        if (page === 1) {
          setVideos(res.data.videos);
        } else {
          setVideos((prev) => [...prev, ...res.data.videos]);
        }
        setHasMore(res.data.videos.length === LIMIT);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, [page]);

  const handleDelete = async (video) => {
    try {
      await axios.delete(`/api/videos/${video._id}`, {
        headers: { Authorization: localStorage.getItem("token") },
      });
      setVideos((prev) => prev.filter((v) => v._id !== video._id));
    } catch (e) {
      alert("Failed to delete video");
    }
  };

  return (
    <div className="homePage__container">
      {videos.length === 0 && !loading ? (
        <p className="homePage__empty">There are no videos yet</p>
      ) : (
        videos.map((video, idx) => {
          return idx === videos.length - 1 ? (
            <div ref={lastVideoRef} key={video._id}>
              <VideoCard
                video={video}
                currentUserRole={currentUser?.role}
                currentUserId={currentUser?._id}
                onDelete={handleDelete}
                showDeleteForAdminOnly={true}
              />
            </div>
          ) : (
            <VideoCard
              key={video._id}
              video={video}
              currentUserRole={currentUser?.role}
              currentUserId={currentUser?._id}
              onDelete={handleDelete}
              showDeleteForAdminOnly={true}
            />
          );
        })
      )}
      {loading && <p className="homePage__loading">Loading...</p>}
    </div>
  );
};

export default HomePage;
