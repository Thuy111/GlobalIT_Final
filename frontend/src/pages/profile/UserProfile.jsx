import { useState, useEffect } from 'react';
import axios from 'axios';
import '../../styles/UserProfile.css';
import DefaultImage from '../../assets/images/default-profile.png';

const UserProfile = () => {
  const baseUrl = import.meta.env.VITE_API_URL;
  const [info, setInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await axios.get(`${baseUrl}/smash/profile`, {
          withCredentials: true,
        });
        setInfo(res.data);
        console.log('프로필 정보:', res.data);
      } catch (err) {
        setError('프로필을 불러오는 데 실패했습니다. 다시 시도해주세요.');
        console.error('프로필 불러오기 실패:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, []);

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div className="profile_container">
      <div className="profile_main_container">
        <h1>마이페이지</h1>
        <div className="profile_inform">
          <div className="profile_inform_img">
            <img
              src={info.profileImageUrl || DefaultImage}
              alt="프로필"
              className="profile_image"
            />
          </div>

          <div className="profile_inform_text">
            <p className="profile_inform_nickname">{info.nickname}</p>
            <p>{info.loginType}</p>
            <p>{info.isPartner ? '파트너' : '일반 회원'}</p>
          </div>
        </div>

        <section className="profile_account_setting">
          <h2>SMaSh 이용 내역</h2>
          <ul>
            <li>작성 글</li>
            <li>받은 견적서</li>
            <li>작성한 리뷰</li>
          </ul>
        </section>

        <section className="profile_account_setting">
          <h2>계정 설정</h2>
          <ul>
            <li>개인 정보 수정</li>
            <li>로그아웃</li>
            <li>계정 탈퇴</li>
          </ul>
        </section>
      </div>
    </div>
  );
};

export default UserProfile;