import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom';
import { useDarkMode } from '../contexts/DarkModeContext';
import { useUser } from '../contexts/UserContext';
import axios from 'axios';
import RequestList from '../pages/RequestList';

const Home = () => {
  const { isDarkMode, setIsDarkMode } = useDarkMode();
  const baseUrl = import.meta.env.VITE_API_URL;
  const user = useUser();
  console.log("user:", user);
  
  return (
    <>
      <TopBar isDarkMode={isDarkMode} setIsDarkMode={setIsDarkMode} user={user} />
      <div className="home">
        {!isDarkMode && <img src="/images/logo3.png" alt="Smash Logo" />}
        {isDarkMode && <img src="/images/logo4.png" alt="Smash Logo" />}
        <RequestList />

        {user && user.role === 0 && // 일반 사용자일 때만 요청 작성 버튼 표시
        <div className="reg_button_box">
          <a className="register_btn" href={`${baseUrl}/smash/request/register`}>
            <i className="fa-solid fa-plus"></i>
          </a>
        </div>
        }
      </div>
    </>
  );
}

export default Home;

// TopBar Component (1회만 사용하므로, 별도 파일로 분리하지 않음)
const TopBar = ({ isDarkMode, setIsDarkMode, user }) => {
  const baseUrl = import.meta.env.VITE_API_URL;
  const [btnText, setBtnText] = useState('☀️');
  const [isChecked, setIsChecked] = useState(isDarkMode); 
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    // 로그인 상태 확인
    if(user) {
      setIsLoggedIn(true);
    }else setIsLoggedIn(false);
  }, [user]);

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
      alert('로그아웃 되었습니다.');
      // 로그아웃 후 홈으로 새로고침
      window.location.href = '/';
    } catch (error) {
      console.error('로그아웃 실패:', error);
    }
  }

  const secessionHandler = async () => {
    if(!window.confirm('정말로 탈퇴하시겠습니까?')) return;
    try {
      await axios.delete(`${baseUrl}/smash/member/delete`, { withCredentials: true });
      setIsLoggedIn(false);
      alert('탈퇴가 완료되었습니다.');
      // 탈퇴 후 홈으로 새로고침
      window.location.href = '/';
    } catch (error) {
      console.error('탈퇴 실패:', error);
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
      (
        <>
          <button className="login-btn" onClick={logoutHandler}>로그아웃</button>
          <button className="secession-btn" onClick={secessionHandler}>탈퇴하기</button>
        </>
      )
      :
      (<Link to="/profile"><button className="login-btn">로그인</button></Link>)}
      
    </div>
  );
}