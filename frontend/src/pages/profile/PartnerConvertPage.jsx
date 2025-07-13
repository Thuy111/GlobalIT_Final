import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import TitleBar from '../../components/TitleBar';
import '../../styles/PartnerConvertPage.css';
import apiClient from '../../config/apiClient';

const PartnerConvertPage = () => {
  const [form, setForm] = useState({
    bno: '',
    name: '',
    telFst: '',
    telSnd: '',
    telThr: '',
    tel: '',         // 합쳐진 전화번호
    region: '',         // 도로명 주소
    detailRegion: '',   // 상세 주소
    description: ''
  });
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value, maxLength  } = e.target;

    // 전화번호 조각은 3, 4, 4자리 제한
    if (name === 'telFst' && !/^\d{0,3}$/.test(value)) return;
    if ((name === 'telSnd' || name === 'telThr') && !/^\d{0,4}$/.test(value)) return;

    // input 자동 포커싱
    if (value.length === 3 && name === 'telFst') {
      document.querySelector('input[name="telSnd"]')?.focus();
    }
    if (value.length === 4 && name === 'telSnd') {
      document.querySelector('input[name="telThr"]')?.focus();
    }

    if(name === 'bno' && !/^\d*$/.test(value)) {
      setError('사업자 번호는 숫자만 입력 가능합니다.');
      return;
    }else if (name === 'tel' && !/^\d{0,11}$/.test(value)) {
      setError('전화번호는 숫자만 입력 가능하며 최대 11자리입니다.');
      return;
    }else {
      setError(null);
    }
    setForm({ ...form, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      const payload = {
        ...form,
        tel: form.telFst + form.telSnd + form.telThr, // 전화번호 합치기
        region: form.region + ' ' + form.detailRegion
      };
      delete payload.detailRegion; // 상세 주소는 백엔드에 따로 안 보낼 거면 제거
      delete payload.telFst; // 전화번호 부분은 합쳐서 보낼 거라 제거
      delete payload.telSnd;
      delete payload.telThr;

      const res = await apiClient.post(`/partner/convert`, payload, { withCredentials: true });
      if (res.data.valid) {
        alert('사업자 전환 성공');
        setSuccess(true);
        navigate('/profile');
      } else {
        setError(res.data.message);
      }
    } catch (err) {
      console.error(err);
      setError('서버 오류 또는 인증 실패');
    }
  };

  const handleAddressSearch = (field) => {
    new window.daum.Postcode({
      oncomplete: (data) => {
        const fullAddr = data.roadAddress + (data.buildingName ? ` (${data.buildingName})` : '');
        setForm(prev => ({ ...prev, [field]: fullAddr }));
      }
    }).open();
  };

  // autoComplete="new-tel-part..." // input의 자동완성 기능을 비활성화하기 위한 속성

  return (
    <div className="partner_convert_container">
      <TitleBar title="사업자 등록 및 전환" />
      <form onSubmit={handleSubmit} className="partner_convert_form">
        <label>
          <h2>사업자 번호</h2>
          <input type="text" name="bno" value={form.bno} onChange={handleChange} required placeholder='숫자만 입력해주세요.' className='short_input' />
        </label>
        <label>
          <h2>업체명</h2>
          <input type="text" name="name" value={form.name} onChange={handleChange} required autoComplete="new-tel-part5" />
        </label>
        <label>
          <h2>업체 전화번호</h2>
          <div className='partner_convert_tel'>
            <input type="text" name="telFst" value={form.telFst} autoComplete="new-tel-part1" onChange={handleChange} className='short_input' />-
            <input type="text" name="telSnd" value={form.telSnd} autoComplete="new-tel-part2" onChange={handleChange} className='short_input' />-
            <input type="text" name="telThr" value={form.telThr} autoComplete="new-tel-part3" onChange={handleChange} className='short_input' />
          </div>
        </label>
        <label>
          <h2>업체 주소</h2>
          <div className='partner_convert_address'>
            <input type="text" name="region" value={form.region} placeholder='주소를 검색해주세요' readOnly required />
            <button type="button" onClick={() => handleAddressSearch('region')} >주소 검색</button>
          </div>
          <input type="text" name="detailRegion" value={form.detailRegion} onChange={handleChange} autoComplete="new-tel-part4" required placeholder='상세 주소를 입력하세요' />
        </label>
        <label>
          <h2>업체 소개</h2>
          <textarea name="description" value={form.description} onChange={handleChange} required placeholder='업체에 대한 소개를 기재해주세요.' />
        </label>
        <div className='partner_convert_buttons'>
          {error && <p className="error_text">❗ {error}</p>}
          <button type="submit">사업자 인증 및 전환</button>
        </div>
      </form>
    </div>
  );
};

export default PartnerConvertPage;