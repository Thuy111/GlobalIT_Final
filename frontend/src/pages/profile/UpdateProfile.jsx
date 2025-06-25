import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import DefaultImage from '../../assets/images/default-profile.png';
import '../../styles/UpdateProfile.css'

const UpdateProfile = () => {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({});
  const [isPartner, setIsPartner] = useState(false);
  const [imageFile, setImageFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [validations, setValidations] = useState({
    nickname: true,
    tel: true,
    partnerTel: true,
  });
  const [originalValues, setOriginalValues] = useState({
    tel: '',
    nickname: '',
  });
  const [nicknameChecked, setNicknameChecked] = useState(false);
  
  const baseUrl = import.meta.env.VITE_API_URL;
  const navigate = useNavigate();

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
        setOriginalValues({
          tel: data.tel || '',
          nickname: data.nickname || '',
        });
      });
  }, []);

  const validatePhone = (phone, type = 'mobile') => {
    const regex = type === 'mobile' 
      ? /^010-?\d{3,4}-?\d{4}$/ 
      : /^0\d{1,2}-?\d{3,4}-?\d{4}$/;
    return regex.test(phone);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));

    // 닉네임이 변경되었을 때만 중복확인 버튼 활성화
    if (name === 'nickname' && value !== originalValues.nickname) {
      setNicknameChecked(false);
    }

    // 전화번호 유효성 체크
    if (name === 'tel' || name === 'partnerTel') {
      const valid = validatePhone(value, name === 'partnerTel' ? 'business' : 'mobile');
      setValidations(prev => ({
        ...prev,
        [name]: valid,
      }));
    }
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setImageFile(file);
      setPreviewUrl(URL.createObjectURL(file));
    }
  };

  const handleImageDelete = () => {
    axios.delete(`${baseUrl}/smash/profile/image`, { withCredentials: true })
      .then(() => {
        setImageFile(null);
        setPreviewUrl(null);

        // ✅ 파일 input 값 초기화
        const fileInput = document.querySelector('input[type="file"]');
        if (fileInput) {
          fileInput.value = '';
        }
      });
  };

  const handleNicknameCheck = () => {
    const nickname = form.nickname;
    if (!nickname) return;

    axios.get(`${baseUrl}/smash/profile/check-nickname`, {
      params: { nickname }
    }).then(res => {
        if (res.data.duplicated) {
          // 중복된 닉네임인 경우
          alert('중복된 닉네임입니다.');
          setValidations(prev => ({
            ...prev,
            nickname: false,  // 유효성 상태를 false로 설정 (중복된 닉네임)
          }));
          setNicknameChecked(false);  // 중복 확인이 완료된 상태는 false로 설정
        } else {
          // 사용 가능한 닉네임인 경우
          alert('사용할 수 있는 닉네임입니다.');
          setValidations(prev => ({
            ...prev,
            nickname: true,  // 유효성 상태를 true로 설정 (사용 가능한 닉네임)
          }));
          setNicknameChecked(true);  // 중복 확인이 완료된 상태는 true로 설정
        }
      }).catch(error => {
        console.error("닉네임 중복 확인 실패:", error);
        alert('닉네임 중복 확인 중 오류가 발생했습니다.');
      });
  };

  // 우편번호 API 처리
  const handleAddressSearch = (field) => {
    new window.daum.Postcode({
      oncomplete: (data) => {
        const fullAddr = data.roadAddress + (data.buildingName ? ` (${data.buildingName})` : '');
        setForm(prev => ({ ...prev, [field]: fullAddr }));
      }
    }).open();
  };

  const handleSubmit = async () => {
    if (form.nickname !== originalValues.nickname && !nicknameChecked) {
      alert('닉네임 중복 확인을 해주세요.');
      return;
    }
    if (form.nickname !== originalValues.nickname && !validations.nickname) {
      alert('중복된 닉네임입니다.');
      return;
    }
    if (form.tel !== originalValues.tel && !validations.tel) {
      alert('휴대폰 번호가 유효하지 않거나 이미 사용 중입니다.');
      return;
    }
    if (isPartner && form.partnerTel && !validations.partnerTel) {
      alert('업체 전화번호 형식이 올바르지 않습니다.');
      return;
    }

    const endpoint = isPartner ? 'partner' : 'member';
    const dto = isPartner ? {
      ...form,
      partnerRegion: form.partnerRegion,
    } : form;

    await axios.put(`${baseUrl}/smash/profile/${endpoint}`, dto, {
      withCredentials: true,
    });

    if (imageFile) {
      const fd = new FormData();
      fd.append("file", imageFile);
      await axios.post(`${baseUrl}/smash/profile/image`, fd, {
        withCredentials: true,
        headers: { 'Content-Type': 'multipart/form-data' },
      });
    }

    alert('수정이 완료되었습니다.');
    navigate('/profile');
  };

  if (!profile) return <div>로딩 중...</div>;

  return (
    <div className="profile_update_container">
      <div className="profile_update_main_container">
        <h1>프로필 수정</h1>

        <div className="profile_update_img_container">
          <div className="profile_update_img">
            <img src={previewUrl || DefaultImage} alt="프로필" className="profile_image" />
            <div className="profile_update_img_inner">
              <input type="file" accept="image/*" onChange={handleImageChange} />
              <i class="fa-solid fa-camera"></i>
            </div>
          </div>

          <div className="profile_update_img_button">
            <button onClick={handleImageDelete}>기본 이미지로 변경</button>
          </div>
        </div>

        <div className="profile_update_inform">
          <div className="profile_update_text"> 
            <h2>닉네임</h2>

            <div className="update_nickname_box">
              <input name="nickname" value={form.nickname} onChange={handleChange} />
              <button type="button" onClick={handleNicknameCheck} disabled={form.nickname === originalValues.nickname}>중복 확인</button>
            </div>
            
              {form.nickname !== originalValues.nickname && !nicknameChecked && (
                <p style={{ color: 'red' }}>중복 확인을 해주세요.</p>
              )}
          
            
	        </div>


          <div className="profile_update_text">
            <h2>이메일</h2>
            <input name="email" value={profile.email} readonly />
          </div>

          <div className="profile_update_text">
            <h2>로그인 타입</h2>
            <input name="login_type" value={profile.loginType} readonly />
          </div>
            
          <div className="profile_update_text">
            <h2>전화번호</h2>
            <input name="tel" value={form.tel} onChange={handleChange} />
            {!validations.tel && <p style={{ color: 'red' }}>휴대폰 번호가 유효하지 않거나 이미 사용 중입니다.</p>}
          </div>

          <div className="profile_update_text">
            <h2>주소</h2>
            <input name="region" 
                value={form.region} 
                readOnly 
                onClick={() => handleAddressSearch('region')} 
                placeholder="주소 검색 클릭" />
            

	        </div>

 

      {isPartner && (
        <>
          <div className="profile_update_text">
            <h2>업체명</h2>
            <input name="partnerName" value={form.partnerName} onChange={handleChange} />
          </div>

          <div className="profile_update_text">
            <h2>업체 전화</h2>
            <input name="partnerTel" value={form.partnerTel} onChange={handleChange} />
              {!validations.partnerTel && <p style={{ color: 'red' }}>업체 전화번호 형식이 올바르지 않습니다.</p>}
	        </div>        

          <div className="profile_update_text">
            <h2>업체 주소</h2>
            <input name="partnerRegion" 
                     value={form.partnerRegion} 
                     readOnly 
                     onClick={() => handleAddressSearch('partnerRegion')} 
                     placeholder="주소 검색 클릭" />
	        </div>

        </>
      )}

          <div className="profile_update_form_buttons">
              <button onClick={handleSubmit}>저장</button>
              <button onClick={() => navigate('/profile')}>뒤로가기</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UpdateProfile;