import { useEffect, useState } from 'react';
import apiClient from '../config/apiClient';

function PartnerReviewList({ bno }) {
  const [reviewList, setReviewList] = useState([]);
  const [avgStar, setAvgStar] = useState(0);

  useEffect(() => {
    if (!bno) return;
    apiClient.get(`/smash/review/bno`, { params: { bno } })
      .then(res => {
        setReviewList(res.data.reviews);
        setAvgStar(res.data.avgScore);
      })
      .catch(err => {
        console.error('리뷰 목록 불러오기 실패:', err);
      });
  }, [bno]);

  return (
    <div className="partner-review-list">
<h3>⭐ 평균 별점: {(avgStar ?? 0).toFixed(1)} / 5.0</h3>


      {(!reviewList || reviewList.length === 0) ?(
        <p>등록된 리뷰가 없습니다.</p>
      ) : (
        reviewList.map((review) => (
          <div className="review-card" key={review.idx}>
            <div className="review-header">
              <span className="nickname">{review.nickname}</span>
              <span className="star">⭐ {review.star}</span>
              <span className="created">{review.createdAt?.slice(0, 10)}</span>
            </div>
            <div className="comment">{review.comment}</div>
            <div className="images">
              {review.images && review.images.map((img, i) => (
                <img key={i} src={img.url} alt="리뷰 이미지" />
              ))}
            </div>
          </div>
        ))
      )}
    </div>
  );
}

export default PartnerReviewList;
