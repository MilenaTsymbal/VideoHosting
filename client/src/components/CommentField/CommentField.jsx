import './CommentField.css'

function CommentField() {

  return (
    <div>
        <p className="commentsTitle">Comments</p>
        <div className='commentFieldContainer'>
            <textarea name="" id=""></textarea>
            <button className='followButton'>Post</button>
        </div>
    </div>
  )
}

export default CommentField