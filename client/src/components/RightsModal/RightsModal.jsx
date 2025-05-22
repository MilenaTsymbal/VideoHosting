import { useState } from "react";
import "./RightsModal.css";

function RightsModal({ user, onClose, onRoleChange, onDeleteUser }) {
  const [role, setRole] = useState(user.role || "user");

  const handleSave = () => {
    onRoleChange(user._id, role);
    onClose();
  };

  const handleDelete = () => {
    if (window.confirm("Are you sure you want to delete this user?")) {
      onDeleteUser(user._id);
      onClose();
    }
  };

  return (
    <div
      className="rightsModal__overlay"
      role="dialog"
      aria-modal="true"
      aria-labelledby="rightsModal__title"
    >
      <div className="rightsModal__container">
        <h3 className="rightsModal__title">Manage Rights</h3>
        <label className="rightsModal__label">
          Role:
          <select
            className="rightsModal__select"
            value={role}
            onChange={(e) => setRole(e.target.value)}
          >
            <option value="user">User</option>
            <option value="admin">Admin</option>
          </select>
        </label>
        <div className="rightsModal__actions">
          <button className="rightsModal__saveBtn" onClick={handleSave}>
            Save
          </button>
          <button className="rightsModal__cancelBtn" onClick={onClose}>
            Cancel
          </button>
        </div>
        <button className="rightsModal__deleteBtn" onClick={handleDelete}>
          Delete User
        </button>
      </div>
    </div>
  );
}

export default RightsModal;
