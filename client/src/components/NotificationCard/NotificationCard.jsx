import './NotificationCard.css'
import { Link } from 'react-router-dom';
import TickIcon from '../../assets/tickIcon.png'

function NotificationCard() {

  return (
      <div className="notificationContainer">
        <div className="notificationMainInfo">
            <img className="avatar" src="https://static1.srcdn.com/wordpress/wp-content/uploads/2025/02/avatar-the-last-airbender-had-the-perfect-ending-but-i-m-happy-that-we-re-getting-more1.jpg" alt="" />
            <div className="notificationTextContainer">
                <Link to="/" className="notificationAuthor">Author name</Link>
                <p className="notificationText">Comment Text</p>
            </div>
        </div>
        <img className="tickIcon" src={TickIcon} alt="" />
      </div>
  )
}

export default NotificationCard