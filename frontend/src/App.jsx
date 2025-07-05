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

function App() {
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
