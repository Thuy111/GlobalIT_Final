import { useNavigate } from 'react-router-dom';

const TitleBar = ({title}) => {
  const navigate = useNavigate();

  return (
    <div className="titleBar_container" style={{display: 'flex', alignItems: 'center', position: 'fixed', top: 0, left: 0, width: '100%', padding: '1.5rem 2rem', boxShadow: '0 2px 4px rgba(0,0,0,0.1)'}}>
      <i class="fa-solid fa-chevron-left"  onClick={() => navigate(-1)} style={{fontSize: '30px', marginRight: '1rem', cursor: 'pointer'}}></i>
      <h1 style={{fontSize: '30px'}}>{title}</h1>
    </div>
  );
}

export default TitleBar;