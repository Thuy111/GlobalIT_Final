import { useState, useEffect } from 'react';
import axios from 'axios';
import Login from './member/LoginPage';
import UserProfile from './profile/UserProfile';
import PartnerProfile from './profile/PartnerProfile';

const Profile = ({ user }) => {
  const [profileData, setProfileData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const isPartner = profileData?.partner;

  const baseUrl = import.meta.env.VITE_API_URL;

  useEffect(() => {
    if (!user) {
      setLoading(false);
      return;
    }

    const fetchProfile = async () => {
      try {
        const res = await axios.get(`${baseUrl}/smash/profile`, {
          withCredentials: true,
        });
        setProfileData(res.data);
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

  console.log('📌 profileData:', profileData);
  console.log('📌 isPartner 값 확인:', profileData?.partner);


  return (
    <div className="profile">
      {isPartner ? (
        <PartnerProfile profile={profileData} />
      ) : (
        <UserProfile profile={profileData} />
      )}
    </div>
  );

}
export default Profile;