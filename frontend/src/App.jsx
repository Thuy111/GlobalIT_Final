import { useEffect } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { DarkModeProvider } from './contexts/DarkModeContext';
import Layout from './layouts/Basic';
import Home from './pages/HomePage';
import Alarm from './pages/AlarmPage';
import Profile from './pages/ProfilePage';
import StorePage from './pages/StorePage';
import UpdateProfile from './pages/profile/UpdateProfile';
import PartnerConvertPage from './pages/profile/PartnerConvertPage';
import Authenticated from './pages/member/AuthenticatedPage';
import Contact from './pages/ContactPage';
import './App.css';
import axios from 'axios';
import { baseUrl } from './config/apiClient';

function App() {
  useEffect (() => {
    const csrf = async() => {
      try{
        await axios.get(`${baseUrl}/api/csrf`, {headers: {"X-Frontend-Auth-Check": "true"}, withCredentials: true});
        // console.log('CSRF 토큰을 성공적으로 가져왔습니다.');
      }catch (error) {
        console.error('CSRF 토큰을 가져오는 중 오류 발생:', error);
      }
    }
    csrf();
  }, []);

  // routing
  const routes = [
    {
      element: <Layout />,
      children: [
        { path: '/', element: <Home /> },
        { path: '/alarm', element: <Alarm /> },
        { path: '/profile', element: <Profile /> },
        { path: '/contact', element: <Contact /> },
        { path: '/profile/update', element: <UpdateProfile /> },
        { path: '/profile/convert-to-partner', element: <PartnerConvertPage /> },
        { path: '/store/:code', element: <StorePage /> },
        { path: '/member/authenticated', element: <Authenticated /> },
      ],
    },
  ];

  const router = createBrowserRouter(routes);
  return (
    <DarkModeProvider>
      <RouterProvider router={router} />
    </DarkModeProvider>
  )
}

export default App
