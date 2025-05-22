import "./RegisterPage.css";
import { useState } from "react";
import axios from "axios";
import { useNavigate, useLocation } from "react-router-dom";

function RegisterPage() {
  const [form, setForm] = useState({
    email: "",
    username: "",
    password: "",
    repeatPassword: "",
  });
  const [message, setMessage] = useState("");
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from || "/";
  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (form.password !== form.repeatPassword) {
      setMessage("Passwords do not match");
      return;
    }

    try {
      await axios.post("/api/auth/register", {
        email: form.email,
        username: form.username,
        password: form.password,
        confirmPassword: form.repeatPassword,
      });

      const loginRes = await axios.post("/api/auth/login", {
        email: form.email,
        password: form.password,
      });

      localStorage.setItem("token", loginRes.data.token);
      window.dispatchEvent(new Event("authChanged"));
      navigate(from, { replace: true });
    } catch (err) {
      setMessage(err.response?.data?.message || "Registration failed");
    }
  };

  return (
    <div className="registerPage__container">
      <h2 className="registerPage__title">Registration</h2>
      <form className="registerPage__form" onSubmit={handleSubmit}>
        <div className="registerPage__formGroup">
          <label htmlFor="email" className="registerPage__label">
            Email
          </label>
          <input
            className="registerPage__input"
            type="email"
            id="email"
            name="email"
            placeholder="Enter email"
            required
            autoComplete="email"
            value={form.email}
            onChange={handleChange}
          />
        </div>
        <div className="registerPage__formGroup">
          <label htmlFor="username" className="registerPage__label">
            Username
          </label>
          <input
            className="registerPage__input"
            type="text"
            id="username"
            name="username"
            placeholder="Enter username"
            required
            value={form.username}
            onChange={handleChange}
          />
        </div>
        <div className="registerPage__formGroup">
          <label htmlFor="password" className="registerPage__label">
            Password
          </label>
          <input
            className="registerPage__input"
            type="password"
            id="password"
            name="password"
            placeholder="Enter password"
            required
            autoComplete="new-password"
            value={form.password}
            onChange={handleChange}
          />
        </div>
        <div className="registerPage__formGroup">
          <label htmlFor="repeatPassword" className="registerPage__label">
            Repeat password
          </label>
          <input
            className="registerPage__input"
            type="password"
            id="repeatPassword"
            name="repeatPassword"
            placeholder="Repeat password"
            required
            value={form.repeatPassword}
            onChange={handleChange}
          />
        </div>
        <button className="registerPage__button" type="submit">
          Register
        </button>
        {message && (
          <p className="registerPage__error" aria-live="polite">
            {message}
          </p>
        )}{" "}
      </form>
    </div>
  );
}

export default RegisterPage;
