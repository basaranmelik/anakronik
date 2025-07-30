import React, { useState, useEffect, useRef, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import RiskMap from '../assets/risk-map.svg?react';
import '../styles/theme.css'; // Ortak tema stilleri
import './MapPage.css';
import { getRegionDisplayName } from '../utils/regionMapping';

function MapPage() {
  const { logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [figures, setFigures] = useState([]);
  const svgRef = useRef(null);

  const [popup, setPopup] = useState({
    visible: false,
    pinned: false,
    x: 0,
    y: 0,
    regionName: '',
    figuresInRegion: []
  });

  const [isLoadingChat, setIsLoadingChat] = useState(false);

  useEffect(() => {
    apiClient.get('/historical-figures')
      .then(response => {
        setFigures(response.data.content || []);
      })
      .catch(error => console.error("Figürler yüklenemedi:", error));
  }, []);

  useEffect(() => {
    const svgElement = svgRef.current;
    if (!svgElement) return;

    const interactivePaths = svgElement.querySelectorAll('g#interactive-layer path');

    const showPopup = (event) => {
      const regionId = event.target.id;
      if (!regionId) return;
      const regionDisplayName = getRegionDisplayName(regionId);
      const figuresInRegion = figures.filter(figure => figure && figure.region && figure.region === regionDisplayName);
      setPopup({ ...popup, visible: true, x: event.clientX, y: event.clientY, regionName: regionDisplayName, figuresInRegion: figuresInRegion });
    };

    const handleMouseOver = (event) => { if (!popup.pinned) showPopup(event); };
    const handleMouseMove = (event) => { if (!popup.pinned) setPopup(prev => ({ ...prev, x: event.clientX, y: event.clientY })); };
    const handleMouseLeave = () => { if (!popup.pinned) setPopup(prev => ({ ...prev, visible: false })); };

    const handleClick = (event) => {
      const regionId = event.target.id;
      if (!regionId || !event.target.closest('g#interactive-layer')) {
        setPopup({ visible: false, pinned: false, figuresInRegion: [] });
        return;
      }
      showPopup(event);
      setPopup(prev => ({ ...prev, pinned: true }));
    };

    interactivePaths.forEach(path => {
      path.addEventListener('mouseover', handleMouseOver);
      path.addEventListener('mousemove', handleMouseMove);
      path.addEventListener('mouseleave', handleMouseLeave);
    });
    svgElement.addEventListener('click', handleClick);

    return () => {
      interactivePaths.forEach(path => {
        path.removeEventListener('mouseover', handleMouseOver);
        path.removeEventListener('mousemove', handleMouseMove);
        path.removeEventListener('mouseleave', handleMouseLeave);
      });
      svgElement.removeEventListener('click', handleClick);
    };
  }, [figures, popup.pinned]);

  const handleFigureSelect = (figure) => {
    navigate(`/chat/${figure.id}`);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="map-page-wrapper">
      <nav className="navbar">
        <div className="navbar-brand">
          <Link to="/">Anakronik</Link>
        </div>
        <div className="navbar-links">
          <Link to="/create-figure">Figür Ekle</Link>
          <button onClick={handleLogout} className="nav-button">Çıkış Yap</button>
        </div>
      </nav>

      <div className="map-content-area">
        <RiskMap ref={svgRef} />
      </div>

      {popup.visible && (
        <div
          className={`figure-menu ${popup.pinned ? 'pinned' : 'floating'}`}
          style={{ top: `${popup.y + 20}px`, left: `${popup.x + 20}px` }}
        >
          <h4>{popup.regionName}</h4>
          <ul>
            {popup.figuresInRegion.length > 0 ? (
              popup.figuresInRegion.map(figure => (
                <li key={figure.id} className="figure-item" onClick={() => handleFigureSelect(figure)}>
                  {figure.name}
                </li>
              ))
            ) : (
              <li className="empty-message">Bu bölgede figür yok.</li>
            )}
          </ul>
        </div>
      )}

      {isLoadingChat && (
        <div className="loading-overlay">
          <div className="spinner"></div>
        </div>
      )}

    </div>
  );
}

export default MapPage;