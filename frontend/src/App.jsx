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
    axios.get(`${baseUrl}/smash/member/check`, { withCredentials: true })
      .then(res => {
        setUser(res.data);
        setIsLoggedIn(true);
      })
      .catch(() => {
        setIsLoggedIn(false);
      });
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
