import { useState, useEffect } from 'react';
import axios from 'axios';
import TitleBar from '../components/TitleBar';
import '../styles/StoreInfo.css';

const StorePage = () => {
  return (
    <>
      <TitleBar title="업체 이름" />
      <div className="patnerInfo_container">
        
      </div>
    </>
  );
}

export default StorePage;