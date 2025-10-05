import React, { useState, useEffect, useRef, useContext } from 'react';
import { useParams, useNavigate, Link, Navigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import apiClient from '../api/axiosConfig';
import './ChatPage.css';

const getInitials = (name = '') => {
    return name.split(' ').map(n => n[0]).join('').toUpperCase();
};

const mapHistoryToMessages = (history = []) => {
    if (!history) return [];
    return history.map(item => ({
        sender: (item.role && item.role.toUpperCase() === 'USER') ? 'user' : 'bot',
        text: item.message || item.text || item.content || ""
    }));
};

function ChatPage() {
    const { user } = useContext(AuthContext);
    const { figureId } = useParams();
    const navigate = useNavigate();

    const [currentFigure, setCurrentFigure] = useState(null);
    const [allFigures, setAllFigures] = useState([]);
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [openMenuId, setOpenMenuId] = useState(null);

    const messagesEndRef = useRef(null);
    const fileInputRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(scrollToBottom, [messages]);

    useEffect(() => {
        const fetchAllData = async () => {
            setIsLoading(true);
            try {
                const [allFiguresResponse, currentFigureResponse, historyResponse] = await Promise.all([
                    apiClient.get('/historical-figures'),
                    apiClient.get(`/historical-figures/${figureId}/card`),
                    apiClient.get(`/chat/${figureId}`)
                ]);

                const currentFig = currentFigureResponse.data;
                setAllFigures(allFiguresResponse.data.content || []);
                setCurrentFigure(currentFig);

                const history = historyResponse.data?.history;
                if (history && history.length > 0) {
                    setMessages(mapHistoryToMessages(history));
                } else {
                    setMessages([{
                        sender: 'bot',
                        text: `Merhaba! Ben ${currentFig.name}. Size nasıl yardımcı olabilirim?`
                    }]);
                }
            } catch (err) {
                console.error("Veri çekilemedi:", err);

                const statusCode = err.response?.status;

                if (statusCode === 404 || statusCode === 500) {
                    navigate('/error-page');
                } else {
                    setMessages([{ sender: 'bot', text: 'Karakter bilgilerini yüklerken bir sorun oluştu.' }]);
                }
            } finally {
                setIsLoading(false);
            }
        };
        if (figureId) {
            fetchAllData();
        }
    }, [figureId, navigate]);

    const handleSendMessage = async (e) => {
        e.preventDefault();
        if (!input.trim() || !currentFigure) return;
        const userMessage = { sender: 'user', text: input };
        setMessages(prev => [...prev, userMessage]);
        setInput('');
        setIsLoading(true);
        try {
            const response = await apiClient.post(`/chat/${currentFigure.id}`, { question: input });
            setMessages(mapHistoryToMessages(response.data.history));
        } catch (error) {
            console.error("Sohbet hatası:", error);
            const errorMessage = { sender: 'bot', text: "Üzgünüm, bir sorun oluştu." };
            setMessages(prev => [...prev, errorMessage]);
        } finally {
            setIsLoading(false);
        }
    };

    const handleMenuToggle = (figId) => setOpenMenuId(openMenuId === figId ? null : figId);

    const handleDeleteFigure = async (figToDelete) => {
        if (window.confirm(`'${figToDelete.name}' karakterini silmek istediğinize emin misiniz? Bu işlem geri alınamaz.`)) {
            try {
                await apiClient.delete(`/historical-figures/${figToDelete.id}`);
                setAllFigures(allFigures.filter(f => f.id !== figToDelete.id));
                if (currentFigure && currentFigure.id === figToDelete.id) {
                    navigate('/map');
                }
            } catch (error) {
                console.error("Figür silinirken hata oluştu:", error);
                alert("Figür silinirken bir hata oluştu.");
            }
        }
    };

    const handleAddDocumentClick = () => fileInputRef.current.click();

    const handleFileSelected = async (event) => {
        const file = event.target.files[0];
        if (!file || !openMenuId) return;
        const formData = new FormData();
        formData.append('file', file);
        formData.append('docName', file.name);
        try {
            await apiClient.post(`/historical-figures/${openMenuId}/add-document`, formData, { headers: { 'Content-Type': 'multipart/form-data' } });
            alert(`'${file.name}' dokümanı başarıyla eklendi.`);
        } catch (error) {
            console.error("Doküman eklenirken hata oluştu:", error);
            alert("Doküman eklenirken bir hata oluştu.");
        }
        setOpenMenuId(null);
        event.target.value = null;
    };

    const handleClearHistory = async (fig) => {
        if (window.confirm(`'${fig.name}' ile olan sohbet geçmişinizi silmek istediğinize emin misiniz?`)) {
            try {
                await apiClient.delete(`/chat/${fig.id}`);
                setMessages([{ sender: 'bot', text: `Merhaba! Ben ${fig.name}. Size nasıl yardımcı olabilirim?` }]);
                alert("Sohbet geçmişi başarıyla silindi.");
            } catch (error) {
                console.error("Geçmiş silinirken hata:", error);
                alert("Sohbet geçmişi silinirken bir sorun oluştu.");
            }
        }
        setOpenMenuId(null);
    };

    return (
        <div className="chat-page-layout">
            <div className="figures-sidebar">
                <Link to="/map" className="back-to-map-link">← Harita</Link>
                <h4>Karakterler</h4>
                <Link to="/create-figure" className="create-figure-link">+ Yeni Karakter Ekle</Link>
                <input type="file" ref={fileInputRef} style={{ display: 'none' }} onChange={handleFileSelected} />
                <ul>
                    {allFigures.map(fig => (
                        <li key={fig.id} className={currentFigure && fig.id === currentFigure.id ? 'active' : ''}>
                            <div className="figure-name" onClick={() => navigate(`/chat/${fig.id}`)}>
                                {fig.imageUrl ? (
                                    <img src={fig.imageUrl} alt={fig.name} className="sidebar-figure-image" />
                                ) : (
                                    <div className="sidebar-figure-placeholder">{getInitials(fig.name)}</div>
                                )}
                                <span>{fig.name}</span>
                            </div>
                            <div className="figure-menu-container">
                                <button className="menu-button" onClick={(e) => { e.stopPropagation(); handleMenuToggle(fig.id); }}>⋮</button>
                                {openMenuId === fig.id && (
                                    <div className="figure-menu-dropdown">
                                        <div onClick={() => handleClearHistory(fig)}>Sohbeti Sil</div>
                                        {user && fig.createdByUsername === user.email && (
                                            <>
                                                <div onClick={handleAddDocumentClick}>Doküman Ekle</div>
                                                <div className="delete-option" onClick={() => handleDeleteFigure(fig)}>Figürü Sil</div>
                                            </>
                                        )}
                                    </div>
                                )}
                            </div>
                        </li>
                    ))}
                </ul>
            </div>
            <div className="chat-main-area">
                <div className="chat-content-wrapper">
                    <div className="chat-header">
                        <h3>{currentFigure ? currentFigure.name : "Karakter Yükleniyor..."}</h3>
                    </div>
                    <div className="chat-messages">
                        {messages.map((msg, index) => (
                            <div key={index} className={`message-block ${msg.sender}`}>
                                <div className="message-header">
                                    {msg.sender === 'user' ? 'Siz' : (currentFigure ? currentFigure.name : '...')}
                                </div>
                                <div className="message-content">
                                    <p>{msg.text}</p>
                                </div>
                            </div>
                        ))}
                        {isLoading && (
                            <div className="message-block bot">
                                <div className="message-header">{currentFigure ? currentFigure.name : '...'}</div>
                                <div className="message-content"><span className="typing-indicator"></span></div>
                            </div>
                        )}
                        <div ref={messagesEndRef} />
                    </div>
                    <form className="chat-input-form" onSubmit={handleSendMessage}>
                        <input
                            type="text"
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            placeholder={currentFigure ? `${currentFigure.name} ile sohbet et...` : "Yükleniyor..."}
                            disabled={isLoading || !currentFigure}
                        />
                        <button type="submit" disabled={isLoading || !currentFigure}>Gönder</button>
                    </form>
                </div>
            </div>
            <div className="character-card-area">
                {currentFigure ? (
                    <div className="character-card">
                        {currentFigure.imageUrl ? (
                            <img src={currentFigure.imageUrl} alt={currentFigure.name} className="character-image" />
                        ) : (
                            <div className="character-image-placeholder">{getInitials(currentFigure.name)}</div>
                        )}
                        <h3>{currentFigure.name}</h3>
                        <p className="character-dates">{currentFigure.birthDate} - {currentFigure.deathDate}</p>
                        <p className="character-bio">{currentFigure.bio}</p>
                    </div>
                ) : (<div className="character-card-placeholder">Yükleniyor...</div>)}
            </div>
        </div>
    );
}

export default ChatPage;