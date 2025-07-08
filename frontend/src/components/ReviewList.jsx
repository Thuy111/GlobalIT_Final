import { useEffect, useState } from 'react';
import apiClient from '../config/apiClient';
import '../styles/ReviewList.css';

function ReviewList({ bno, onUpdateStats }) {  // onUpdateStats 콜백 추가
  const [reviewList, setReviewList] = useState([]);
  const [avgStar, setAvgStar] = useState(0);
  
  useEffect(() => {
    if (!bno) return;
    apiClient.get(`/store/reviews`, { params: { bno } })
      .then(res => {
        const reviews = res.data.reviews || [];
        const avgScore = res.data.avgScore || 0;
        setReviewList(reviews);
        setAvgStar(avgScore);

        // 부모에게 리뷰수와 평균별점 전달
        if (onUpdateStats) {
          onUpdateStats({ count: reviews.length, avgStar: avgScore });
        }
      })
      .catch(err => {
        console.error('리뷰 목록 불러오기 실패:', err);
        if (onUpdateStats) {
          onUpdateStats({ count: 0, avgStar: 0 });
        }
      });
  }, [bno, onUpdateStats]);

  return (
    <>
      {(!reviewList || reviewList.length === 0) ?(
        <p>등록된 리뷰가 없습니다.</p>
      ) : (
        reviewList.map((review) => (
        <div className="review_list">
          <div className="review-card" key={review.idx}>
            <div className="review-header">
              <span className="nickname">{review.nickname}</span>
              <span className="created">{review.createdAt?.slice(0, 10)}</span>
            </div>
            <div className="star_area">
              <p className="star">
                {Array.from({ length: review.star }).map((_, i) => (
                  <i key={i} className="fa-solid fa-star"></i>
                ))}
                </p>
              <p className='point'>{review.star}.0점</p>
            </div>
            <div className="comment">{review.comment}</div>
            <div className="images">
              {review.images && review.images.map((img, i) => (
                <img key={i} src={`http://localhost:8080${img.path}`} alt="리뷰 이미지" />
              ))}
            </div>
          </div>
        </div>  
        ))
      )}
    </>
  );
}

export default ReviewList;
