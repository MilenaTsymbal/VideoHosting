import './LoginPage.css'
import { Link } from 'react-router-dom';

function LoginPage() {

  return (
    <div className="addVideoPageContainer">
      <h2 className="addVideoTitle">Login</h2>
      <form className='addForm'>
        <div className="formGroup">
          <label htmlFor="username">Username</label>
          <input type="text" id="username" name="username" placeholder="Enter username" required />
        </div>
        <div className="formGroup">
          <label htmlFor="password">Password</label>
          <input type="text" id="password" name="password" placeholder="Enter password" required />
        </div>
        <Link className="link" to="/">
            <button className="addButton" type="submit">Login</button>
        </Link>
        <p className='dontHaveAndAccount'>
            Don't have an account?      
            <Link className="link register" to="/register">Register</Link>
        </p>
      </form>
    </div>
  )
}

export default LoginPage
