import { useState } from 'react'
import { Link } from 'react-router-dom';

const TopBar = () => {
  const [btnText, setBtnText] = useState('DarkMode🌙');
  const [isDarkMode, setIsDarkMode] = useState(false);

  const toggleTheme = () => {
    setIsDarkMode(!isDarkMode);
    if(!isDarkMode) {
      document.body.classList.add('dark');
      setBtnText('LoghtMode☀️');
    }else {
      document.body.classList.remove('dark');
      setBtnText('DarkMode🌙');
    }
  };

  return (
    <div className="top-bar">
      <Link to="/profile"><button className="login-btn">로그인</button></Link>
      <button id="toggle-theme" onClick={toggleTheme}>{btnText}</button>
    </div>
  );
}

export default TopBar;