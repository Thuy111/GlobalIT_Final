import { Outlet } from 'react-router-dom';
import Nav from './Navigation.jsx';

const Layout = () => {
  return (
    <>
      <div className="container">
        <Outlet />
      </div>
      <Nav />
    </>
  );
};

export default Layout;