import axios from 'axios';
import DefaultImage from '../../assets/images/default-profile.png';
import '../../styles/UserProfile.css';

const PartnerProfile = ({ profile, setIsLoggedIn }) => {
  const baseUrl = import.meta.env.VITE_API_URL;

  const logoutHandler = async () => {
    try {
      await fetch(`${import.meta.env.VITE_API_URL}/logout`, {
        method: 'POST',
        credentials: 'include',
      });
      setIsLoggedIn(false);
      alert('로그아웃 되었습니다.');
      window.location.href = '/';
    } catch (err) {
      console.error('로그아웃 실패:', err);
    }
  };

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
    <div className="profile_container">
      <div className="profile_main_container">
        <h1>마이페이지</h1>
        <div className="profile_inform">
          <div className="profile_inform_img">
            <img
              src={profile.profileImageUrl || DefaultImage}
              alt="프로필"
              className="profile_image"
            />
          </div>

          <div className="profile_inform_text">
            <p className="profile_inform_nickname">{profile.nickname}</p>
            <p>{profile.loginType}</p>
            <p>{profile.partner ? '파트너' : '일반 회원'}</p>
          </div>
        </div>

        <div className="profile_partner_inform">
          <h2>업체명</h2>
          <input type="text" 
                 value={profile.partnerName || '-'} 
                 readOnly />

          <h2>사업자 번호</h2>
          <input type="text" 
                 value={profile.bno || '-'} 
                 readOnly />
        </div>

        <section className="profile_account_setting">
          <h2>SMaSh 이용 내역</h2>
          <ul>
            <li>회사 소개</li>
            <li>보낸 견적서</li>
            <li>작성된 리뷰</li>
          </ul>
        </section>

        <section className="profile_account_setting">
          <h2>계정 설정</h2>
          <ul>
            <li>개인 정보 수정</li>
            <li onClick={logoutHandler}>로그아웃</li>
            <li onClick={secessionHandler}>계정 탈퇴</li>
          </ul>
        </section>
        
        <div className="profile_blank"></div>
      </div>
    </div>
  );
};

export default PartnerProfile;
