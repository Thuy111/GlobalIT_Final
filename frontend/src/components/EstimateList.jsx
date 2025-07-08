import { useState, useEffect } from 'react';
import apiClient, { baseUrl } from '../config/apiClient';
import '../styles/EstimateList.css';

function formatDate(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const yy = String(date.getFullYear()).slice(2);
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  return `${yy}년 ${mm}월 ${dd}일`;
}

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
        <div key={estimate.idx} className="estimate_card">
          <p className='estimate_title'>{estimate.title}</p>
          <div className="estimate_info">
            <div className='estimate_row'>
              <span className="badge">제시 가격</span>
              <p>{estimate.price} 원</p>
            </div>
            <div className='estimate_row'>
              <span className="badge">대여 방법</span>
              <p className="deal_method">
                {estimate.isDelivery === true && (
                  <span className="delivery yes">배달<i className="fa-solid fa-check"></i></span>
                )}
                {estimate.isDelivery === false && (
                  <span className="delivery no">배달<i className="fa-solid fa-xmark"></i></span>
                )}
                {estimate.isPickup === true && (
                  <span className="pickup yes">픽업<i className="fa-solid fa-check"></i></span>
                )}
                {estimate.isPickup === false && (
                  <span className="pickup no">픽업<i className="fa-solid fa-xmark"></i></span>
                )}
              </p>
            </div>
            <div className='estimate_footer'>
              <p className="estimate_date">
                {formatDate(estimate.createdAt)}
                {estimate.isModified && <small>(수정 됨)</small>}
              </p>
              <button className='detail'
                      onClick={() => window.location.href = `${baseUrl}/smash/request/detail/${estimate.requestIdx}`}>
                <i className="fa-solid fa-share"></i>
              </button>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default EstimateList;