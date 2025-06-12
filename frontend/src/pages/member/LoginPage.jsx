import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { useDarkMode } from '../../contexts/DarkModeContext';

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
    if (error=="SignupFailed") {
      alert("회원가입에 실패했습니다. 다시 시도해주세요.");
    }else{
      console.error("로그인 중 오류 발생:", error);
    }
  }, []);
  

  return (
      <div className="sign_container">

        <div className="logo_img">
          <h1 className='hide'>Smash Logo</h1>
          {!isDarkMode && <img src="/images/logo.png" alt="Smash Logo" />}
          {isDarkMode && <img src="/images/logo2.png" alt="Smash Logo" />}
        </div>
        <section>
          <h2>로그인</h2>
          <a className="login-btn" href={`${baseUrl}/oauth2/authorization/google`}>Google로 로그인</a>
          <a className="login-btn" href={`${baseUrl}/oauth2/authorization/kakao`}>Kakao로 로그인</a>
        </section>
        {/* <section>
          <h2>회원가입</h2>
          <a className="login-btn" href={`${baseUrl}/oauth2/authorization/google`}>Google로 회원가입</a>
          <a className="login-btn" href={`${baseUrl}/oauth2/authorization/kakao`}>Kakao로 회원가입</a>
        </section> */}
      </div>
  )
}

export default Login;