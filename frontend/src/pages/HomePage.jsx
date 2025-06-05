import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom';
import { useDarkMode } from '../contexts/DarkModeContext';
import axios from 'axios';

const TopBar = ({ isDarkMode, setIsDarkMode }) => {
  const [btnText, setBtnText] = useState('☀️');
  const [isChecked, setIsChecked] = useState(isDarkMode); 

  // toggle 유지
  useEffect(() => {
    if(isDarkMode){
      setIsChecked(true);
      setBtnText('🌙');
    }else {
      setIsChecked(false);
      setBtnText('☀️');
    }
  }, [isDarkMode]);

  const toggleTheme = () => {
    setIsDarkMode(!isDarkMode);
    if(!isDarkMode) {
      localStorage.setItem('darkMode', JSON.stringify(true));
      setBtnText('🌙');
      // spring boot로 전달 (axios 사용) + withCredentials 설정으로 세션 유지
      axios.post(`${import.meta.env.VITE_API_URL}/smash/theme`, { theme: 'dark' }, { withCredentials: true })
        .catch(error => {
          console.error('There was an error updating the theme:', error);
        });

    }else {
      localStorage.setItem('darkMode', JSON.stringify(false));
      setBtnText('☀️');
      // 위와 동일하게 spring boot로 전달
      axios.post(`${import.meta.env.VITE_API_URL}/smash/theme`, { theme: 'light' }, { withCredentials: true })
        .catch(error => {
          console.error('There was an error updating the theme:', error);
        });
    }
  };

  return (
    <div className="top-bar">
      <div className="change-theme">
        <span className="toggle-text">{btnText}</span>
        <input type="checkbox" className="toggle-input" id="toggle" onChange={toggleTheme} checked={isChecked} />
        <label className="toggle-label" htmlFor="toggle"></label>
      </div>
      <Link to="/profile"><button className="login-btn">로그인</button></Link>
    </div>
  );
}

const Home = () => {
  const { isDarkMode, setIsDarkMode } = useDarkMode();
  return (
    <>
      <TopBar isDarkMode={isDarkMode} setIsDarkMode={setIsDarkMode} />
      <div className="home">
        {!isDarkMode && <img src="/images/logo3.png" alt="Smash Logo" />}
        {isDarkMode && <img src="/images/logo4.png" alt="Smash Logo" />}
        <h1>Welcome to the Home Page</h1>
        <p>This is the main page of our application.</p>
      </div>
    </>
  );
}

export default Home;