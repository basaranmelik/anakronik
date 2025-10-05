import React, { useState, useEffect, useRef, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import RiskMap from '../assets/anakronik_map.svg?react';
import '../styles/theme.css';
import './MapPage.css';
import { getRegionDisplayName } from '../utils/regionMapping';

const toRoman = (num) => {
  if (num < 1 || num > 39) return "?";
  const roman = { M: 1000, CM: 900, D: 500, CD: 400, C: 100, XC: 90, L: 50, XL: 40, X: 10, IX: 9, V: 5, IV: 4, I: 1 };
  let str = '';
  for (let i of Object.keys(roman)) {
    let q = Math.floor(num / roman[i]);
    num -= q * roman[i];
    str += i.repeat(q);
  }
  return str;
};

function MapPage() {
  const { user } = useContext(AuthContext);
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

  const [regionData, setRegionData] = useState({});

  const [isLoadingChat, setIsLoadingChat] = useState(false);

  useEffect(() => {
    const svgElement = svgRef.current;
    if (!svgElement) return;

    apiClient.get('/historical-figures')
      .then(response => {
        const fetchedFigures = response.data.content || [];
        setFigures(fetchedFigures);

        const interactivePaths = svgElement.querySelectorAll('g#interactive-layer path');
        const newRegionData = {};
        const viewBox = svgElement.viewBox.baseVal;

        const counts = fetchedFigures.reduce((acc, figure) => {
          if (figure && figure.region) {
            acc[figure.region] = (acc[figure.region] || 0) + 1;
          }
          return acc;
        }, {});

        interactivePaths.forEach(path => {
          const regionId = path.id;
          const regionDisplayName = getRegionDisplayName(regionId);
          const bbox = path.getBBox();
          newRegionData[regionId] = {
            count: counts[regionDisplayName] || 0,
            x: bbox.x + bbox.width / 2,
            y: bbox.y + bbox.height / 2,
          };
        });

        const interactiveGroup = svgElement.querySelector('g#interactive-layer');
        if (interactiveGroup) {
          interactiveGroup.querySelectorAll('.region-count-text').forEach(el => el.remove());

          Object.entries(newRegionData).forEach(([regionId, data]) => {
            if (data.count > 0) {
              const textElement = document.createElementNS('http://www.w3.org/2000/svg', 'text');
              textElement.setAttribute('x', data.x);
              textElement.setAttribute('y', data.y);
              textElement.setAttribute('class', 'region-count-text');
              textElement.textContent = toRoman(data.count);

              interactiveGroup.appendChild(textElement);
            }
          });
        }
        setRegionData(newRegionData);
      })
      .catch(error => console.error("Figürler yüklenemedi:", error));

    const interactivePaths = svgElement.querySelectorAll('g#interactive-layer path');

    const showPopup = (event) => {
      const regionId = event.target.id;
      if (!regionId) return;
      const regionDisplayName = getRegionDisplayName(regionId);
      const figuresInRegion = figures.filter(figure => figure && figure.region && figure.region === regionDisplayName);
      setPopup({ visible: true, pinned: false, x: event.clientX, y: event.clientY, regionName: regionDisplayName, figuresInRegion: figuresInRegion });
    };
    const handleMouseOver = (event) => { if (!popup.pinned) showPopup(event); };
    const handleMouseMove = (event) => { if (!popup.pinned) setPopup(prev => ({ ...prev, x: event.clientX, y: event.clientY })); };
    const handleMouseLeave = () => { if (!popup.pinned) setPopup(prev => ({ ...prev, visible: false })); };
    const handleClick = (event) => {
      const regionId = event.target.id;
      if (!regionId || !event.target.closest('g#interactive-layer')) {
        setPopup(prev => ({ ...prev, visible: false, pinned: false }));
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
  }, [figures.length, popup.pinned]);

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
          <Link to="/profile">Profil</Link>
          {user?.role === 'ROLE_ADMIN' && (
            <Link to="/admin/users">Admin Paneli</Link>
          )}
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