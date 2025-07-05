import { useState, useEffect } from 'react';
// Link : 상태, 레이아웃 유지. JS로 경로만 바꿈(SPA), 빠름
import { Link, useLocation } from 'react-router-dom';
import { useUnreadAlarm } from '../contexts/UnreadAlarmContext';
import { useUser } from '../contexts/UserContext';

const Nav = () => {
  const user = useUser();

  // active 상태: 현재 활성화된 메뉴를 나타냄
  const [active, setActive] = useState('home');
  // 알림 개수 상태: UnreadAlarmContext에서 관리
  const { unreadCount } = useUnreadAlarm();
  // 현재 경로에 따라 active 상태 설정
  const location = useLocation();

  const baseUrl = import.meta.env.VITE_API_URL; // 백엔드 API URL
  
  useEffect(() => {
    // location.pathname: 현재 URL의 경로
    const pathname = location.pathname;
    if (pathname.includes('alarm')) {
      setActive('alarm');
    } else if (pathname.includes('profile') || pathname.includes('contact')) {
      setActive('profile');
    } else if (pathname.includes('chat') || pathname.includes('roomList')) {
      setActive('chat');
    } else {
      setActive('home');
    }
  }, [location]);

  return (
    <nav className="navigation">
      <ul className="nav-list">
        <li className={active === 'alarm' ? 'active' : ''}>
          { user &&
            <Link to="/alarm">
              <i className="fa-solid fa-bell"></i>
              알림
              {unreadCount > 0 && (<span className='unread_alarm'>{unreadCount}</span>)}
            </Link>
          }
          <Link to="/contact">
          <i className="fa-regular fa-circle-question"></i>문의</Link>
        </li>
        <li className={active === 'home' ? 'active' : ''}><Link to="/"><i className="fa-solid fa-house"></i>홈</Link></li>
        { user &&
          <li className={active === 'chat' ? 'active' : ''}><Link to={`${baseUrl}/smash/chat/roomList?memberUser=${user.emailId}`}><i className="fa-solid fa-comment"></i>채팅</Link></li>
        }
        <li className={active === 'profile' ? 'active' : ''}><Link to="/profile"><i className="fa-solid fa-user"></i>프로필</Link></li>
      </ul>
    </nav>
  );
}

export default Nav;