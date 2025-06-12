import { useEffect, useState } from 'react'
import { useNavigate  } from 'react-router-dom';
import { Link } from 'react-router-dom';
import { useDarkMode } from '../contexts/DarkModeContext';
import axios from 'axios';
import RequestList from '../pages/RequestList';

const Home = () => {
  const { isDarkMode, setIsDarkMode } = useDarkMode();
  
  return (
    <>
      <TopBar isDarkMode={isDarkMode} setIsDarkMode={setIsDarkMode} />
      <div className="home">
        {!isDarkMode && <img src="/images/logo3.png" alt="Smash Logo" />}
        {isDarkMode && <img src="/images/logo4.png" alt="Smash Logo" />}
        <RequestList />
      </div>
    </>
  );
}

export default Home;

// TopBar Component (1회만 사용하므로, 별도 파일로 분리하지 않음)
const TopBar = ({ isDarkMode, setIsDarkMode }) => {
  const [btnText, setBtnText] = useState('☀️');
  const [isChecked, setIsChecked] = useState(isDarkMode); 
  const baseUrl = import.meta.env.VITE_API_URL;
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    // 로그인 상태 확인
    const checkLoginStatus = async () => {
      try {
        const response = await axios.get(`${baseUrl}/smash/member/user`, { withCredentials: true });
        if (response.data) {
          // 유저정보
          console.log('User is logged in:', response.data);
          setIsLoggedIn(true);
        } else {
          console.log('User is not logged in');
          setIsLoggedIn(false);
        }
      } catch (error) {
        console.error('Error checking login status:', error);
        setIsLoggedIn(false);
      }
    };
    checkLoginStatus();
  }, []);

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

  const logoutHandler = async () => {
    try {
      await axios.post(`${baseUrl}/logout`, {}, { withCredentials: true });
      setIsLoggedIn(false);
      console.log('로그아웃 성공');
      navigate('/'); // 로그아웃 후 홈으로 리다이렉트
    } catch (error) {
      console.error('로그아웃 실패:', error);
    }
  }

  return (
    <div className="top-bar">
      <div className="change-theme">
        <span className="toggle-text">{btnText}</span>
        <input type="checkbox" className="toggle-input" id="toggle" onChange={toggleTheme} checked={isChecked} />
        <label className="toggle-label" htmlFor="toggle"></label>
      </div>
      {isLoggedIn ?
      (<button className="login-btn" onClick={logoutHandler}>로그아웃</button>)
      :
      (<Link to="/profile"><button className="login-btn">로그인</button></Link>)}
      
    </div>
  );
}