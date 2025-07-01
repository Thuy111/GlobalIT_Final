import { useState, useEffect } from 'react';
import apiClient from '../config/apiClient';
import Login from './member/LoginPage';
import UserProfile from './profile/UserProfile';
import PartnerProfile from './profile/PartnerProfile';
import { useUser } from '../contexts/UserContext';

const Profile = () => {
  const [profileData, setProfileData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false); // 로그인 상태 관리
  const [isChecked, setIsChecked] = useState(false); // 토글 상태

  const user = useUser();

  // ✔️ profileData가 있을 때만 isPartner 체크
  const isPartner = profileData?.partner ?? false;


  useEffect(() => {
    if (!user) {
      setLoading(false);
      setIsLoggedIn(false);
      return;
    }

    const fetchProfile = async () => {
      try {
        const res = await apiClient.get(`/profile`, {
          withCredentials: true,
        });
         console.log('프로필 응답:', res.data); // 👈 여기 추가
        setProfileData(res.data);
        setIsChecked(res.data?.partner ?? false);
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

  const handleToggleChange = async () => {
    const newChecked = !isChecked;
    setIsChecked(newChecked);

    try {
      if (newChecked) {
        const bno = profileData?.bno;
        if (bno) {
          await apiClient.post(`/partner/update`, {}, { withCredentials: true });
          alert('파트너 회원으로 전환되었습니다.');
          window.location.href = '/profile';
        } else {
          window.location.href = '/profile/convert-to-partner';
        }
      } else {
        await apiClient.post(`/partner/revert`, {}, { withCredentials: true });
        alert('일반 회원으로 전환되었습니다.');
        window.location.href = '/profile';
      }
    } catch (error) {
      console.error('회원 전환 실패:', error);
      alert('회원 전환 중 오류 발생');
    }
  };

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div>{error}</div>;

  if (!user) return <Login />;


  return (
    <div className="profile">
      {isPartner ? (
        <PartnerProfile
          profile={profileData}
          setIsLoggedIn={setIsLoggedIn}
          isChecked={isChecked}
          onToggleChange={handleToggleChange}
        />
      ) : (
        <UserProfile
          profile={profileData}
          setIsLoggedIn={setIsLoggedIn}
          isChecked={isChecked}
          onToggleChange={handleToggleChange}
        />
      )}
    </div>
  );

}
export default Profile;