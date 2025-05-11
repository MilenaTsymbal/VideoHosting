import './VideoCard.css'
import { Link } from 'react-router-dom';
import DeleteIcon from "../../assets/deleteIcon.png"

function VideoCard() {

  return (
      <div className="videoCardContainer">
        <img className="posterCard" src="https://miro.medium.com/v2/resize:fit:2000/0*zj_kGMq6f2ZxW7p3.png" alt="Постер видео"/>
        <div className="videoInfoContainer">
          <div className="infoContainer">
            <Link to="/channel">
              <img className="avatarPhoto" src="https://static1.srcdn.com/wordpress/wp-content/uploads/2025/02/avatar-the-last-airbender-had-the-perfect-ending-but-i-m-happy-that-we-re-getting-more1.jpg" alt="Аватарка канала"/>
            </Link>
            <div className="textInfoContainer">
              <Link to="/video" className="videoTitle">Video name</Link>
              <Link to="/channel" className="channelName">Имя канала</Link>
            </div>
          </div>
          <img className="deleteIcon" src={DeleteIcon} alt="" />
        </div>
      </div>
  )
}

export default VideoCard