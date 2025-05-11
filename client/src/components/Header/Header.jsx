import './Header.css'
import { Link } from 'react-router-dom';
import Logo from '../../assets/logo.png'
import PlusIcon from '../../assets/plusIcon.png'
import NotificationIcon from '../../assets/notificationIcon.png'

function Header() {

  return (
    <div className='containerHeader'>
      <div className='logoAndNameContainer'>
        <img src={Logo} className="logo" alt="" />
        <Link className="name" to="/">VideoHosting</Link>
      </div>
      <div className='links'>
        <Link className="link" to="/notification">
            <img className="notificationIcon" src={NotificationIcon} alt="" />
        </Link>
        <Link className="link" to="/add-video">
            <img className="notificationIcon" src={PlusIcon} alt="" />
        </Link>
        <Link className="link" to="/login">Login</Link>
        <Link className="link" to="/channel">Profile</Link>
      </div>
    </div>
  )
}

export default Header
