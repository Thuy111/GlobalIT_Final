import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import TitleBar from '../../components/TitleBar';
import apiClient, { baseUrl } from '../../config/apiClient';
import DefaultImage from '../../assets/images/default-profile.png';
import '../../styles/UpdateProfile.css';

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
  const [phoneChecked, setPhoneChecked] = useState(false);
  const [addressDetail, setAddressDetail] = useState({
    regionDetail: '',
    partnerRegionDetail: '',
  });

  const navigate = useNavigate();

  useEffect(() => {
    apiClient.get(`/profile`, { withCredentials: true })
      .then(res => {
        const data = res.data;

        const [region, regionDetail = ''] = (data.region || '').split(',');
        const [partnerRegion, partnerRegionDetail = ''] = (data.partnerRegion || '').split(',');

        setProfile(data);
        setForm({
          nickname: data.nickname,
          tel: data.tel,
          region: region,
          partnerName: data.partnerName || '',
          partnerTel: data.partnerTel,
          partnerRegion: partnerRegion,
        });
        setAddressDetail({
          regionDetail,
          partnerRegionDetail,
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
      ? /^010-?\d{3,4}-?\d{4}$/ // 010 -? 3 or 4자리 -? 4자리 = 10~11자리
      : /^0\d{1,2}-?\d{3,4}-?\d{4}$/; // 0 1 or 2자리 -? 3 or 4자리 -? 4자리 = 9~11자리
    return regex.test(phone);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    setForm(prev => ({ ...prev, [name]: value }));

    if (name === 'nickname' && value !== originalValues.nickname) {
      setNicknameChecked(false);
    }

    if (name === 'tel') {
      setPhoneChecked(false);

      const valid = validatePhone(value, 'mobile');
      setValidations(prev => ({
        ...prev,
        tel: valid,
      }));
    }

    if (name === 'partnerTel') {
      const valid = validatePhone(value, 'business');
      setValidations(prev => ({
        ...prev,
        partnerTel: valid,
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
    apiClient.delete(`/profile/image`, { withCredentials: true })
      .then(() => {
        setImageFile(null);
        setPreviewUrl(null);
        const fileInput = document.querySelector('input[type="file"]');
        if (fileInput) fileInput.value = '';
      });
  };

  const handleNicknameCheck = () => {
    const nickname = form.nickname;
    if (!nickname) return;

    apiClient.get(`/profile/check-nickname`, {
      params: { nickname }
    }).then(res => {
      if (res.data.duplicated) {
        alert('중복된 닉네임입니다.');
        setValidations(prev => ({ ...prev, nickname: false }));
        setNicknameChecked(false);
      } else {
        alert('사용할 수 있는 닉네임입니다.');
        setValidations(prev => ({ ...prev, nickname: true }));
        setNicknameChecked(true);
      }
    }).catch(error => {
      console.error("닉네임 중복 확인 실패:", error);
      alert('닉네임 중복 확인 중 오류가 발생했습니다.');
    });
  };

  const handlePhoneCheck = () => {
    const phone = form.tel;
    if (!phone) return;

    if (!validatePhone(phone, 'mobile')) {
      alert('유효하지 않은 핸드폰 번호입니다.');
      setValidations(prev => ({ ...prev, tel: false }));
      setPhoneChecked(false);
      return;
    }

    apiClient.get(`/profile/check-phone`, {
      params: { phone }
    }).then(res => {
      if (!res.data.valid) {
        alert('중복된 핸드폰 번호입니다. 다시 입력하세요.');
        setValidations(prev => ({ ...prev, tel: false }));
        setPhoneChecked(false);
      } else {
        alert('사용 가능한 핸드폰 번호입니다.');
        setValidations(prev => ({ ...prev, tel: true }));
        setPhoneChecked(true);
      }
    }).catch(error => {
      console.error("핸드폰 번호 중복 확인 실패:", error);
      alert('핸드폰 번호 중복 확인 중 오류가 발생했습니다.');
      setPhoneChecked(false);
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
    if (form.nickname !== originalValues.nickname && !nicknameChecked) {
      alert('닉네임 중복 확인을 해주세요.');
      return;
    }
    if (form.nickname !== originalValues.nickname && !validations.nickname) {
      alert('중복된 닉네임입니다.');
      return;
    }
    if (form.tel !== originalValues.tel && !phoneChecked) {
      alert('핸드폰 번호 중복 확인을 해주세요.');
      return;
    }
    if (form.tel !== originalValues.tel && !validations.tel) {
      alert('유효하지 않은 핸드폰 번호입니다.');
      return;
    }
    if (isPartner && form.partnerTel && !validations.partnerTel) {
      alert('업체 전화번호 형식이 올바르지 않습니다.');
      return;
    }

    const dto = isPartner ? {
      ...form,
      region: `${form.region},${addressDetail.regionDetail}`,
      partnerRegion: `${form.partnerRegion},${addressDetail.partnerRegionDetail}`,
    } : {
      ...form,
      region: `${form.region},${addressDetail.regionDetail}`,
    };

    await apiClient.put(`/profile/${isPartner ? 'partner' : 'member'}`, dto, {
      withCredentials: true,
    });

    if (imageFile) {
      const fd = new FormData();
      fd.append("file", imageFile);
      await apiClient.post(`/profile/image`, fd, {
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
        <TitleBar title="프로필 수정" />

        <div className="profile_update_img_container">
          <div className="profile_update_img">
            <img src={previewUrl || DefaultImage} alt="프로필" className="profile_image" />
            <div className="profile_update_img_inner">
              <input type="file" accept="image/*" onChange={handleImageChange} />
              <i className="fa-solid fa-camera"></i>
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
              <button
                type="button"
                onClick={handleNicknameCheck}
                disabled={form.nickname === originalValues.nickname}
              >
                중복 확인
              </button>
            </div>
            {form.nickname !== originalValues.nickname && !nicknameChecked && (
              <p style={{ color: 'red' }}>중복 확인을 해주세요.</p>
            )}
            {!validations.nickname && (
              <p style={{ color: 'red' }}>중복된 닉네임입니다.</p>
            )}
          </div>

          <div className="profile_update_text">
            <h2>이메일</h2>
            <input name="email" value={profile.email} readOnly />
          </div>

          <div className="profile_update_text">
            <h2>로그인 타입</h2>
            <input name="login_type" value={profile.loginType} readOnly />
          </div>

          <div className="profile_update_text">
            <h2>전화번호</h2>
            <div className="update_phone_box">
              <input name="tel" value={form.tel} onChange={handleChange} maxLength={11} />
              <button
                type="button"
                onClick={handlePhoneCheck}
                disabled={form.tel === originalValues.tel}
              >
                중복 확인
              </button>
            </div>
            {!validations.tel && (
              <p style={{ color: 'red' }}>
                {phoneChecked
                  ? '중복된 핸드폰 번호입니다. 다시 입력하세요.'
                  : '유효하지 않은 핸드폰 번호입니다.'}
              </p>
            )}
          </div>

          <div className="profile_update_text">
            <h2>주소</h2>
            <div className="profile_region_field">
              <input
                name="region"
                value={form.region}
                readOnly
                onClick={() => handleAddressSearch('region')}
                placeholder="주소 검색 클릭"
              />
              <input
                className="region_detail"
                type="text"
                placeholder="상세 주소"
                value={addressDetail.regionDetail}
                onChange={(e) =>
                  setAddressDetail((prev) => ({ ...prev, regionDetail: e.target.value }))
                }
              />
            </div>
          </div>

          {isPartner && (
            <>
              <div className="profile_update_text">
                <h2>업체명</h2>
                <input name="partnerName" value={form.partnerName} onChange={handleChange} />
              </div>

              <div className="profile_update_text">
                <h2>업체 전화</h2>
                <input name="partnerTel" value={form.partnerTel} onChange={handleChange} maxLength={11} />
                {!validations.partnerTel && (
                  <p style={{ color: 'red' }}>업체 전화번호 형식이 올바르지 않습니다.</p>
                )}
              </div>

              <div className="profile_update_text">
                <h2>업체 주소</h2>
                <div className="profile_region_field">
                  <input
                    name="partnerRegion"
                    value={form.partnerRegion}
                    readOnly
                    onClick={() => handleAddressSearch('partnerRegion')}
                    placeholder="주소 검색 클릭"
                  />
                  <input
                    className="region_detail"
                    type="text"
                    placeholder="상세 주소"
                    value={addressDetail.partnerRegionDetail}
                    onChange={(e) =>
                      setAddressDetail((prev) => ({ ...prev, partnerRegionDetail: e.target.value }))
                    }
                  />
                </div>
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
