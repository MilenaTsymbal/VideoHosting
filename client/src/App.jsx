import './App.css'
import Header from './components/Header/Header.jsx'
import HomePage from './pages/HomePage/HomePage.jsx'
import VideoPage from './pages/VideoPage/VideoPage.jsx'
import NotificationPage from './pages/NotificationPage/NotificationPage.jsx'
import ChannelPage from './pages/ChannelPage/ChannelPage.jsx'
import AddVideoPage from './pages/AddVideoPage/AddVideoPage.jsx'
import LoginPage from './pages/LoginPage/LoginPage.jsx'
import RegisterPage from './pages/RegisterPage/RegisterPage.jsx'
import RightsManagementPage from './pages/RightsManagementPage/RightsManagementPage.jsx'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

function App() {

  return (
    <Router>
    <Header />
      <div className='container'>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/video" element={<VideoPage />} />
          <Route path="/notification" element={<NotificationPage />} />
          <Route path="/channel" element={<ChannelPage />} />
          <Route path="/add-video" element={<AddVideoPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/rights" element={<RightsManagementPage />} />
        </Routes>
      </div>
  </Router>
  )
}

export default App
