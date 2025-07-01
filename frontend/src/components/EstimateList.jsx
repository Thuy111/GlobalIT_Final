import { useState, useEffect } from 'react';
import apiClient from '../config/apiClient';

const EstimateList = ({ bno }) => {
  const [estimates, setEstimates] = useState([]);

  useEffect(() => {
    const fetchEstimates = async () => {
      try {
        const res = await apiClient.get('/store/estimates', {
          params: { bno },
        });
        setEstimates(res.data);
      } catch (error) {
        console.error('견적서 목록 불러오기 실패:', error);
      }
    };

    fetchEstimates();
  }, [bno]);

  if (estimates.length === 0) {
    return <p>견적서 목록이 없습니다.</p>;
  }

  return (
    <div className="estimate_list">
      {estimates.map((estimate) => (
        <div key={estimate.idx} className="estimate_item">
          <h3>{estimate.title}</h3>
          <p>{estimate.content}</p>
          <p>가격: {estimate.price} 원</p>
          <p>배송: {estimate.isDelivery ? '가능' : '불가능'}</p>
          <p>픽업: {estimate.isPickup ? '가능' : '불가능'}</p>
          <p>반품: {estimate.isReturn ? '가능' : '불가능'}</p>
          <p>작성일: {estimate.createdAt}</p>
        </div>
      ))}
    </div>
  );
};

export default EstimateList;