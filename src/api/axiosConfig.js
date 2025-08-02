import axios from 'axios';

// Tek bir global değişken, yenileme işleminin devam edip etmediğini kontrol etmek için.
let isRefreshing = false;
// Yenileme tamamlandığında çalıştırılacak olan bekleyen istekleri (callback'leri) tutan bir dizi.
let refreshSubscribers = [];

// Bekleyen istekleri yeni token ile çalıştıran fonksiyon.
const onRefreshed = (token) => {
    refreshSubscribers.map(callback => callback(token));
    refreshSubscribers = []; // İşlem bitince listeyi temizle
};

const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api',
});

const logoutUser = () => {
    console.error("Auth Error: Tokenlar temizleniyor ve kullanıcı giriş sayfasına yönlendiriliyor.");
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    window.location.href = '/login';
};

apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

apiClient.interceptors.response.use(
    (response) => {
        return response;
    },
    async (error) => {
        const { config, response } = error;
        const originalRequest = config;

        // Hatanın token yenileme için uygun olup olmadığını kontrol et.
        if (response && (response.status === 401 || response.status === 403)) {
            
            // Eğer zaten bir yenileme işlemi yoksa, yenisini başlat.
            if (!isRefreshing) {
                isRefreshing = true;

                const refreshToken = localStorage.getItem('refreshToken');
                if (!refreshToken) {
                    logoutUser();
                    return Promise.reject(error);
                }

                try {
                    // Yeni access token için istek gönder.
                    const refreshResponse = await axios.post(`${apiClient.defaults.baseURL}/auth/refresh`, {
                        refreshToken: refreshToken
                    });

                    const { accessToken: newAccessToken } = refreshResponse.data;
                    localStorage.setItem('accessToken', newAccessToken);
                    
                    isRefreshing = false; // Yenileme tamamlandı.
                    
                    // Bekleyen istekleri yeni token ile tekrar gönder.
                    onRefreshed(newAccessToken);

                    // Orijinal isteği de yeni token ile tekrar gönder.
                    originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
                    return apiClient(originalRequest);

                } catch (refreshError) {
                    console.error("REFRESH TOKEN İLE YENİ TOKEN ALINAMADI!", refreshError.response?.data || refreshError.message);
                    isRefreshing = false; // Hata durumunda da flag'i sıfırla.
                    onRefreshed(null); // Bekleyen isteklere hata gönder.
                    logoutUser();
                    return Promise.reject(refreshError);
                }
            }

            // Eğer zaten bir yenileme işlemi varsa, bu isteği bekleme listesine ekle.
            // Yenileme tamamlandığında bu Promise çözülecek ve istek yeni token ile tekrarlanacak.
            return new Promise((resolve) => {
                refreshSubscribers.push((token) => {
                    if (token) {
                        originalRequest.headers['Authorization'] = `Bearer ${token}`;
                        resolve(apiClient(originalRequest));
                    } else {
                        resolve(Promise.reject(error));
                    }
                });
            });
        }

        return Promise.reject(error);
    }
);

export default apiClient;
