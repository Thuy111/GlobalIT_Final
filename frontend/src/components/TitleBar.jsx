import { useNavigate } from 'react-router-dom';

const TitleBar = ({title}) => {
  const navigate = useNavigate();

  return (
    <div className="titleBar_container" >
      <i className="fa-solid fa-chevron-left"
         onClick={() => navigate(-1)} 
         style={{fontSize: '30px', marginRight: '1rem', cursor: 'pointer'}}></i>
      <h1 style={{fontSize: '30px'}}>{title}</h1>
    </div>
  );
}

export default TitleBar;