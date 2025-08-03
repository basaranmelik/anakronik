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

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

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

    // return bloğunuzda (JSX) değişiklik yok, aynı kalabilir...
    return (
        <div className="chat-page-layout">
            <div className="figures-sidebar">
                <Link to="/map" className="back-to-map-link">← Harita</Link>
                <h4>Karakterler</h4>
                <Link to="/create-figure" className="create-figure-link">+ Yeni Karakter Ekle</Link>

                <ul>
                    {allFigures.map(fig => (
                        <li
                            key={fig.id}
                            className={currentFigure && fig.id === currentFigure.id ? 'active' : ''}
                            onClick={() => navigate(`/chat/${fig.id}`)}
                        >
                            <img src={`${API_BASE_URL}${fig.imageUrl}`} alt={fig.name} className="sidebar-figure-image" />
                            <span>{fig.name}</span>
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