import { useState, useEffect, useRef } from 'react';
import TitleBar from '../components/TitleBar';
import EditableField from '../components/UpdateStore';
import Slider from 'react-slick';
import apiClient from '../config/apiClient';
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
  const nameRef = useRef();

  const [storeName, setStoreName] = useState('');
  const [location, setLocation] = useState('');
  const [contact, setContact] = useState('');
  const [description, setDescription] = useState('');
  const [bno, setBno] = useState('');
  const [imageURLs, setImageURLs] = useState([]);
  const [newImages, setNewImages] = useState([]); // 새로 업로드할 이미지 파일들
  const [deleteImageIds, setDeleteImageIds] = useState([]); // 삭제할 이미지 ID 목록
  const [estimatesCount, setEstimatesCount] = useState(0);
  const [reviewsCount, setReviewsCount] = useState(0);


  const handleNewImagesChange = (e) => {
    setNewImages([...newImages, ...e.target.files]);
  };

  useEffect(() => {
    // 서버에서 업체 정보 가져오기
    if (!code || !loggedInMemberId) return;

    apiClient
      .get(`/store/${code}`, { params: { memberId: loggedInMemberId } })
      .then((res) => {
        const data = res.data;
        setStoreName(data.name || '');
        setLocation(data.region || '');
        setContact(data.tel || '');
        setDescription(data.description || '');
        setBno(data.bno || '');
        setImageURLs(data.imageURLs || []);
        setEstimatesCount(data.estimates?.length || 0);
        setReviewsCount(data.reviews?.length || 0);
      })
      .catch((err) => {
        console.error('업체 정보 불러오기 실패:', err);
      });
  

    if (isEditing && nameRef.current) {
      nameRef.current.focus();
    }
  },[code, loggedInMemberIdisEditing]);

  const viewHandler = (type) => {
    setView(type);
    // 견적서 & 리뷰 API 호출
  };

  const updateHandelr = async () => {
    if (isEditing) {
      try {
        const formData = new FormData();
        formData.append('name', storeName);
        formData.append('tel', contact);
        formData.append('region', location);
        formData.append('description', description);
        formData.append('bno', bno);

        // 새 이미지 추가
        newImages.forEach((file) => {
          formData.append('newImages', file);
        });

    await apiClient.put(`/store/update`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });

        alert('업체 정보가 수정되었습니다.');

        // 수정 후 다시 최신 정보 불러오기
        setIsEditing(false);
        setNewImages([]);
        setDeleteImageIds([]);

        const res = await apiClient.get(`/store/${code}`, { params: { memberId: loggedInMemberId } });
        const data = res.data;
        setStoreName(data.name || '');
        setLocation(data.region || '');
        setContact(data.tel || '');
        setDescription(data.description || '');
        setBno(data.bno || '');
        setImageURLs(data.imageURLs || []);
        setEstimatesCount(data.estimates?.length || 0);
        setReviewsCount(data.reviews?.length || 0);
      } catch (error) {
        console.error('업체 정보 수정 실패:', error);
        alert('업체 정보 수정에 실패했습니다.');
      }
    } else {
      setIsEditing(true);
    }
  };

  return (
    <>
      <TitleBar title="업체 이름" />
      <div className="storeInfo_container">
        {/* 상점 이미지 수정 버튼 */}
        <div className="updateStore">
          <span onClick={updateHandler}>{isEditing ? '수정 완료' : '업체정보 수정'}</span>
        </div>
        {/* 이미지 슬라이더 */}
        {imageURLs.length > 0 ? (
          <Slider {...sliderSettings} className="carousel-slider">
            {imageURLs.map((imgUrl, idx) => (
              <div key={idx} className="carousel-wrapper" style={{ position: 'relative' }}>
                <div className="carousel-card">
                  <div className="carousel-overlay" />
                  <img src={imgUrl} alt={`store image ${idx}`} />
                </div>
              </div>
            ))}
          </Slider>
        ) : (
          <div style={{ padding: '1rem', textAlign: 'center' }}>
            <p>업체 이미지가 없습니다.</p>
          </div>
        )}

        {/* 새 이미지 선택 (편집 모드일 때만) */}
        {isEditing && (
          <div style={{ marginTop: '1rem' }}>
            <label htmlFor="newImages">새 이미지 추가: </label>
            <input type="file" id="newImages" multiple onChange={handleNewImagesChange} />
          </div>
        )}

        {/* 업체 별점 (임시 하드코딩) */}
        <div className="storeRating">
          <div className="ratingStars">
            <i className="fa-solid fa-star"></i>
            <i className="fa-solid fa-star"></i>
            <i className="fa-solid fa-star"></i>
            <i className="fa-regular fa-star-half-stroke"></i>
            <i className="fa-regular fa-star"></i>
          </div>
          <span className="point">3.8점</span>
        </div>

        {/* 업체 정보 */}
        <div className="storeInfo_box">
          <hr className="line" />
          <div className="store_info">
            <div className="store_info_left">
              <div className="store_icon_box">
                <i className="fa-solid fa-store"></i>
              </div>
              {!isEditing && <h2 className="store_name">{storeName}</h2>}
              {isEditing && (
                <input
                  type="text"
                  value={storeName}
                  onChange={(e) => setStoreName(e.target.value)}
                  placeholder="업체 이름을 입력하세요"
                />
              )}
            </div>
            <div className="store_info_right">
              <p>
                <span className="badge">사업자 번호</span> {bno || '-'}
              </p>
              <EditableField label="사업자 위치" value={location} onChange={setLocation} isEditing={isEditing} />
              <EditableField label="사업자 연락처" value={contact} onChange={setContact} isEditing={isEditing} />
            </div>
          </div>

          <hr className="line" />
          <div className="description">
            <h2 className="store_des">업체 설명</h2>
            {isEditing ? (
              <textarea value={description} onChange={(e) => setDescription(e.target.value)} />
            ) : (
              <p>
                {description.split('\n').map((line, idx) => (
                  <span key={idx}>
                    {line}
                    <br />
                  </span>
                ))}
              </p>
            )}
          </div>

          <hr className="line" />
          <div className="view_more">
            <div className={`button ${view === 'estimate' ? 'active' : ''}`} onClick={() => viewHandler('estimate')}>
              견적서 목록({estimatesCount})
            </div>
            <div className={`button ${view === 'review' ? 'active' : ''}`} onClick={() => viewHandler('review')}>
              대여 후기({reviewsCount})
            </div>
          </div>

          <div className="view_content">
            {view === 'estimate' ? (
              <div className="estimate_list">
                <p>견적서 목록이 여기에 표시됩니다.</p>
              </div>
            ) : (
              <div className="review_list">
                <p>대여 후기 목록이 여기에 표시됩니다.</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
};

export default StorePage;