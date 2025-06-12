import { useState, useEffect } from 'react';
import Login from './member/LoginPage';
import Member from './profile/UserProfile';
import axios from 'axios';

const Profile = () => {
  const baseUrl = import.meta.env.VITE_API_URL;
  const [isLoggedIn, setIsLoggedIn] = useState(false);

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

  return (
    <div className="profile">
      {isLoggedIn ? <Member /> : <Login />}
    </div>
  );
}

export default Profile;