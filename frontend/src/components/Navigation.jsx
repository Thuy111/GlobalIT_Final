import { useState, useEffect, use } from 'react';
// Link : 상태, 레이아웃 유지. JS로 경로만 바꿈(SPA), 빠름
import { Link, useLocation } from 'react-router-dom';
import { useUnreadAlarm } from '../contexts/UnreadAlarmContext';
import { useUser } from '../contexts/UserContext';
import apiClient from '../config/apiClient';

const Nav = () => {
  const user = useUser();

  const [hasChatUnread, setHasChatUnread] = useState(false); // 채팅 알림 상태
  // active 상태: 현재 활성화된 메뉴를 나타냄
  const [active, setActive] = useState('home');
  // 알림 개수 상태: UnreadAlarmContext에서 관리
  const { unreadCount } = useUnreadAlarm();
  // 현재 경로에 따라 active 상태 설정
  const location = useLocation();

  const baseUrl = import.meta.env.VITE_API_URL; // 백엔드 API URL

  useEffect(() => {
    const chatUnread = async () => {
      try {
        const response = await apiClient.get(`/chat/has-unread`, {});
        console.log('채팅 알림 조회:', response.data.hasUnread);
        setHasChatUnread(response.data.hasUnread);
      } catch (error) {
        console.error('채팅 알림 조회 실패:', error);
      }
    }
    if(user){
      chatUnread();
    }
  }, [location.pathname, location.search, user]);
  
  useEffect(() => {
    // location.pathname: 현재 URL의 경로
    const pathname = location.pathname;
    if (pathname.includes('alarm')) {
      setActive('alarm');
    } else if (pathname.includes('profile')) {
      setActive('profile');
    } else if (pathname.includes('chat') || pathname.includes('roomList')) {
      setActive('chat');
    } else if(pathname.includes('contact')){
      setActive('contact');
    } else {
      setActive('home');
    }
  }, [location]);

  return (
    <nav className="navigation">
      <ul className="nav-list">
        { user &&
          <li className={active === 'alarm' ? 'active' : ''}>
              <Link to="/alarm">
                <i className="fa-solid fa-bell"></i>
                알림
                {unreadCount > 0 && (<span className='unread_alarm'>{unreadCount}</span>)}
              </Link>
          </li>
        }
        <li className={active === 'contact' ? 'active' : ''}>
          <Link to="/contact">
            <i className="fa-regular fa-circle-question"></i>문의
          </Link>
        </li>
        <li className={active === 'home' ? 'active' : ''}><Link to="/"><i className="fa-solid fa-house"></i>홈</Link></li>
        { user &&
          <li className={active === 'chat' ? 'active' : ''}>
            <Link to={`${baseUrl}/smash/chat/roomList?user=${user.emailId}`}>
              {hasChatUnread && <div className='new_msg_point'></div>}
              <i className="fa-solid fa-comment"></i>채팅
            </Link>
          </li>
        }
        <li className={active === 'profile' ? 'active' : ''}><Link to="/profile"><i className="fa-solid fa-user"></i>프로필</Link></li>
      </ul>
    </nav>
  );
}

export default Nav;