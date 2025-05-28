import { Outlet } from 'react-router-dom';
import Nav from './Navigation.jsx';
import TopBar from './TopBar.jsx';

const Layout = () => {
  return (
    <>
      <TopBar />
      <div className="container">
        <Outlet />
      </div>
      <Nav />
    </>
  );
};

export default Layout;