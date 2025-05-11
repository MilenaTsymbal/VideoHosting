import './ChannelPage.css'
import { Link } from 'react-router-dom';
import VideoCard from '../../components/VideoCard/VideoCard.jsx'

function ChannelPage() {

  return (
    <div>
        <div className='channelInfoContainer'>
            <img className="channelPhoto" src="https://static1.srcdn.com/wordpress/wp-content/uploads/2025/02/avatar-the-last-airbender-had-the-perfect-ending-but-i-m-happy-that-we-re-getting-more1.jpg" alt="" />
            <h2>Channel name</h2>
            <button className="followButton">Follow</button>
            <Link className="link" to="/rights">
              <button className="followButton rightsButton">Rights management</button>
            </Link>
        </div>
        <div className='homeContainer'>
            <VideoCard />
            <VideoCard />
            <VideoCard />
            <VideoCard />
            <VideoCard />
            <VideoCard />
            <VideoCard />
            <VideoCard />
            <VideoCard />
        </div>
    </div>
  )
}

export default ChannelPage
