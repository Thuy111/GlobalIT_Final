import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const PartnerConvertPage = () => {
  const [form, setForm] = useState({
    bno: '',
    name: '',
    tel: '',
    region: '',
    description: ''
  });
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const navigate = useNavigate();
  const baseUrl = import.meta.env.VITE_API_URL;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      const res = await axios.post(`${baseUrl}/smash/partner/convert`, form, { withCredentials: true });
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

  return (
    <div className="partner_convert_container">
      <h1>사업자 등록 및 전환</h1>
      <form onSubmit={handleSubmit} className="partner_convert_form">
        <label>
          사업자 번호
          <input type="text" name="bno" value={form.bno} onChange={handleChange} required />
        </label>
        <label>
          업체명
          <input type="text" name="name" value={form.name} onChange={handleChange} required />
        </label>
        <label>
          업체 전화번호
          <input type="text" name="tel" value={form.tel} onChange={handleChange} required />
        </label>
        <label>
          업체 주소
          <input type="text" name="region" value={form.region} onChange={handleChange} required />
        </label>
        <label>
          업체 소개
          <textarea name="description" value={form.description} onChange={handleChange} required />
        </label>
        {error && <p className="error_text">❗ {error}</p>}
        <button type="submit">사업자 인증 및 전환</button>
      </form>
    </div>
  );
};

export default PartnerConvertPage;