import { jwtDecode } from "jwt-decode";

export function getCurrentUser() {
  const token = localStorage.getItem("token");
  if (!token) return null;
  try {
    return jwtDecode(token);
  } catch (e) {
    return null;
  }
}
