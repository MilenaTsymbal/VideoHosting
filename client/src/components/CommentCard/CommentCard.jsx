import './CommentCard.css'
import { Link } from 'react-router-dom';

function CommentCard() {

  return (
      <div className="commentContainer">
        <img className="avatar" src="https://static1.srcdn.com/wordpress/wp-content/uploads/2025/02/avatar-the-last-airbender-had-the-perfect-ending-but-i-m-happy-that-we-re-getting-more1.jpg" alt="" />
        <div className="commentTextContainer">
            <Link to="/" className="commentAuthor">Author name</Link>
            <p className="commentText">Comment Text</p>
        </div>
      </div>
  )
}

export default CommentCard