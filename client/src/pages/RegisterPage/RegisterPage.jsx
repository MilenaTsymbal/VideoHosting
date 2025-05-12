import './RegisterPage.css'
import { Link } from 'react-router-dom';

function RegisterPage() {

  return (
    <div className="addVideoPageContainer">
      <h2 className="addVideoTitle">RegisterPage</h2>
      <form className='addForm'>
        <div className="formGroup">
          <label htmlFor="email">Email</label>
          <input type="text" id="email" name="email" placeholder="Enter email" required />
        </div>
        <div className="formGroup">
          <label htmlFor="username">Username</label>
          <input type="text" id="username" name="username" placeholder="Enter username" required />
        </div>
        <div className="formGroup">
          <label htmlFor="password">Password</label>
          <input type="text" id="password" name="password" placeholder="Enter password" required />
        </div>
        <div className="formGroup">
          <label htmlFor="repeatPassword">Repeat password</label>
          <input type="text" id="repeatPassword" name="repeatPassword" placeholder="Enter repeat password" required />
        </div>
        <Link className="link" to="/">
            <button className="addButton" type="submit">Login</button>
        </Link>
      </form>
    </div>
  )
}

export default RegisterPage
