// src/pages/ChatPage.jsx
import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import './ChatPage.css';

const API_BASE_URL = 'http://localhost:8080';

function ChatPage() {
    // State'lerinizde değişiklik yok...
    const { figureId } = useParams();
    const navigate = useNavigate();
    const [currentFigure, setCurrentFigure] = useState(null);
    const [allFigures, setAllFigures] = useState([]);
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const messagesEndRef = useRef(null);
    const [openMenuId, setOpenMenuId] = useState(null); // Hangi menünün açık olduğunu tutar
    const [currentUser, setCurrentUser] = useState(null); // Mevcut kullanıcı bilgilerini tutar
    const fileInputRef = useRef(null); // Gizli dosya input'u için referans

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        apiClient.get('/users/profile')
            .then(response => setCurrentUser(response.data))
            .catch(error => console.error("Kullanıcı profili çekilemedi:", error));
    }, []);

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    useEffect(() => {
        const fetchAllData = async () => {
            setIsLoading(true);
            try {
                const [allFiguresResponse, currentFigureResponse] = await Promise.all([
                    apiClient.get('/historical-figures'),
                    apiClient.get(`/historical-figures/${figureId}`)
                ]);

                const currentFig = currentFigureResponse.data;
                setAllFigures(allFiguresResponse.data.content || []);
                setCurrentFigure(currentFig);

                // --- DÜZELTME: Karakter yüklendiğinde hoş geldin mesajı ekleniyor ---
                setMessages([
                    {
                        sender: 'bot',
                        text: `Merhaba! Ben ${currentFig.name}. Size nasıl yardımcı olabilirim?`
                    }
                ]);

            } catch (err) {
                console.error("Veri çekilemedi:", err);
                setMessages([{ sender: 'bot', text: 'Karakter bilgilerini yüklerken bir sorun oluştu.' }]);
            } finally {
                setIsLoading(false);
            }
        };
        if (figureId) {
            fetchAllData();
        }
    }, [figureId]);

    const handleSendMessage = async (e) => {
        e.preventDefault();
        if (!input.trim() || !currentFigure) return;

        const userMessage = { sender: 'user', text: input };
        setMessages(prev => [...prev, userMessage]);
        setInput('');
        setIsLoading(true);

        try {
            const response = await apiClient.post(`/chat/${currentFigure.id}`, { question: input });
            const botMessage = { sender: 'bot', text: response.data.answer };
            setMessages(prev => [...prev, botMessage]);
        } catch (error) {
            console.error("Sohbet hatası:", error);
            const errorMessage = { sender: 'bot', text: "Üzgünüm, bir sorun oluştu." };
            setMessages(prev => [...prev, errorMessage]);
        } finally {
            setIsLoading(false);
        }
    };

    const handleMenuToggle = (figId) => {
        setOpenMenuId(openMenuId === figId ? null : figId);
    };

    const handleDeleteFigure = async (figToDelete) => {
        if (window.confirm(`'${figToDelete.name}' karakterini silmek istediğinize emin misiniz? Bu işlem geri alınamaz.`)) {
            try {
                await apiClient.delete(`/historical-figures/${figToDelete.id}`);
                // Listeyi yenile ve eğer silinen karakter o anki sohbet ise haritaya dön
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

    const handleAddDocumentClick = () => {
        // Gizli file input'u tetikle
        fileInputRef.current.click();
    };

    const handleFileSelected = async (event) => {
        const file = event.target.files[0];
        if (!file || !openMenuId) return;

        const formData = new FormData();
        formData.append('file', file);
        formData.append('docName', file.name); // Veya başka bir isim

        try {
            // openMenuId, o anki figürün ID'sini tutuyor
            await apiClient.post(`/historical-figures/${openMenuId}/add-document`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            alert(`'${file.name}' dokümanı başarıyla eklendi.`);
        } catch (error) {
            console.error("Doküman eklenirken hata oluştu:", error);
            alert("Doküman eklenirken bir hata oluştu.");
        }
        setOpenMenuId(null); // Menüyü kapat
        event.target.value = null;
    };

    // Henüz backend'i hazır olmadığı için bu fonksiyon şimdilik sadece bir uyarı verir
    const handleClearHistory = (figureName) => {
        alert(`'${figureName}' için sohbet geçmişini silme özelliği yakında eklenecek!`);
        setOpenMenuId(null);
    };

    // return bloğunuzda (JSX) değişiklik yok, aynı kalabilir...
    return (
        <div className="chat-page-layout">
            <div className="figures-sidebar">
                <Link to="/map" className="back-to-map-link">← Harita</Link>
                <h4>Karakterler</h4>
                <Link to="/create-figure" className="create-figure-link">+ Yeni Karakter Ekle</Link>
                <input type="file" ref={fileInputRef} style={{ display: 'none' }} onChange={handleFileSelected} />

                <ul>
                    {allFigures.map(fig => (
                        <li
                            key={fig.id}
                            className={currentFigure && fig.id === currentFigure.id ? 'active' : ''}
                            onClick={() => navigate(`/chat/${fig.id}`)}
                        >
                            <img src={`${API_BASE_URL}${fig.imageUrl}`} alt={fig.name} className="sidebar-figure-image" />
                            <span>{fig.name}</span>

                            <div className="figure-menu-container">
                                <button className="menu-button" onClick={(e) => {
                                    e.stopPropagation(); // Tıklama olayının yayılmasını burada durdur!
                                    handleMenuToggle(fig.id);
                                }}>⋮</button>
                                {openMenuId === fig.id && (
                                    <div className="figure-menu-dropdown">
                                        <div onClick={() => handleClearHistory(fig.name)}>Sohbeti Sil</div>
                                        {/* Sadece kullanıcının kendi oluşturduğu figürler için bu seçenekleri göster */}
                                        {currentUser && fig.createdByUsername === currentUser.email && (
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
                        {isLoading &&
                            <div className="message-block bot">
                                <div className="message-header">{currentFigure ? currentFigure.name : '...'}</div>
                                <div className="message-content">
                                    <span className="typing-indicator"></span>
                                </div>
                            </div>
                        }
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
                        <img
                            src={`${API_BASE_URL}${currentFigure.imageUrl}`}
                            alt={currentFigure.name}
                            className="character-image"
                        />
                        <h3>{currentFigure.name}</h3>
                        <p className="character-dates">{currentFigure.birthDate} - {currentFigure.deathDate}</p>
                        <p className="character-bio">{currentFigure.bio}</p>
                    </div>
                ) : (
                    <div className="character-card-placeholder">
                        Karakter bilgileri yükleniyor...
                    </div>
                )}
            </div>
        </div>
    );
}

export default ChatPage;