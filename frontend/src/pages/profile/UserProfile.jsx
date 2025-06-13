import { useState, useEffect } from 'react';
import axios from 'axios';

const UserProfile = () => {
  const baseUrl = import.meta.env.VITE_API_URL;
  const [info, setInfo] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await axios.get(`${baseUrl}/smash/profile`, {
          withCredentials: true
        });
        setInfo(res.data);
        console.log('프로필 정보:', res.data);
      } catch (err) {
        console.error('프로필 불러오기 실패:', err);
      }
    };

    fetchProfile();
  }, []);

  if (!info) return <div>로딩 중...</div>;

  return (
    <div className="member-page">
      <div className="member_main_container">
        <h1>마이페이지</h1>
        <div className="member_inform">
          <div className="inform_img">
            {info.profileImageUrl ? (
              <img src={info.profileImageUrl} alt="프로필" />
            ) : (
              <div>기본 이미지</div>
            )}
          </div>
          <div className="inform_text">
            <p>닉네임: {info.nickname}</p>
            <p>로그인 방식: {info.loginType}</p>
            <p>파트너 여부: {info.isPartner ? '파트너' : '일반 회원'}</p>
          </div>
        </div>

        <section className="account_setting">
          <h2>SMaSh 이용 내역</h2>
          <ul>
            <li>작성 글</li>
            <li>받은 견적서</li>
            <li>작성한 리뷰</li>
          </ul>
        </section>

        <section className="account_setting">
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