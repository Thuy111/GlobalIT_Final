import { useState } from 'react'
import { useDarkMode } from '../../contexts/DarkModeContext';

const Login = () => {
  const { isDarkMode } = useDarkMode();
  const baseUrl = import.meta.env.VITE_API_URL;

  return (
    <>
      <div className="sign_container">

        <div className="logo_img">
          <h1 className='hide'>Smash Logo</h1>
          {!isDarkMode && <img src="/images/logo.png" alt="Smash Logo" />}
          {isDarkMode && <img src="/images/logo2.png" alt="Smash Logo" />}
        </div>
        <section>
          <h2>로그인</h2>
          <a className="login-btn" href="/oauth2/authorization/google">Google로 로그인</a>
          <a className="login-btn" href="http://localhost:8080/oauth2/authorization/kakao">Kakao로 로그인</a>
        </section>
        <section>
          <h2>회원가입</h2>
          <a className="login-btn" href="http://localhost:8080/oauth2/authorization/google">Google로 회원가입</a>
          <a className="login-btn" href="http://localhost:8080/oauth2/authorization/kakao">Kakao로 회원가입</a>
        </section>
      </div>
    </>
  )
}

export default Login;