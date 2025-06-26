import { useState, useEffect, useRef } from 'react';
import TitleBar from '../components/TitleBar';
import EditableField from '../components/UpdateStore';
import Slider from 'react-slick';
import axios from 'axios';
import '../styles/StoreInfo.css';

const dummyImages = [
  'https://images.pexels.com/photos/46798/the-ball-stadion-football-the-pitch-46798.jpeg',
  'https://images.pexels.com/photos/863988/pexels-photo-863988.jpeg',
  'https://images.pexels.com/photos/248547/pexels-photo-248547.jpeg',
  'https://images.pexels.com/photos/163452/basketball-dunk-blue-game-163452.jpeg',
]

// react-slick 슬라이더 설정
  const sliderSettings = {
    dots: true,
    infinite: true,
    speed: 500,
    slidesToShow: 1,
    slidesToScroll: 1,
    autoplay: true,
    autoplaySpeed: 3000,
    arrows: false,
  };

// StorePage
const StorePage = () => {
  const [view, setView] = useState('estimate'); // 'estimate' or 'review'
  const [isEditing, setIsEditing] = useState(false);
  const textareaRef = useRef();
  
  const [storeName, setStoreName] = useState("업체 이름");
  const [location, setLocation] = useState("경기 광명시 OO동");
  const [contact, setContact] = useState("02-024-3578");
  const [description, setDescription] = useState("스포츠용품 판매하며\n다른곳과 차별화된 판매처.\n프리미엄만을 추구함.");

  useEffect(() => {
    if (isEditing && textareaRef.current) {
      textareaRef.current.focus();
    }
  }, [isEditing]);

  const viewHandler = (type) => {
    setView(type);
    // 견적서 & 리뷰 API 호출
  };

  const updateHandelr = () => {
    if (isEditing) {
      // API 호출 업데이트
    }
    setIsEditing(!isEditing);
  }

  return (
    <>
      <TitleBar title="업체 이름" />
      <div className="storeInfo_container">
        {/* 상점 images */}
        {!isEditing && <div className="updateStore"><span onClick={() => setIsEditing(true)}>업체정보 수정</span></div>}
        {isEditing && <div className="updateStore"><span onClick={()=> updateHandelr()}>수정 완료</span></div>}
        {dummyImages.length > 0 ? (
          <>
            <Slider {...sliderSettings} className="carousel-slider">
              {dummyImages.map((img, idx) => (
                <div key={idx} className="carousel-wrapper">
                  <div className="carousel-card">
                    <div className="carousel-overlay" />
                    <img src={img} alt={img + "이미지"} />
                  </div>
                </div>
              ))}
            </Slider>
          </>
        ) 
        : 
        (
          <div style={{ padding: "1rem", textAlign: "center" }}>
            <p>업체 이미지가 없습니다.</p>
          </div>
        )}

        {/* 업체 별점 */}
        <div className="storeRating">
          <div className="ratingStars">
            <i className="fa-solid fa-star"></i>
            <i className="fa-solid fa-star"></i>
            <i className="fa-solid fa-star"></i>
            <i className="fa-regular fa-star-half-stroke"></i>
            <i className="fa-regular fa-star"></i>
          </div>
          <span className='point'>3.8점</span>
        </div>

        {/* 업체 정보 */}
        <div className="storeInfo_box">
          <hr className='line' />
          <div className='store_info'>
            <div  className='store_info_left'>
              <div className='store_icon_box'>
                <i className="fa-solid fa-store"></i>
              </div>
              {!isEditing && <h2 className='store_name'>{storeName}</h2>}
              {isEditing && <input
                type="text"
                value={storeName}
                onChange={(e) => setStoreName(e.target.value)}
                placeholder="업체 이름을 입력하세요"
              />}
            </div>
            <div className='store_info_right'>
              <p><span className='badge'>사업자 번호</span>0507-0116</p>
              <EditableField
                label="사업자 위치"
                value={location}
                onChange={setLocation}
                isEditing={isEditing}
              />
              <EditableField
                label="사업자 연락처"
                value={contact}
                onChange={setContact}
                isEditing={isEditing}
              />
            </div>
          </div>

          <hr className='line' />
          <div className='description'>
            <h2 className='store_des'>업체 설명</h2>
            {isEditing ? (
            <textarea
              ref={textareaRef}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          ) : (
            <p>
              {description.split("\n").map((line, idx) => (
                <span key={idx}>{line}<br /></span>
              ))}
            </p>
          )}
          </div>

          <hr className='line' />
          <div className="view_more">
            <div className={`button ${view == "estimate" && "active"}`} onClick={()=> viewHandler('estimate')}>견적서 목록(21)</div>
            <div className={`button ${view == "review" && "active"}`} onClick={()=> viewHandler('review')}>대여 후기(8)</div>
          </div>
          <div className="view_content">
            {view === 'estimate' ? (
              <div className="estimate_list">
                {/* 견적서 목록 컴포넌트 */}
                <p>견적서 목록이 여기에 표시됩니다.</p>
              </div>
            ) : (
              <div className="review_list">
                {/* 대여 후기 목록 컴포넌트 */}
                <p>대여 후기 목록이 여기에 표시됩니다.</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
}

export default StorePage;