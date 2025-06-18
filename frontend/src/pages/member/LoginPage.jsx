import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { useDarkMode } from '../../contexts/DarkModeContext';
import '../../styles/Login.css';

// 백엔드에서 쿼리 파라미터를 가져오기 위한 훅
function useQuery() {
  return new URLSearchParams(useLocation().search);
}

const Login = () => {
  const { isDarkMode } = useDarkMode();
  const baseUrl = import.meta.env.VITE_API_URL;
  const query = useQuery();

  // 로그인 실패 시 쿼리 파라미터를 확인하고 알림 표시
  useEffect(() => {
    // 쿼리 파라미터에서 'error'가 있는지 확인
    const error = query.get('error');
    if(!error) return; // error가 없으면 아무것도 하지 않음
    if (error=="SignupFailed") {
      alert("회원가입에 실패했습니다. 다시 시도해주세요.");
    }else if(error.includes("AlreadyExists")) {
      const loginType = error.replace("AlreadyExists", "");
      alert(`이미 ${loginType}로 가입된 사용자입니다. ${loginType}로 로그인 합니다.`);
    }else if(error){
      console.error("로그인 중 오류 발생:", error);
    }
    window.location.href = '/profile'; // error 삭제
  }, []);
  

  return (
      <div className="sign_container">

        <div className="logo_img">
          <h1 className='hide'>Smash Logo</h1>
          {!isDarkMode && <img src="/images/logo.png" alt="Smash Logo" />}
          {isDarkMode && <img src="/images/logo2.png" alt="Smash Logo" />}
        </div>
        <section className='login_section'>
          <h2>소셜 로그인</h2>
          <div className="login-box">
            <a className="login-btn" href={`${baseUrl}/oauth2/authorization/google`}><i className="fa-brands fa-google"></i>Google로 로그인</a>
            <a className="login-btn" href={`${baseUrl}/oauth2/authorization/kakao`}><i className="fa-solid fa-comment"></i>Kakao로 로그인</a>
          </div>
        </section>
      </div>
  )
}

export default Login;