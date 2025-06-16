import DefaultImage from '../../assets/images/default-profile.png';
import '../../styles/UserProfile.css';

const PartnerProfile = ({ profile }) => {
  const logoutHandler = async () => {
    try {
      await fetch(`${import.meta.env.VITE_API_URL}/logout`, {
        method: 'POST',
        credentials: 'include',
      });
      alert('로그아웃 되었습니다.');
      window.location.href = '/';
    } catch (err) {
      console.error('로그아웃 실패:', err);
    }
  };

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

        <div className="partner_infom">
          <h2>업체명</h2>
          <p>{profile.companyName || '-'}</p>
          <h2>사업자 번호</h2>
          <p>{profile.businessNumber || '-'}</p>
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
            <li>계정 탈퇴</li>
          </ul>
        </section>
      </div>
    </div>
  );
};

export default PartnerProfile;
