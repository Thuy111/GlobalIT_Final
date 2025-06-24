import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import DefaultImage from '../../assets/images/default-profile.png';

const UpdateProfile = () => {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({});
  const [isPartner, setIsPartner] = useState(false);
  const [imageFile, setImageFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [nicknameValid, setNicknameValid] = useState(true);
  const [telValid, setTelValid] = useState(true);
  const [partnerTelValid, setPartnerTelValid] = useState(true);

  // 원래 값 저장용
  const [originalTel, setOriginalTel] = useState('');
  const [originalNickname, setOriginalNickname] = useState('');

  const baseUrl = import.meta.env.VITE_API_URL;
  const navigate = useNavigate();

  // 휴대폰 번호 (010으로 시작) 하이픈 허용
  const mobilePhoneRegex = /^010-?\d{3,4}-?\d{4}$/;

  // 업체 전화번호 (02, 031, 070 등 다양하게) 하이픈 허용
  const businessPhoneRegex = /^0\d{1,2}-?\d{3,4}-?\d{4}$/;

  useEffect(() => {
    axios.get(`${baseUrl}/smash/profile`, { withCredentials: true })
      .then(res => {
        const data = res.data;
        setProfile(data);
        setForm({
          nickname: data.nickname,
          tel: data.tel,
          region: data.region,
          partnerName: data.partnerName || '',
          partnerTel: data.partnerTel,
          partnerRegion: data.partnerRegion,
        });
        setIsPartner(data.partner);
        setPreviewUrl(data.profileImageUrl ? `${baseUrl}${data.profileImageUrl}` : null);
        setOriginalTel(data.tel || '');
        setOriginalNickname(data.nickname || '');
      });
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));

    if (name === 'nickname' && value !== originalNickname) {
      axios.get(`${baseUrl}/smash/profile/check-nickname`, {
        params: { nickname: value }
      }).then(res => setNicknameValid(!res.data.duplicated));
    } else if (name === 'nickname') {
      // 변경 안 하면 무조건 유효하다고 처리
      setNicknameValid(true);
    }

    if (name === 'tel') {
      if (value === originalTel) {
        setTelValid(true);  // 변경 없으면 무조건 유효
        return;
      }
      const isValidMobile = mobilePhoneRegex.test(value);
      if (!isValidMobile) {
        setTelValid(false);
        return;
      }
      axios.get(`${baseUrl}/smash/profile/check-phone`, {
        params: { phone: value }
      }).then(res => setTelValid(res.data.valid));
    }

    if (name === 'partnerTel' && value) {
      const isValidBusiness = businessPhoneRegex.test(value);
      setPartnerTelValid(isValidBusiness);
      // 업체 번호 중복검사 필요하면 여기서 호출
    }
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setImageFile(file);
    setPreviewUrl(URL.createObjectURL(file));
  };

  const handleImageDelete = () => {
    axios.delete(`${baseUrl}/smash/profile/image`, { withCredentials: true })
      .then(() => {
        setImageFile(null);
        setPreviewUrl(null);
      });
  };

  const handleAddressSearch = (field) => {
    new window.daum.Postcode({
      oncomplete: (data) => {
        const fullAddr = data.roadAddress + (data.buildingName ? ` (${data.buildingName})` : '');
        setForm(prev => ({ ...prev, [field]: fullAddr }));
      }
    }).open();
  };

  const handleSubmit = async () => {
    // 닉네임 변경했으면 유효성 체크
    if (form.nickname !== originalNickname && !nicknameValid) {
      alert('중복된 닉네임입니다.');
      return;
    }
    // 휴대폰 번호 변경했으면 유효성 체크
    if (form.tel !== originalTel && !telValid) {
      alert('휴대폰 번호가 유효하지 않거나 이미 사용 중입니다.');
      return;
    }
    // 업체 전화번호는 무조건 체크
    if (isPartner && form.partnerTel && !partnerTelValid) {
      alert('업체 전화번호 형식이 올바르지 않습니다.');
      return;
    }

    const endpoint = isPartner ? 'partner' : 'member';
    const dto = isPartner ? {
      nickname: form.nickname,
      tel: form.tel,
      region: form.region,
      partnerName: form.partnerName,
      partnerTel: form.partnerTel,
      partnerRegion: form.partnerRegion,
    } : {
      nickname: form.nickname,
      tel: form.tel,
      region: form.region
    };

    await axios.put(`${baseUrl}/smash/profile/${endpoint}`, dto, {
      withCredentials: true
    });

    if (imageFile) {
      const fd = new FormData();
      fd.append("file", imageFile);
      await axios.post(`${baseUrl}/smash/profile/image`, fd, {
        withCredentials: true,
        headers: { 'Content-Type': 'multipart/form-data' }
      });
    }

    alert('수정이 완료되었습니다.');
    navigate('/profile');
  };

  if (!profile) return <div>로딩 중...</div>;

  return (
    <div className="profile_container">
      <div className="profile_main_container">
        <h1>프로필 수정</h1>

        <div className="profile_inform">
          <div className="profile_inform_img">
            <img src={previewUrl || DefaultImage} alt="프로필" className="profile_image" />
            <input type="file" accept="image/*" onChange={handleImageChange} />
            <button onClick={handleImageDelete}>이미지 삭제</button>
          </div>

          <div className="profile_inform_text">
            <p><strong>이메일</strong>: {profile.email}</p>
            <p><strong>로그인 타입</strong>: {profile.loginType}</p>
          </div>
        </div>

        <div className="profile_form">
          <label>닉네임</label>
          <input name="nickname" value={form.nickname} onChange={handleChange} />
          {!nicknameValid && <p style={{ color: 'red' }}>중복된 닉네임입니다.</p>}

          <label>전화번호</label>
          <input name="tel" value={form.tel} onChange={handleChange} />
          {!telValid && <p style={{ color: 'red' }}>휴대폰 번호가 유효하지 않거나 이미 사용 중입니다.</p>}

          <label>주소</label>
          <input name="region" 
          value={form.region} 
          readOnly 
          onClick={() => handleAddressSearch('region')} 
          placeholder="주소 검색 클릭" />


          {isPartner && (
            <>
              <label>업체명</label>
              <input name="partnerName" value={form.partnerName} onChange={handleChange} />

              <label>업체 전화</label>
              <input name="partnerTel" value={form.partnerTel} onChange={handleChange} />
              {!partnerTelValid && <p style={{ color: 'red' }}>업체 전화번호 형식이 올바르지 않습니다.</p>}

                  <label>업체 주소</label>
                  <input name="partnerRegion" 
                  value={form.partnerRegion} 
                  readOnly 
                  onClick={() => handleAddressSearch('partnerRegion')} 
                  placeholder="주소 검색 클릭" />
            </>
          )}

          <div className="form_buttons">
            <button onClick={handleSubmit}>저장</button>
            <button onClick={() => navigate('/profile')}>뒤로가기</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UpdateProfile;