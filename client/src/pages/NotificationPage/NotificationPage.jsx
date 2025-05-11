import './NotificationPage.css'
import { Link } from 'react-router-dom';
import NotificationCard from '../../components/NotificationCard/NotificationCard';

function NotificationPage() {

  return (
    <div className='notificationsContainer'>
        <h2 className="addVideoTitle">Notifications</h2>
        <p className="commentsTitle">Unread</p>
        <NotificationCard />      
        <NotificationCard />      
        <NotificationCard />      
        <NotificationCard />      
        <p className="commentsTitle">Read</p>
        <NotificationCard /> 
        <NotificationCard /> 
        <NotificationCard /> 
        <NotificationCard /> 
    </div>
  )
}

export default NotificationPage
