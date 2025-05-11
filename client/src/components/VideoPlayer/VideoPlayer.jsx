import React, { useState } from 'react';
import './VideoPlayer.css';
import CommentCard from '../CommentCard/CommentCard.jsx';
import CommentField from '../CommentField/CommentField.jsx';
import Video from "../../videos/videoplayback.mp4";
import { Link } from 'react-router-dom';
import ShareModal from '../ShareModal/ShareModal.jsx';

function VideoPlayer() {
  const [isModalOpen, setModalOpen] = useState(false);

  const toggleModal = () => setModalOpen(!isModalOpen);

  return (
    <div>
      {isModalOpen && <ShareModal onClose={toggleModal} />}
      <video src={Video} controls poster="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSlGW_JZyoiq4vsQ_6VSts_ZcVtd7WVjiLJTA&s"></video>
      <p className="videoName">Video name</p>
      <div className='videoContainer' style={{ justifyContent: 'space-between' }}>
        <div className='videoContainer'>
          <Link to="/channel">
            <img className="avatar" src="https://m.media-amazon.com/images/M/MV5BZjQ1YTZmMjItZmZkMC00MGVmLTk1OTUtNzQzZTJjZGM1NjVlXkEyXkFqcGc@._V1_FMjpg_UX1000_.jpg" alt="" />
          </Link>
          <Link to="/channel" className='nickname'>Author nickname</Link>
          <button className="followButton">Follow</button>
        </div>

        <img 
          className="shareIcon" 
          src="https://img.icons8.com/?size=35&id=11504&format=png&color=000000" 
          alt="Share" 
          onClick={toggleModal}
          style={{ cursor: 'pointer' }}
        />
      </div>
      <CommentField />
      <CommentCard />
      <CommentCard />
      <CommentCard />
      <CommentCard />
      <CommentCard />
      <CommentCard />
      <CommentCard />
    </div>
  );
}

export default VideoPlayer;
