import { useState, useEffect } from 'react';
import axios from 'axios';
import Login from './member/LoginPage';
import UserProfile from './profile/UserProfile';
import PartnerProfile from './profile/PartnerProfile';

const Profile = ({ user }) => {
  const [profileData, setProfileData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false); // 로그인 상태 관리

  // ✔️ profileData가 있을 때만 isPartner 체크
  const isPartner = profileData?.partner ?? false;

  const baseUrl = import.meta.env.VITE_API_URL;

  

  useEffect(() => {
    if (!user) {
      setLoading(false);
      setIsLoggedIn(false);
      return;
    }

    const fetchProfile = async () => {
      try {
        const res = await axios.get(`${baseUrl}/smash/profile`, {
          withCredentials: true,
        });
        setProfileData(res.data);
        setIsLoggedIn(true);
      } catch (err) {
        setError('프로필 정보를 불러오지 못했습니다.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [user]);

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div>{error}</div>;

  if (!user) return <Login />;

  // ✔️ profileData가 있을 때만 로그 출력
  if (profileData) {
    console.log('📌 profileData:', profileData);
    console.log('📌 isPartner 값 확인:', profileData.partner);
  }

  return (
    <div className="profile">
      {isPartner ? (
        <PartnerProfile profile={profileData} setIsLoggedIn={setIsLoggedIn} />
      ) : (
        <UserProfile profile={profileData} setIsLoggedIn={setIsLoggedIn} />
      )}
    </div>
  );

}
export default Profile;