import "./Header.css";
import axios from "axios";
import { Link, useNavigate, useLocation } from "react-router-dom";
import Logo from "../../assets/logo.png";
import PlusIcon from "../../assets/plusIcon.png";
import NotificationIcon from "../../assets/notificationIcon.png";
import LogoutIcon from "../../assets/logoutIcon.png";
import { useEffect, useState } from "react";

function Header() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const location = useLocation();

  useEffect(() => {
    const fetchUser = () => {
      const token = localStorage.getItem("token");
      if (!token) {
        setUser(null);
        return;
      }
      axios
        .get("/api/users/me", {
          headers: { Authorization: token },
        })
        .then((res) => setUser(res.data))
        .catch(() => setUser(null));
    };

    fetchUser();
    window.addEventListener("authChanged", fetchUser);
    return () => window.removeEventListener("authChanged", fetchUser);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("token");
    setUser(null);
    window.dispatchEvent(new Event("authChanged"));
    navigate("/");
  };

  const isAuth = !!user;

  return (
    <header className="header">
      <div className="header__logoBlock">
        <img src={Logo} className="header__logo" alt="Logo" />
        <Link className="header__brand" to="/">
          VideoHosting
        </Link>
      </div>
      <nav className="header__nav">
        {isAuth && (
          <>
            <Link className="header__navLink" to="/notification">
              <img
                className="header__icon"
                src={NotificationIcon}
                alt="Notifications"
              />
            </Link>
            <Link className="header__navLink" to="/add-video">
              <img className="header__icon" src={PlusIcon} alt="Add Video" />
            </Link>
            <Link className="header__navLink" to={`/channel/${user?.username}`}>
              Profile
            </Link>
            <button
              className="header__navLink header__logoutBtn"
              onClick={handleLogout}
              aria-label="Logout"
              title="Logout"
            >
              <img
                className="header__icon"
                src={LogoutIcon}
                alt=""
                aria-hidden="true"
              />
            </button>
          </>
        )}

        {!isAuth && (
          <>
            <Link
              className="header__navLink"
              to="/login"
              state={{ from: location.pathname }}
            >
              Login
            </Link>
            <Link
              className="header__navLink"
              to="/register"
              state={{ from: location.pathname }}
            >
              Register
            </Link>
          </>
        )}
      </nav>
    </header>
  );
}

export default Header;
