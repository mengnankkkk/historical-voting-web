import axios from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: '/api/users',
  timeout: 5000
});

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      config.headers['Refresh-Token'] = `Bearer ${refreshToken}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  response => {
    // 如果有新的访问令牌，更新本地存储
    const newToken = response.headers['new-access-token'];
    if (newToken) {
      localStorage.setItem('accessToken', newToken);
    }
    return response;
  },
  error => {
    if (error.response && error.response.status === 401) {
      // 清除token并跳转到登录页面
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const userApi = {
  // 发送验证码
  sendVerificationCode(email) {
    return api.post('/register/send-code', null, { params: { email } });
  },

  // 注册
  register(userData) {
    return api.post('/register', userData);
  },

  // 登录
  login(credentials) {
    return api.post('/login', credentials);
  },

  // 登出
  logout() {
    return api.post('/logout');
  },

  // 获取用户信息
  getProfile() {
    return api.get('/profile');
  },

  // 更新用户信息
  updateProfile(profileData) {
    return api.put('/profile', profileData);
  }
}; 