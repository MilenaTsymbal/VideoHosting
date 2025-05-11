import { useState } from 'react';
import './ShareModal.css';

function ShareModal({ onClose }) {
  const [search, setSearch] = useState('');
  const [selectedUser, setSelectedUser] = useState('');

  const users = [
    { id: 1, nickname: 'john_doe' },
    { id: 2, nickname: 'jane_smith' },
    { id: 3, nickname: 'catlover99' },
    { id: 4, nickname: 'videoFan42' },
    { id: 5, nickname: 'admin' },
  ];

  const filteredUsers = users.filter((user) =>
    user.nickname.toLowerCase().includes(search.toLowerCase())
  );

  const handleSelect = (nickname) => {
    setSelectedUser(nickname);
    setSearch(nickname); 
  };

  const handleShare = () => {
    if (selectedUser) {
      alert(`Video link shared with ${selectedUser}!`);
      onClose();
    }
  };

  return (
    <div className="modalOverlay" onClick={onClose}>
      <div className="modalContainer" onClick={(e) => e.stopPropagation()}>
        <button className="closeButton" onClick={onClose}>X</button>
        <p className='shareText'>Share this video with a user:</p>
        <input
          className='shareInput'
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
                onClick={() => handleSelect(user.nickname)}
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
        <button className="shareButton" onClick={handleShare} disabled={!selectedUser}>
          Share
        </button>
      </div>
    </div>
  );
}

export default ShareModal;
