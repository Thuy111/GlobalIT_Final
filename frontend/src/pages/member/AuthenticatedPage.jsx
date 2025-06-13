import { useState, useEffect } from 'react';
import { useDarkMode } from '../../contexts/DarkModeContext';
import axios from 'axios';

const Authenticated = ({}) => {
  const baseUrl = import.meta.env.VITE_API_URL;
  const { isDarkMode } = useDarkMode();
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [nickname, setNickname] = useState('');
  const [provider, setProvider] = useState('');
  const [isDisable, setIsDisable] = useState(true);
  const [isExistUser, setIsExistUser] = useState(false); // 이미 등록된 사용자 여부

  useEffect(() => {
    window.onload = () => {
      const currentUser = async () => { // 현재 로그인된 사용자의 정보를 가져옴
        console.log('현재 사용자 정보 요청 중...');
        try {
          const res = await axios.get(`${baseUrl}/smash/member/currnet-user`, { withCredentials: true });
          if (res.data) {
            console.log('현재 사용자 정보:', res.data);
            setEmail(res.data.emailId);
          }
        } catch (err) {
          console.error('현재 사용자 정보 요청 실패:', err);
          alert('잘못된 접근입니다. 다시 로그인해주세요.');
          window.location = '/profile'; // 세션 없으면 프로필(로그인)로 이동
        }
      }

      const getSessionInfo = async () => {
        // 세션 정보를 백엔드에서 받아옴
        try{
            const res = await axios.get(`${baseUrl}/smash/member/auth/session-info`, { withCredentials: true })
            // console.log('세션 정보:', res.data);
            setEmail(res.data.email);
            setProvider(res.data.provider);
            setNickname(res.data.nickname);
          }
          catch(err) {
            // 받아올 세션정보가 없는 경우(기존 회원인 경우)
            setIsExistUser(true);
            currentUser();
          };
      }
      getSessionInfo();
    }
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (isExistUser){ // 기존 사용자 정보 제출
      try{
        const res = await axios.post(`${baseUrl}/smash/member/auth/register-phone`, {
          email,
          phone,
        }, { withCredentials: true });
        // 성공 후 프로필 또는 홈으로 이동
        alert(res.data);
        window.location = '/profile'; // 프로필 페이지로 이동
      }catch(err) {
        // 에러 메시지
        if (err.response) {
          alert(err.response.data); // 가입되지 않은 계정입니다, 로그인 정보가 없습니다.
        } else {
          alert('번호인증 실패 : 서버와의 연결에 실패했습니다.');
        }
      }
    }else{ // 신규 사용자 정보 제출
      try {
        const res = await axios.post(`${baseUrl}/smash/member/auth/complete-social`, {
          email,
          nickname,
          provider,
          phone,
        }, { withCredentials: true });
  
        // 성공 후 프로필 또는 홈으로 이동
        alert(res.data);
        window.location = '/';
      } catch (err) {
        // 에러 메시지
        if (err.response) {
          alert(err.response.data); // 이미 등록된 사용자, 정보 누락
          if(err.response.data.includes('가입된')) {
            // 이미 등록된 사용자면 프로필로 이동
            window.location = '/profile';
          }
        } else {
          alert('회원가입 실패 : 서버와의 연결에 실패했습니다.');
        }
      }
    }
  };

  const changeHandlePhone = (value) => {
    // 번호만 입력하도록 필터링
    const regex = /^[0-9]*$/; // 숫자만 허용
    if(value && !regex.test(value)) {
      setPhone(phone); // 숫자가 아닌 경우 이전 값으로 되돌림
    }else if(value.length > 11) {
      setPhone(phone); // 최대 11자리로 제한
    } else if(value.length === 0) {
      setPhone(''); // 빈 문자열로 초기화
    } else {
      // 전화번호가 숫자만 포함된 경우 업데이트
      setPhone(value);
    }

    // 한국 전화번호 정규식
    // const phoneRegex = /^(01[0-9]{8,9})$/; // 010, 011, 016, 017, 018, 019로 시작하는 11자리 전화번호

    if (value.length >= 9) {
      setIsDisable(false); // 전화번호가 9자리 이상이면 제출 버튼 활성화 (예: 01012345678 또는 021234567)
    }else setIsDisable(true); // 그렇지 않으면 비활성화
  }

  return (
    <div className='authenticated_container'>
      <div className="logo_img">
          <h1 className='hide'>Smash Logo</h1>
          {!isDarkMode && <img src="/images/logo.png" alt="Smash Logo" />}
          {isDarkMode && <img src="/images/logo2.png" alt="Smash Logo" />}
        </div>
      <h2>회원정보 입력</h2>
      <form className='authenticated_form' onSubmit={handleSubmit}>
        <p className="auth_title"><strong>{email}</strong> 님의 전화번호</p>
        <div className="flex_center">
          <input
            type="text"
            placeholder="전화번호 입력 (-없이 숫자만)"
            value={phone}
            onChange={e => changeHandlePhone(e.target.value)}
          />
          <button type="submit" disabled={isDisable}>제출</button>
        </div>
      </form>
    </div>
  );
};

export default Authenticated;
