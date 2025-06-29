import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const TitleBar = ({title}) => {
  const [pageTitle, setPageTitle] = useState(title);
  const [badgeTitle, setBadgeTitle] = useState('');
  const [isPartner, setIsPartner] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    // 괄호가 있는 경우만 분리
    const match = title.match(/^\s*(.+?)\s*\(\s*(.+?)\s*\)\s*$/);
    console.log('REGEX match:', match);
    if (match) {
      const pureTitle = match[1].trim();
      const badge = match[2].trim();
      const partner = badge.includes('업체');

      setPageTitle(pureTitle);
      setBadgeTitle(badge);
      setIsPartner(partner); 
    } else {
      setPageTitle(title);
      setBadgeTitle('');
      setIsPartner(false);
    }

  }, [title]);

  return (
    <div className="titleBar_container" >
      <i className="fa-solid fa-chevron-left"
         onClick={() => navigate(-1)} 
         style={{fontSize: '30px', marginRight: '1rem', cursor: 'pointer'}}></i>
      <h1 style={{fontSize: '30px'}}>
        {pageTitle}
      </h1>
      {badgeTitle && !isPartner && <span className="title_badge user_badge">{badgeTitle}</span>}
      {badgeTitle && isPartner && <span className="title_badge partner_badge">{badgeTitle}</span>}
    </div>
  );
}

export default TitleBar;