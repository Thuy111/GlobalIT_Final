import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import Layout from './components/Layout';
import Home from './pages/HomePage';
import Alarm from './pages/AlarmPage';
import Profile from './pages/ProfilePage';
import './App.css';

// routing
const routes = [
  {
    element: <Layout />,
    children: [
      { path: '/', element: <Home /> },
      { path: '/alarm', element: <Alarm /> },
      { path: '/profile', element: <Profile /> },
    ],
  },
];


function App() {
  const router = createBrowserRouter(routes);

  return (
    <>
      <RouterProvider router={router} />
    </>
  )
}

export default App
