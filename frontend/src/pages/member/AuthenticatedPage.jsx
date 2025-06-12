import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const Authenticated = () => {
  const baseUrl = import.meta.env.VITE_API_URL;
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [nickname, setNickname] = useState('');
  const [provider, setProvider] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    window.onload = () => {
      // 세션 정보를 백엔드에서 받아옴
      axios.get(`${baseUrl}/smash/member/auth/session-info`, { withCredentials: true })
        .then(res => {
          console.log('세션 정보:', res.data);
          setEmail(res.data.email);
          setProvider(res.data.provider);
          setNickname(res.data.nickname);
        })
        .catch(err => {
          console.error('세션 정보 요청 실패:', err);
          navigate('/profile'); // 세션 없으면 프로필(로그인)로 이동
        });
  
      // if(!email || !nickname || !provider) {
      //   alert('잘못된 접근입니다. 다시 로그인해주세요.');
      //   navigate('/profile'); // 세션 정보가 없으면 프로필(로그인)로 이동
      // }
    }
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post(`${baseUrl}/smash/member/auth/complete-social`, {
        email,
        nickname,
        provider,
        phone,
      }, { withCredentials: true });

      // 성공 후 프로필 또는 홈으로 이동
      alert('회원가입이 완료되었습니다. 소셜 로그인을 다시 시도해주세요.');
      navigate('/');
    } catch (error) {
      console.error('전화번호 제출 실패:', error);
    }
  };

  const changeHandlePhone = (value) => {
    console.log('전화번호 입력:', value);

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
  }

  return (
    <div>
      <h1>회원정보 입력</h1>
      <form onSubmit={handleSubmit}>
        <p>"{email}"님의 전화번호</p>
        <input
          type="text"
          placeholder="전화번호 입력 (-없이 숫자만)"
          value={phone}
          onChange={e => changeHandlePhone(e.target.value)}
        />
        <button type="submit">제출</button>
      </form>
    </div>
  );
};

export default Authenticated;
