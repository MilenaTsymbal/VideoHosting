import "./NotificationPage.css";
import { useEffect, useState } from "react";
import NotificationCard from "../../components/NotificationCard/NotificationCard";
import axios from "axios";

function NotificationPage() {
  const [notifications, setNotifications] = useState([]);
  const token = localStorage.getItem("token");

  useEffect(() => {
    if (!token) return;
    axios
      .get("/api/users/me/notifications", {
        headers: { Authorization: token },
      })
      .then((res) => setNotifications(res.data))
      .catch(() => setNotifications([]));
  }, [token]);

  return (
    <div className="notificationPage__container">
      <h2 className="notificationPage__title">Notifications</h2>
      {notifications.length === 0 && (
        <p className="notificationPage__empty">No notifications</p>
      )}
      <div className="notificationPage__list">
        {notifications.map((n) => (
          <NotificationCard key={n._id} notification={n} />
        ))}
      </div>
    </div>
  );
}

export default NotificationPage;
