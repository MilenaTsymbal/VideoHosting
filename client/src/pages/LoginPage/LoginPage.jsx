import "./LoginPage.css";
import { useState } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import axios from "axios";

function LoginPage() {
  const [form, setForm] = useState({ email: "", password: "" });
  const [message, setMessage] = useState("");
  const location = useLocation();
  const from = location.state?.from || "/";
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post("/api/auth/login", form);
      localStorage.setItem("token", res.data.token);
      window.dispatchEvent(new Event("authChanged"));
      navigate(from, { replace: true });
    } catch (err) {
      setMessage(err.response?.data?.message || "Login error");
    }
  };

  return (
    <div className="loginPage__container">
      <h2 className="loginPage__title">Login</h2>
      <form
        className="loginPage__form"
        onSubmit={handleSubmit}
        autoComplete="on"
      >
        <div className="loginPage__formGroup">
          <label htmlFor="login-email" className="loginPage__label">
            Email
          </label>
          <input
            className="loginPage__input"
            id="login-email"
            type="email"
            name="email"
            value={form.email}
            onChange={handleChange}
            autoComplete="email"
            required
          />
        </div>
        <div className="loginPage__formGroup">
          <label htmlFor="login-password" className="loginPage__label">
            Password
          </label>
          <input
            className="loginPage__input"
            id="login-password"
            type="password"
            name="password"
            value={form.password}
            onChange={handleChange}
            autoComplete="current-password"
            required
          />
        </div>
        <button
          className="loginPage__button"
          type="submit"
          disabled={!form.email || !form.password}
        >
          Login
        </button>
        {message && (
          <p className="loginPage__error" aria-live="polite">
            {message}
          </p>
        )}
        <p className="loginPage__registerText">
          Don't have an account?{" "}
          <Link to="/register" className="loginPage__registerLink">
            Register
          </Link>
        </p>
      </form>
    </div>
  );
}

export default LoginPage;
