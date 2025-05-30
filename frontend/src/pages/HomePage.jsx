import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom';

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
      document.body.classList.add('dark');
      localStorage.setItem('darkMode', JSON.stringify(true));
      setBtnText('🌙');
    }else {
      document.body.classList.remove('dark');
      localStorage.setItem('darkMode', JSON.stringify(false));
      setBtnText('☀️');
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

const Home = ({ isDarkMode, setIsDarkMode }) => {
  return (
    <>
      <TopBar isDarkMode={isDarkMode} setIsDarkMode={setIsDarkMode} />
      <div className="home">
        {!isDarkMode && <img src="/logo.png" alt="Smash Logo" />}
        {isDarkMode && <img src="/logo2.png" alt="Smash Logo" />}
        <h1>Welcome to the Home Page</h1>
        <p>This is the main page of our application.</p>
      </div>
    </>
  );
}

export default Home;