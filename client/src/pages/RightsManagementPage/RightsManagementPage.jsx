import './RightsManagementPage.css'
import { useState } from 'react';

function RightsManagementPage() {
    const [search, setSearch] = useState('');
    const [selectedUser, setSelectedUser] = useState('');
    const [selectedRole, setSelectedRole] = useState('');
  
    const users = [
      { id: 1, nickname: 'john_doe' },
      { id: 2, nickname: 'jane_smith' },
      { id: 3, nickname: 'catlover99' },
      { id: 4, nickname: 'videoFan42' },
      { id: 5, nickname: 'admin' },
    ];
  
    const roles = ['user', 'moderator', 'admin'];
  
    const filteredUsers = users.filter((user) =>
      user.nickname.toLowerCase().includes(search.toLowerCase())
    );
  
    const handleSelectUser = (nickname) => {
      setSelectedUser(nickname);
      setSearch(nickname);
    };
  
    const handleSubmit = (e) => {
      e.preventDefault();
      if (selectedUser && selectedRole) {
        alert(`User ${selectedUser} was assigned role "${selectedRole}"`);
        // Тут можна зробити POST-запит
      }
    };

  return (
    <div className="addVideoPageContainer">
      <h2 className="addVideoTitle">Rights management</h2>
      <form className='addForm'>
      <div className="formGroup">
          <label htmlFor="username">Username:</label>
          <input
            type="text"
            placeholder="Type a username..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          {search && (
            <ul className="dropdown">
              {filteredUsers.map((user) => (
                <li
                  key={user.id}
                  onClick={() => handleSelectUser(user.nickname)}
                  className="dropdownItem"
                >
                  {user.nickname}
                </li>
              ))}
              {filteredUsers.length === 0 && (
                <li className="dropdownItem">No users found</li>
              )}
            </ul>
          )}
        </div>

        <div className="formGroup">
          <label htmlFor="role">Role:</label>
          <select className="select" id="role" name="role" required>
            <option value="user">User</option>
            <option value="moderator">Admin</option>
          </select>
        </div>

        <button className="addButton" type="submit">Зберегти</button>
      </form>
    </div>
  )
}

export default RightsManagementPage
