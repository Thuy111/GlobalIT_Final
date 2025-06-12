import { useState, useEffect } from 'react';
import Login from './member/LoginPage';
import Member from './profile/UserProfile';
import axios from 'axios';

const Profile = ({ user }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    // 로그인 상태 확인
    if(user) {
      setIsLoggedIn(true);
    }else setIsLoggedIn(false);
  }, [user]);

  return (
    <div className="profile">
      {isLoggedIn ? <Member /> : <Login />}
    </div>
  );
}

export default Profile;