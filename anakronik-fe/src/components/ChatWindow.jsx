import React, { useState } from 'react';
import apiClient from '../api/axiosConfig';

function ChatWindow({ figure, onClose }) {
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleSendMessage = async (e) => {
        e.preventDefault();
        if (!input.trim()) return;

        const userMessage = { sender: 'user', text: input };
        setMessages(prev => [...prev, userMessage]);
        setInput('');
        setIsLoading(true);

        try {
            const response = await apiClient.post(`/chat/${figure.id}`, {
                question: input
            });

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

    return (
        <div className="chat-window">
            <div className="chat-header">
                <h3>{figure.name} ile Sohbet</h3>
                <button onClick={onClose} className="close-btn">X</button>
            </div>
            <div className="chat-messages">
                {messages.map((msg, index) => (
                    <div key={index} className={`message ${msg.sender}`}>
                        {msg.text}
                    </div>
                ))}
                {isLoading && <div className="message bot">...</div>}
            </div>
            <form className="chat-input-form" onSubmit={handleSendMessage}>
                <input
                    type="text"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    placeholder="Bir soru sorun..."
                    disabled={isLoading}
                />
                <button type="submit" disabled={isLoading}>Gönder</button>
            </form>
        </div>
    );
}

export default ChatWindow;