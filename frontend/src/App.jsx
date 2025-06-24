import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { DarkModeProvider } from './contexts/DarkModeContext';
import Layout from './layouts/Basic';
import Home from './pages/HomePage';
import Alarm from './pages/AlarmPage';
import Profile from './pages/ProfilePage';
import UpdateProfile from './pages/profile/UpdateProfile';
import Authenticated from './pages/member/AuthenticatedPage';

import './App.css';

function App() {
  // routing
  const routes = [
    {
      element: <Layout />,
      children: [
        { path: '/', element: <Home /> },
        { path: '/alarm', element: <Alarm /> },
        { path: '/profile', element: <Profile /> },
        { path: '/profile/update', element: <UpdateProfile /> },
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
