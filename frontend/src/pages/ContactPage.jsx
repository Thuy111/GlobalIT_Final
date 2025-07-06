import { useState, useEffect } from 'react'
import TitleBar from "../components/TitleBar";
import { IoCall } from "react-icons/io5";
import { FaLocationDot } from "react-icons/fa6";
import { IoIosMail } from "react-icons/io";
import { FaExclamationCircle } from "react-icons/fa";
import { LuSend } from "react-icons/lu";
import { useUser } from '../contexts/UserContext';
import apiClient from '../config/apiClient';
import axios from 'axios';
import '../styles/Contact.css';

const Contact= () => {
  const user = useUser();
  const [currentUser, setCurrentUser] = useState(user);
  const [aboutTel, setAboutTel] = useState('');
  const [aboutEmail, setAboutEmail] = useState('');

  useEffect(() => {
    const fetchAbout = async () => {
      try {
        const response = await apiClient.get('/contact');
        if (response.data) {
          setAboutTel(response.data.tel || '');
          setAboutEmail(response.data.email || '');
        }
      } catch (error) {
        console.error('Error fetching about information:', error);
      }
    }
    fetchAbout();
  },[]);

  useEffect(() => {
    const handleUserChange = (newUser) => {
      setCurrentUser(newUser);
    };
  }, [user]);

  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [file, setFile] = useState(null);
  const [activeLoading, setActiveLoading] = useState(false);

  const [phonMgs, setPhoneMsg] = useState(null);
  const [emailMsg, setEmailMsg] = useState(null);
  const [buttonStatus, setbuttonStatus] = useState(true);

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      setFile(Array.from(e.target.files));
    } else {
      setFile(null);
    }
  };
  const handleSubmit = async (e) => {
    e.preventDefault(); 
    const alert = window.confirm(`
    연락처 : ${phone}
    이메일 : ${email ? email : currentUser.emailId}

    당신의 정보가 일치합니까?
    `);
    if (alert){
      setActiveLoading(true);
      const formData = new FormData();
      formData.append('name', name);
      formData.append('phone', phone);
      formData.append('email', email || currentUser.emailId);
      formData.append('message', message);

      if (file) {
        file.forEach((f) => {
          formData.append('files', f);
        });
      }

      try {
        await apiClient.post('/contact', formData);
        window.alert('성공적으로 문의가 접수되었습니다. 곧 연락드리겠습니다.');
        setName('');
        setPhone('');
        setEmail('');
        setMessage('');
        if (file) setFile(null);
        setActiveLoading(false);
      }catch(err) {
        if (axios.isAxiosError(err)) {
          console.error('이메일 보내기 실패:', err.response?.data);
          setActiveLoading(false);
        } else {
          console.error('Network error:', err);
          setActiveLoading(false);
        }
      }
    }else return;
  };

  const checkFormTel = () => {
    const regex = /^[0-9#*\-\s\+\(\)]+$/;
    return !regex.test(phone);
  }
  const checkFormEmail = () => {
    const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return !regex.test(email);
  }

  useEffect(() => {
    if(checkFormTel()===true){
      setPhoneMsg(
        <p className='form_warn_text'>
          <FaExclamationCircle className='icon_warn' />
          숫자와 해당 기호만 사용하세요. #, -, *, +, (, ).
        </p>
      );
    }else{
      setPhoneMsg(null);
    }
  }, [phone]);

  const emailHandleBlur = () => {
    if(checkFormEmail()===true){
      setEmailMsg(
        <p className='form_warn_text'>
          <FaExclamationCircle className='icon_warn' />
          유효한 이메일을 입력하세요.
        </p>
      );
    }else{
      setEmailMsg(null);
    }
  };

  useEffect(() => {
    if(checkFormEmail()===false){
      setEmailMsg(null);
    }
  }, [email]);

  useEffect(() => {
    if(user){
      if(name && phone && !checkFormTel() && message) {
        setbuttonStatus(false);
      } else {
        setbuttonStatus(true);
      }
    }else{
      if(name && phone && email && message && !checkFormTel() && !checkFormEmail()) {
        setbuttonStatus(false);
      } else {
        setbuttonStatus(true);
      }
    }
  }, [phone, email, name, message]);

  if(activeLoading) return <div className='loading'><i className="fa-solid fa-circle-notch"></i></div>;

  return (
    <>
      <TitleBar title="문의하기" />
      <div className="contact_container">
        <div className="contact_info_wrap">
          <div className="contact_info_box">
            <div className="contact_info_icon"><IoCall size={30} /></div>
            <div className="contact_info_title">Call us</div>
            <div className="contact_info_text">{aboutTel}</div>
          </div>
          <div className="contact_info_box">
            <div className="contact_info_icon"><FaLocationDot size={30} /></div>
            <div className="contact_info_title">Our location</div>
            <div className="contact_info_text">
              서울 관악구 남부순환로 1820 에그옐로우 14층
              <span className="contact_info_sub">(서울 관악구 봉천동 862-1 | 087887)</span>
              <div className="contact_info_button_wrap">
                <a href='/about/location' className='contact_info_btn'>자세히</a>
              </div>
            </div>
          </div>
          <div className="contact_info_box">
            <div className="contact_info_icon"><IoIosMail size={30} /></div>
            <div className="contact_info_title">Mail</div>
            <div className="contact_info_text">{aboutEmail}</div>
          </div>
        </div>
        <div className="contact_form_wrap">
          <h2 className="contact_form_title"><strong>문의하기</strong></h2>
          <p className="contact_form_req">별표 문양은 (<span className="req_star">*</span>) 필수 입력입니다.</p>

          <form className="contact_form" onSubmit={handleSubmit}>
            <div className="contact_form_row">
              <div className="form_group">
                <label className="form_label">이름 <span className="req_star">*</span></label>
                <input
                  type="text"
                  className="form_input"
                  placeholder="이름을 입력하세요."
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
              </div>
              <div className="form_group">
                <label className="form_label">연락처 <span className="req_star">*</span></label>
                <input
                  type="tel"
                  className="form_input"
                  placeholder="연락 받을 전화번호를 입력하세요."
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  required
                />
                {phonMgs}
              </div>
            </div>
            <div className="form_group">
              <label className="form_label">이메일 <span className="req_star">*</span></label>
              {currentUser?
              (<input
                type="email"
                className="form_input"
                value={currentUser.emailId}
                readOnly
                required
              />)
              :
              (<input
                type="email"
                className="form_input"
                placeholder="이메일을 입력하세요."
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                onBlur={emailHandleBlur}
              />)
            }
              {emailMsg} 
            </div>
            <div className="form_group">
              <label className="form_label">문의 내용 <span className="req_star">*</span></label>
              <textarea
                className="form_textarea"
                rows={3}
                placeholder="문의 내용을 입력하세요."
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                required
              />
            </div>
            <div className="form_group">
              <label className="form_label">파일 첨부</label>
              <input
                type="file"
                className="form_input"
                accept=".jpg, .jpeg, .png, .gif, .pdf, .doc, .docx, .txt, .ppt, .pptx, .zip, .rar, .xlsx, .hwp, .mp4"
                onChange={handleFileChange}
                multiple
              />
              <span className="form_file_label">
                <span className='form_file_selected'>첨부된 파일</span> : {file ? 
                  file.map(f => (
                    <span key={f.name} className='form_file_badge'>{f.name}</span>
                  ))
                : '파일이 없습니다.'}
              </span>
            </div>
            <div className="contact_button_wrap">
              <button disabled={buttonStatus} className="contact_button_submit" type="submit">
                <LuSend className='contact_button_icon' /> 보내기
              </button>
            </div>
          </form>
        </div>
      </div>
    </>
  )
}

export default Contact;