import { useNavigate } from 'react-router-dom';
import TitleBar from '../../components/TitleBar';
import apiClient, { baseUrl } from '../../config/apiClient';
import DefaultImage from '../../assets/images/default-profile.png';
import '../../styles/UserProfile.css';

const UserProfile = ({ profile, setIsLoggedIn, isChecked, onToggleChange  }) => {
  const navigate = useNavigate();

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
      alert('로그아웃 중 오류 발생');
    }
  };

  const secessionHandler = async () => {
    if (!window.confirm('정말로 탈퇴하시겠습니까?')) return;
    try {
      await apiClient.delete(`/member/delete`, { withCredentials: true });
      setIsLoggedIn(false);
      alert('탈퇴가 완료되었습니다.');
      // 탈퇴 후 홈으로 새로고침
      window.location.href = '/';
    } catch (error) {
      console.error('탈퇴 실패:', error);
      alert('탈퇴 중 오류 발생');
    }
  };

  const handleSocialLogout = async () => {
    console.log('소셜 로그아웃 시도');
    try {
      await apiClient.post('/member/auth/unlink', {}, { withCredentials: true })
      setIsLoggedIn(false);
      window.location.href = '/profile';
    }catch (error) {
      console.error('소셜 로그아웃 실패:', error);
    }
  }


  if (!profile) return <div className='loading'><i className="fa-solid fa-circle-notch"></i></div>;

  const imageUrl = profile?.profileImageUrl ? `${baseUrl}${profile.profileImageUrl}` : DefaultImage;

  return (
    <div className="profile_container">
      <div className="profile_main_container">
        <TitleBar title="마이페이지(일반)" />
         {/* ✅ 역할 전환 토글 */}
        <div className="profile_toggle_container">
          <div className="change_role_toggle">
            <h2 className="toggle_text">{isChecked ? '일반 회원으로 전환하기' : '파트너 회원으로 전환하기'}</h2>
            <input
            type="checkbox"
            className="toggle_input"
            id="roleToggle"
            onChange={onToggleChange}
            checked={isChecked}
            />
            <label className="toggle_label" htmlFor="roleToggle"></label>
          </div>

         </div>
        

        <div className="profile_inform">
          <div className="profile_inform_img">
            <img src={imageUrl} alt="프로필" className="profile_image" />
          </div>

          <div className="profile_inform_text">
            <p className="profile_inform_nickname">{profile.nickname}</p>
            <p>{profile.loginType}</p>
            <p>{profile.partner ? '파트너' : '일반 회원'}</p>
            <div onClick={handleSocialLogout} className='another_social'>다른 계정으로 로그인</div>
          </div>
        </div>

        <section className="profile_account_setting">
          <h2>SMaSh 이용 내역</h2>
          <ul>
            <li onClick={() => window.location.href = `${baseUrl}/smash/request/mylist`}>
              작성 글
            </li>
            <li onClick={() => window.location.href = `${baseUrl}/smash/estimate/mylist`}>
              받은 견적서
            </li>
            <li onClick={() => window.location.href = `${baseUrl}/smash/review/mylist`}>
              작성한 리뷰
            </li>
          </ul>
        </section>

        <section className="profile_account_setting">
          <h2>계정 설정</h2>
          <ul>
            <li onClick={() => navigate('/profile/update')}>개인 정보 수정</li>
            <li onClick={logoutHandler}>로그아웃</li>
            <li onClick={secessionHandler}>계정 탈퇴</li>
          </ul>
        </section>

        <div className="profile_blank"></div>
      </div>
    </div>
  );
};

export default UserProfile;