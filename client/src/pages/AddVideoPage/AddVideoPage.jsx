import './AddVideoPage.css'
import { Link } from 'react-router-dom';

function AddVideoPage() {

  return (
    <div className="addVideoPageContainer">
      <h2 className="addVideoTitle">Add new video</h2>
      <form className='addForm'>
        <div className="formGroup">
          <label htmlFor="videoTitle">Назва відео:</label>
          <input type="text" id="videoTitle" name="videoTitle" placeholder="Введіть назву відео" required />
        </div>

        <div className="formGroup">
          <label htmlFor="videoFile">Завантажити відео:</label>
          <input type="file" id="videoFile" name="videoFile" accept="video/*" required />
        </div>

        <div className="formGroup">
          <label htmlFor="posterFile">Завантажити постер:</label>
          <input type="file" id="posterFile" name="posterFile" accept="image/*" required />
        </div>
        <Link className="link" to="/">
            <button className="addButton" type="submit">Add video</button>
        </Link>
      </form>
    </div>
  )
}

export default AddVideoPage
