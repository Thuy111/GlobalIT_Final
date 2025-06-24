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
  const [phoneValid, setPhoneValid] = useState(true);

  const baseUrl = import.meta.env.VITE_API_URL;
  const navigate = useNavigate();

  useEffect(() => {
    axios.get(`${baseUrl}/smash/profile`, { withCredentials: true })
      .then(res => {
        const data = res.data;
        setProfile(data);
        setForm({
          nickname: data.nickname,
          tel: '',
          region: '',
          partnerName: data.partnerName || '',
          partnerTel: '',
          partnerRegion: '',
          description: '',
        });
        setIsPartner(data.partner);
        setPreviewUrl(data.profileImageUrl);
      });
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));

    if (name === 'nickname' && value !== profile.nickname) {
      axios.get(`${baseUrl}/smash/profile/check-nickname`, {
        params: { nickname: value }
      }).then(res => setNicknameValid(!res.data.duplicated));
    }

    if (name === 'tel' && value) {
      axios.get(`${baseUrl}/smash/profile/check-phone`, {
        params: { phone: value }
      }).then(res => setPhoneValid(res.data.valid));
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

  const handleAddressSearch = () => {
    new window.daum.Postcode({
      oncomplete: (data) => {
        const fullAddr = data.roadAddress + (data.buildingName ? ` (${data.buildingName})` : '');
        if (isPartner) {
          setForm(prev => ({ ...prev, partnerRegion: fullAddr }));
        } else {
          setForm(prev => ({ ...prev, region: fullAddr }));
        }
      }
    }).open();
  };

  const handleSubmit = async () => {
    const endpoint = isPartner ? 'partner' : 'member';
    const dto = isPartner ? {
      nickname: form.nickname,
      tel: form.tel,
      region: form.region,
      partnerName: form.partnerName,
      partnerTel: form.partnerTel,
      partnerRegion: form.partnerRegion,
      description: form.description
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
          {!phoneValid && <p style={{ color: 'red' }}>이미 사용 중인 번호입니다.</p>}

          <label>주소</label>
          <input name="region" value={form.region} readOnly onClick={handleAddressSearch} placeholder="주소 검색 클릭" />

          {isPartner && (
            <>
              <label>업체명</label>
              <input name="partnerName" value={form.partnerName} onChange={handleChange} />

              <label>업체 전화</label>
              <input name="partnerTel" value={form.partnerTel} onChange={handleChange} />

              <label>업체 주소</label>
              <input name="partnerRegion" value={form.partnerRegion} readOnly onClick={handleAddressSearch} placeholder="주소 검색 클릭" />

              <label>소개</label>
              <textarea name="description" value={form.description} onChange={handleChange} />
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