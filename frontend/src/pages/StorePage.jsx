import { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { useUser } from '../contexts/UserContext'; 
import TitleBar from '../components/TitleBar';
import EditableField from '../components/UpdateStore';
import Slider from 'react-slick';
import apiClient , { baseUrl } from '../config/apiClient';
import EstimateList from '../components/EstimateList'; 
import ReviewList from '../components/ReviewList';
import '../styles/StoreInfo.css';


const StorePage = () => {
  const [view, setView] = useState('estimate');
  const [isEditing, setIsEditing] = useState(false);
  const nameRef = useRef();
  const [isOwner, setIsOwner] = useState(false);
  const [storeName, setStoreName] = useState('');
  const [location, setLocation] = useState('');
  const [contact, setContact] = useState('');
  const [description, setDescription] = useState('');
  const [bno, setBno] = useState('');
  const [imageURLs, setImageURLs] = useState([]);
  const [newImages, setNewImages] = useState([]);
  const [deleteImageIds, setDeleteImageIds] = useState([]);
  const [previewImages, setPreviewImages] = useState([]);  // 미리보기 이미지 상태 추가
  const [estimatesCount, setEstimatesCount] = useState(0);
  const [reviewsCount, setReviewsCount] = useState(0);

  const { code } = useParams();
  const user = useUser();
  const loggedInMemberId = user?.emailId;
  



  // 이찬영이 추가
    // ReviewList에서 전달받을 함수 추가
  const handleReviewStatsUpdate = ({ count, avgStar }) => {
    setReviewsCount(count);
    setAvgStar(avgStar);
  }; 
   // avgStar 상태 추가
  const [avgStar, setAvgStar] = useState(0);
  //여기까지

  useEffect(() => {
    if (!code || !loggedInMemberId) return;

    apiClient
      .get(`/store/${code}`, { params: { memberId: loggedInMemberId } })
      .then((res) => {
        console.log("서버 응답 데이터:", res.data); // 서버 응답 확인
        const data = res.data;
        setStoreName(data.name || '');
        setLocation(data.region || '');
        setContact(data.tel || '');
        setDescription(data.description || '');
        setBno(data.bno || '');
        setImageURLs(data.imageURLs || []);
        setEstimatesCount(data.estimates?.length || 0);
        setReviewsCount(data.reviews?.length || 0);
        setIsOwner(data.owner);  
      })
      .catch((err) => {
        console.error('업체 정보 불러오기 실패:', err);
      });

    if (isEditing && nameRef.current) {
      nameRef.current.focus();
    }
  }, [code, loggedInMemberId, isEditing]);

  const updateHandler = async () => {
    if (isEditing) {
      try {
        const formData = new FormData();
        formData.append('name', storeName);
        formData.append('tel', contact);
        formData.append('region', location);
        formData.append('description', description);
        formData.append('bno', bno);

        newImages.forEach((file) => {
          formData.append('newImages', file);
        });

        // 삭제할 이미지 ID도 같이 전송
        deleteImageIds.forEach((id) => {
          formData.append('deleteImageIds', id);
        });

        await apiClient.put(`/store/update`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });

        alert('업체 정보가 수정되었습니다.');

        setIsEditing(false);
        setNewImages([]);
        setDeleteImageIds([]);
        setPreviewImages([]);  // 미리보기 이미지 상태 초기화

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

  const viewHandler = (viewType) => {
    setView(viewType);  // 클릭한 버튼에 맞춰 탭을 변경
  };

  const sliderSettings = {
  dots: true,
  infinite: imageURLs > 1,
  speed: 500,
  slidesToShow: 1,
  slidesToScroll: 1,
  autoplay: true,
  autoplaySpeed: 100,
  arrows: false,
};

  const handleNewImagesChange = (e) => {
    const files = Array.from(e.target.files);
    setNewImages([...newImages, ...files]);
 
    // 미리보기 이미지 추가
    const previews = files.map((file) => URL.createObjectURL(file));
    setPreviewImages([...previewImages, ...previews]);
  };

  const handleImageDelete = (imgId) => {
    setDeleteImageIds([...deleteImageIds, imgId]);
  };

  const handleAllImagesDelete = () => {
    setDeleteImageIds(imageURLs.map((img, idx) => idx));  // 모든 이미지 삭제 
  };


  return (
    <>
      <TitleBar title="업체 이름" />
      <div className="storeInfo_container">
        {isOwner && (
          <div className="updateStore">
            <span onClick={updateHandler}>{isEditing ? '수정 완료' : '업체정보 수정'}</span>
          </div>
        )}

        {/* 이미지 슬라이더 */}
        {imageURLs.length > 0 ? (
          <Slider {...sliderSettings} className="carousel-slider">
            {imageURLs.map((imgURL, idx) => (
              <div key={idx} className="carousel-wrapper" style={{ position: 'relative' }}>
                <div className="carousel-card">
                  <div className="carousel-overlay" />
                  <img src={`${baseUrl}${imgURL}`} alt={`store image ${idx}`} />
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

        {/* 미리보기 이미지 */}
        {isEditing && previewImages.length > 0 && (
          <div className="image-preview">
            {previewImages.map((preview, idx) => (
              <img key={idx} src={preview} alt={`preview ${idx}`} style={{ width: '100px', margin: '5px' }} />
            ))}
          </div>
        )}

        {/* 이미지 삭제 및 전체 삭제 */}
        {isEditing && (
          <div>
            <button onClick={handleAllImagesDelete}>전체 이미지 삭제</button>
            {imageURLs.map((img, idx) => (
              <button key={idx} onClick={() => handleImageDelete(idx)}>삭제 {idx + 1}</button>
            ))}
          </div>
        )}

        {/* 업체 별점 - 하드코딩 대신 실제 avgStar 출력 이찬영 수정부분*/}
        <div className="storeRating">
          <div className="ratingStars">
            {/* 별점 아이콘은 필요에 따라 동적으로 변경해도 됩니다 */}
            {[...Array(5)].map((_, i) => {
              const starValue = i + 1;
              if (avgStar >= starValue) return <i key={i} className="fa-solid fa-star"></i>;
              if (avgStar >= starValue - 0.5) return <i key={i} className="fa-regular fa-star-half-stroke"></i>;
              return <i key={i} className="fa-regular fa-star"></i>;
            })}
          </div>
          <span className="point">{avgStar.toFixed(1)}점</span>
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
                <EstimateList bno={bno} />
              </div>
            ) : (
              <div className="review_list">
                <ReviewList bno={bno} onUpdateStats={handleReviewStatsUpdate} />
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
};

export default StorePage;
