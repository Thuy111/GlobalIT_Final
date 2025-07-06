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

function formatBno(bno) {
  // 이미 하이픈이 있으면 그대로 반환
  if (!bno) return '';
  if (bno.includes('-')) return bno;
  if (bno.length === 10) {
    // 10글자일 때: 0000000000 -> 000-00-00000
    return `${bno.slice(0, 3)}-${bno.slice(3, 5)}-${bno.slice(5)}`;
  }
  // 그 외는 원본 반환
  return bno;
}

function formatPhoneNumber(phone) {
  if (!phone) return '';
  // 이미 하이픈이 있으면 그대로 반환
  if (phone.includes('-')) return phone;
  // 숫자만 남기기
  const num = phone.replace(/\D/g, '');

  // 02-xxxx-xxxx (서울)
  if (num.length === 9 && num.startsWith('02')) {
    return `${num.slice(0,2)}-${num.slice(2,5)}-${num.slice(5)}`;
  }
  if (num.length === 10 && num.startsWith('02')) {
    return `${num.slice(0,2)}-${num.slice(2,6)}-${num.slice(6)}`;
  }
  // 0XX-xxx(x)-xxxx (지역번호 3자리)
  if (num.length === 10) {
    return `${num.slice(0,3)}-${num.slice(3,6)}-${num.slice(6)}`;
  }
  if (num.length === 11) {
    // 휴대폰(010 등) 3-4-4
    return `${num.slice(0,3)}-${num.slice(3,7)}-${num.slice(7)}`;
  }
  // 그 외는 원본 반환
  return phone;
}

const StorePage = () => {
  const baseUrl = import.meta.env.VITE_API_URL;
  const [view, setView] = useState('estimate');
  const [isEditing, setIsEditing] = useState(false);
  const nameRef = useRef();
  const [isOwner, setIsOwner] = useState(false);
  const [ownerEmail, setOwnerEmail] = useState('');
  const [storeName, setStoreName] = useState('');
  const [location, setLocation] = useState('');
  const [contact, setContact] = useState('');
  const [description, setDescription] = useState('');
  const [bno, setBno] = useState('');
  const [imageURLs, setImageURLs] = useState([]);        // 기존 이미지 URL 배열 (서버에서 받은)
  const [imageObjs, setImageObjs] = useState([]);        // 기존 이미지 객체 배열 (id, path)
  const [newImages, setNewImages] = useState([]);        // 새로 추가될 File 객체 배열
  const [previewImages, setPreviewImages] = useState([]); // 미리보기용 새 이미지 URL
  const [oldPreviewImages, setOldPreviewImages] = useState([]); // 기존 이미지 미리보기(state 추가)
  const [deleteImageIds, setDeleteImageIds] = useState([]); // 삭제할 기존 이미지 id
  const [estimatesCount, setEstimatesCount] = useState(0);
  const [reviewsCount, setReviewsCount] = useState(0);

  const [loading, setLoading] = useState(true); // 로딩 상태

  const { code } = useParams();
  const user = useUser();
  const loggedInMemberId = user?.emailId;
  
  // ReviewList에서 전달받을 함수 추가
  const handleReviewStatsUpdate = ({ count, avgStar }) => {
    setReviewsCount(count);
    setAvgStar(avgStar);
  }; 
  // avgStar 상태 추가
  const [avgStar, setAvgStar] = useState(0);

  useEffect(() => {
    if (!code) return;
    setLoading(true); // 데이터 로딩 시작
    const params = loggedInMemberId ? { memberId: loggedInMemberId } : {};
    apiClient
      .get(`/store/${code}`, { params })
      .then((res) => {
        console.log("서버 응답 데이터:", res.data); // 서버 응답 확인
        const data = res.data;
        setStoreName(data.name || '');
        setLocation(data.region || '');
        setContact(data.tel || '');
        setDescription(data.description || '');
        setBno(data.bno || '');
        setImageURLs(data.imageURLs || []);
        if (data.imageIdxs && data.imageURLs && data.imageIdxs.length === data.imageURLs.length) {
          setImageObjs(
            data.imageIdxs.map((id, idx) => ({
              imageIdx: id,
              path: data.imageURLs[idx],
            }))
          );
        } else {
          setImageObjs([]);
        }
        setEstimatesCount(data.estimates?.length || 0);
        setReviewsCount(data.reviews?.length || 0);
        setIsOwner(data.owner);
        setOwnerEmail(data.ownerEmail || '');
      })
      .catch((err) => {
        console.error('업체 정보 불러오기 실패:', err);
      });
    setLoading(false); // 데이터 로딩 완료
    if (isEditing && nameRef.current) {
      nameRef.current.focus();
    }
  }, [code, loggedInMemberId, isEditing]);

  // isEditing이 true로 변경될 때 기존 이미지들 oldPreviewImages로 복사
  // 1. 편집모드 진입 시에만 전체 초기화
  useEffect(() => {
    if (!isEditing) {
      setOldPreviewImages([]);
      setNewImages([]);
      setPreviewImages([]);
      setDeleteImageIds([]);
    }
  }, [isEditing]);

  // 2. 편집모드일 때만 oldPreviewImages 동기화
  useEffect(() => {
    if (isEditing) {
      const baseImages =
        imageObjs.length > 0
          ? imageObjs
          : imageURLs.map((url, idx) => ({ imageIdx: `temp${idx}`, path: url }));
      setOldPreviewImages(baseImages.filter(img => !deleteImageIds.includes(img.imageIdx)));
    }
  }, [isEditing, imageObjs, imageURLs, deleteImageIds]);

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
        deleteImageIds
          .filter(id => typeof id === 'number')
          .forEach((id) => {
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

        // 최산 data fetch
        const res = await apiClient.get(`/store/${code}`, { params: { memberId: loggedInMemberId } });
        const data = res.data;
        setStoreName(data.name || '');
        setLocation(data.region || '');
        setContact(data.tel || '');
        setDescription(data.description || '');
        setBno(data.bno || '');
        setImageURLs(data.imageURLs || []);
        setImageObjs(data.images || []);
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

  // 기존 이미지 개별 삭제(DB에 id 전송)
  const handleRemoveOldImage = (imgId) => {
    if (!deleteImageIds.includes(imgId)) {
      setDeleteImageIds([...deleteImageIds, imgId]);
    }
  };

  // 새로 추가된 이미지 삭제 (미리보기와 파일 배열에서 삭제)
  const handleRemoveNewImage = (idx) => {
    setNewImages((prev) => prev.filter((_, i) => i !== idx));
    setPreviewImages((prev) => prev.filter((_, i) => i !== idx));
  };

  // 기존 이미지 전체 삭제
  const handleAllOldImagesDelete = () => {
    // imageObjs 우선, 없으면 imageURLs
    const baseImages =
      imageObjs.length > 0
        ? imageObjs
        : imageURLs.map((url, idx) => ({ imageIdx: `temp${idx}`, path: url }));
    setDeleteImageIds(baseImages.map(img => img.imageIdx));
  };

  // 새로 추가한 이미지 전체 삭제
  const handleAllNewImagesDelete = () => {
    setNewImages([]);
    setPreviewImages([]);
  };

  // 채팅방 생성 함수
  const handleCreateRoom = (memberUser, partnerUser) => {
    console.log('채팅방 생성 요청:', memberUser, partnerUser);
    if(confirm(`${storeName}에 1:1 문의`) == false) {
      return;
    }
    window.location.href = `${baseUrl}/smash/chat/chatRoomInit?user=${partnerUser}`;
  }

  if (loading) return <div className='loading'><i className="fa-solid fa-circle-notch"></i></div>; // 로딩 중 표시

  return (
    <>
      <TitleBar title="업체 이름" />
      <div className="storeInfo_container">
        {isOwner && (
          <div className="updateStore">
            <span onClick={updateHandler}>{isEditing ? '수정 완료' : '업체정보 수정'}</span>
          </div>
        )}

        {/* 이미지 영역 */}
        {!isEditing ? (
          // 일반 모드: 기존 이미지 슬라이더
          imageURLs.length > 0 ? (
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
          )
        ) : (
          // 편집 모드: 이미지 업로드/미리보기/삭제
          <div className="image_preview_wrapper">
            <div className="camera_wrapper">
              <label htmlFor="image_Files" className="image_upload_box">
                <i className="fas fa-camera fa-2x"></i>
                <span>
                  <span id="image_count">{oldPreviewImages.length + newImages.length}</span>/10
                </span>
              </label>
              <button type="button" id="image_reset_btn" onClick={() => { handleAllOldImagesDelete(); handleAllNewImagesDelete(); }}>
                이미지 전체삭제
              </button>
            </div>
            {/* 새 이미지 업로드 */}
            <input
              type="file"
              id="image_Files"
              name="newImages"
              className="re_image"
              multiple
              accept="image/*"
              hidden
              onChange={handleNewImagesChange}
            />
            {/* 기존 이미지(썸네일) */}
            <div className="image_thumb_list">
              {oldPreviewImages.map((img, idx) => (
                <div className="image_thumb" key={img.imageIdx}>
                  <img src={`${baseUrl}${img.path}`} alt={`기존 이미지${idx + 1}`} />
                  <button type="button" className="remove_btn" onClick={() => handleRemoveOldImage(img.imageIdx)} data-id={img.imageIdx}>✕</button>
                </div>
              ))}
              {/* 새로 추가할 이미지 썸네일 (업로드 취소 가능) */}
              {previewImages.map((preview, idx) => (
                <div className="image_thumb" key={`newimg${idx}`}>
                  <img src={preview} alt={`미리보기${idx + 1}`} />
                  <button type="button" className="remove_btn" onClick={() => handleRemoveNewImage(idx)}>✕</button>
                </div>
              ))}
            </div>
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
              {loggedInMemberId&&!isOwner&&user?.role!=1&&
                <span className="icon_box"
                  onClick={()=> handleCreateRoom(loggedInMemberId, ownerEmail)}><i className="fa-solid fa-comment-dots"></i></span>
              }
            </div>
            <div className="store_info_right">
              <p>
                <span className="badge">사업자 번호</span> {bno ? formatBno(bno) : '-'}
              </p>
              <EditableField label="사업장 위치" value={location} onChange={setLocation} isEditing={isEditing} />
              <EditableField label="사업자 연락처" value={formatPhoneNumber(contact)} onChange={setContact} isEditing={isEditing} />
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
              <div className="estimate_area">
                <EstimateList bno={bno} />
              </div>
            ) : (
              <div className="review_area">
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
