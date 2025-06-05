import { useState } from 'react'
import { useDarkMode } from '../../contexts/DarkModeContext';

const Login = () => {
  const { isDarkMode } = useDarkMode();

  return (
    <>
      {!isDarkMode && <img src="/images/logo.png" alt="Smash Logo" />}
      {isDarkMode && <img src="/images/logo2.png" alt="Smash Logo" />}
      <section>
        <button className="login-btn">로그인</button>
      </section>
    </>
  )
}

export default Login;