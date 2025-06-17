import { useState, useEffect } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { DarkModeProvider } from './contexts/DarkModeContext';
import Layout from './layouts/Basic';
import Home from './pages/HomePage';
import Alarm from './pages/AlarmPage';
import Profile from './pages/ProfilePage';
import Authenticated from './pages/member/AuthenticatedPage';
import axios from 'axios';
import './App.css';

function App() {
  const baseUrl = import.meta.env.VITE_API_URL;
  const [isLoggedIn, setIsLoggedIn] = useState(null); // null: 로딩 중
  const [user, setUser] = useState(null);
  

  // routing
  const routes = [
    {
      element: <Layout />,
      children: [
        { path: '/', element: <Home user={user} /> },
        { path: '/alarm', element: <Alarm /> },
        { path: '/profile', element: <Profile user={user} /> },
        { path: '/member/authenticated', element: <Authenticated /> },
      ],
    },
  ];

  // 로그인 상태 확인
  useEffect(() => {
    // 검증 페이지에서는 유저 체크를 하지 않음
    if (location.pathname === "/member/authenticated") return;
    // 유저 정보가 있을 때만 요청
    axios.get(`${baseUrl}/smash/member/current-user`, { withCredentials: true })
      .then(res => {
        setUser(res.data);
        regUser(); // 로그인 상태 확인 함수 호출 (DB, 전화번호 존재 여부 확인)
      })
      .catch(err => {
        // console.warn("유저 인증 실패:", err);
      }
    );
    

    const regUser = async () => {
      // 백엔드에서 로그인 상태 확인
      if (isLoggedIn !== null) return; // 이미 상태가 설정되어 있으면 중복 요청 방지
      await axios.get(`${baseUrl}/smash/member/check`, { withCredentials: true })
        .then(res => {
          setUser(res.data);
          setIsLoggedIn(true);
        })
        .catch((err) => {
          setIsLoggedIn(false);
          const msg = err.response?.data; // 백엔드에서 body로 보낸 메시지
          // console.warn("유저 인증 실패:", msg); // 예: "번호가 등록되지 않은 계정입니다."
          if (msg) {
            alert(msg);
            if(msg.includes('번호')){
              // 번호가 등록되지 않은 계정이면 인증 페이지로 이동
              window.location = '/member/authenticated';
            }else if(msg.includes('가입')){
              // 로그인 정보가 없으면 로그인 페이지로 이동
              window.location = '/profile';
            }
          } else {
            alert("로그인 정보가 없습니다. 다시 로그인해주세요.");
          }
        });
    }
  }, []);
  

  const router = createBrowserRouter(routes);
  return (
    <DarkModeProvider>
      {!isLoggedIn===null && (<div className='loading'><i className="fa-solid fa-circle-notch"></i></div>)}
      <RouterProvider router={router} />
    </DarkModeProvider>
  )
}

export default App
