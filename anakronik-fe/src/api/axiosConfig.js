import axios from 'axios';

let isRefreshing = false;
let refreshSubscribers = [];

const onRefreshed = (token) => {
    refreshSubscribers.map(callback => callback(token));
    refreshSubscribers = [];
};

const apiClient = axios.create({
    baseURL: '/api',
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

        if (response && (response.status === 401 || response.status === 403)) {

            if (!isRefreshing) {
                isRefreshing = true;

                const refreshToken = localStorage.getItem('refreshToken');
                if (!refreshToken) {
                    logoutUser();
                    return Promise.reject(error);
                }

                try {
                    const refreshResponse = await axios.post(`${apiClient.defaults.baseURL}/auth/refresh`, {
                        refreshToken: refreshToken
                    });

                    const { accessToken: newAccessToken } = refreshResponse.data;
                    localStorage.setItem('accessToken', newAccessToken);

                    isRefreshing = false;

                    onRefreshed(newAccessToken);

                    originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
                    return apiClient(originalRequest);

                } catch (refreshError) {
                    console.error("REFRESH TOKEN İLE YENİ TOKEN ALINAMADI!", refreshError.response?.data || refreshError.message);
                    isRefreshing = false;
                    onRefreshed(null);
                    logoutUser();
                    return Promise.reject(refreshError);
                }
            }

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
