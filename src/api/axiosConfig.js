// src/api/axiosConfig.js
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api', // Backend adresin
  headers: {
    'Content-Type': 'application/json',
  },
});

// İşte sihir burada başlıyor: Axios Interceptor
// Bu kod, her API isteği gönderilmeden önce araya girer.
apiClient.interceptors.request.use(
  (config) => {
    // localStorage'dan token'ı al
    const token = localStorage.getItem('accessToken');
    if (token) {
      // Eğer token varsa, isteğin header'ına ekle
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default apiClient;