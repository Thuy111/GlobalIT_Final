import { useEffect, useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { UserContext } from '../contexts/UserContext';
import { UnreadAlarmProvider } from '../contexts/UnreadAlarmContext';
import Nav from '../components/Navigation.jsx';
import ScrollUp from '../components/ScrollUp.jsx';
import axios from 'axios';

const Layout = () => {
  const location = useLocation();
  const [loading, setLoading] = useState(true); // 로딩 상태
  const [user, setUser] = useState(null);
  const baseUrl = import.meta.env.VITE_API_URL; // 백엔드 API URL

  // 로그인 상태 확인
  useEffect(() => {
    // 검증 페이지 + 탈퇴에서는 return (나머지 페이지에서는 인증 필요 : 사용)
    const skipAuthPaths = ["/member/authenticated"];
    const isUnlinked = location.pathname === "/profile" && location.search === "?unlinked=true"; // => /profile?unlinked=true
    if (skipAuthPaths.includes(location.pathname) || isUnlinked) return;

    // 현재 로그인된 유저의 DB 정보
    const checkUser = async () => {
      await axios.get(`${baseUrl}/smash/member/current-user`, { withCredentials: true })
        .then(res => {
          setUser(res.data); // Member 객체
          regUser(); // 유저 유효성 검사 (번호등록, 가입여부 등)
        })
        .catch(err => {
          // console.warn("유저 인증 실패:", err);
        }
      );
    }

    // 현재 유저의 유효성 검사
    const regUser = async () => {
      await axios.get(`${baseUrl}/smash/member/check`, { withCredentials: true }) // headers를 보내지 않기 위해 appClient 사용하지 않음
        .then(res => {
          if(res) setLoading(true);
        })
        .catch((err) => {
          setLoading(false);
          const msg = err.response?.data; // 백엔드에서 body로 보낸 메시지
          // console.warn("유저 인증 실패:", msg); // 예: "번호가 등록되지 않은 계정입니다."
          if (msg) {
            alert(msg);
            if(msg.includes('번호')){
              // 번호가 등록되지 않은 계정이면 인증 페이지로 이동
              window.location = '/member/authenticated';
            }else if(msg.includes('가입')){
              // 로그인 정보가 없으면 로그인 페이지로 이동
              window.location = '/profile';
            }
          } else {
            alert("로그인 정보가 없습니다. 다시 로그인해주세요.");
          }
        });
    }

    checkUser();
  }, [location.pathname, location.search]);

  return (
    <UserContext.Provider value={user}>
      <UnreadAlarmProvider>
      {!loading===null && (<div className='loading'><i className="fa-solid fa-circle-notch"></i></div>)}
        <div className="main_container">
          <Outlet />
          <ScrollUp />
        </div>
        <Nav />
      </UnreadAlarmProvider>
    </UserContext.Provider>
  );
};

export default Layout;