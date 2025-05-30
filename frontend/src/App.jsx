import { useState, useEffect } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import Layout from './components/Layout';
import Home from './pages/HomePage';
import Alarm from './pages/AlarmPage';
import Profile from './pages/ProfilePage';
import './App.css';

function App() {
  const [isDarkMode, setIsDarkMode] = useState(() => {
    const saved = localStorage.getItem('darkMode');
    return saved === null ? false : JSON.parse(saved);
  });
  // localStorage에 값이 없을 경우 false 저장
  useEffect(() => {
    const saved = localStorage.getItem('darkMode');
    if (saved === null) {
      localStorage.setItem('darkMode', JSON.stringify(false));
    }
    if(isDarkMode ) {
      document.body.classList.add('dark');
    }
    else {
      document.body.classList.remove('dark');
    }
  }, []);
  
  // routing
  const routes = [
    {
      element: <Layout />,
      children: [
        { path: '/', element: <Home isDarkMode={isDarkMode} setIsDarkMode={setIsDarkMode} /> },
        { path: '/alarm', element: <Alarm /> },
        { path: '/profile', element: <Profile /> },
      ],
    },
  ];

  const router = createBrowserRouter(routes);

  return (
      <RouterProvider router={router} />
  )
}

export default App
