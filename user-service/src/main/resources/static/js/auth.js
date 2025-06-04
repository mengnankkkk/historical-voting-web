// 配置axios默认值
axios.defaults.baseURL = 'http://localhost:8080';
axios.defaults.headers.common['Content-Type'] = 'application/json';

// 添加请求拦截器
axios.interceptors.request.use(config => {
    const accessToken = localStorage.getItem('accessToken');
    if (accessToken) {
        config.headers['Authorization'] = `Bearer ${accessToken}`;
    }
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
        config.headers['Refresh-Token'] = `Bearer ${refreshToken}`;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

// 添加响应拦截器
axios.interceptors.response.use(response => {
    // 如果响应头中包含新的访问令牌，则更新本地存储
    const newAccessToken = response.headers['new-access-token'];
    if (newAccessToken) {
        localStorage.setItem('accessToken', newAccessToken);
    }
    return response;
}, error => {
    if (error.response && error.response.status === 401) {
        // 如果是401错误，清除令牌并重定向到登录页面
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login.html';
    }
    return Promise.reject(error);
});

// 检查用户是否已登录
function checkAuth() {
    const accessToken = localStorage.getItem('accessToken');
    const userNav = document.getElementById('userNav');
    if (userNav) {
        if (accessToken) {
            userNav.querySelector('.guest-nav').classList.add('d-none');
            userNav.querySelector('.user-nav').classList.remove('d-none');
            // 获取用户信息
            getUserInfo();
        } else {
            userNav.querySelector('.guest-nav').classList.remove('d-none');
            userNav.querySelector('.user-nav').classList.add('d-none');
        }
    }
}

// 获取用户信息
async function getUserInfo() {
    try {
        const response = await axios.get('/api/users/profile');
        const user = response.data;
        // 更新用户昵称
        const userNickname = document.getElementById('userNickname');
        if (userNickname) {
            userNickname.textContent = user.nickname || user.username;
        }
    } catch (error) {
        console.error('获取用户信息失败:', error);
    }
}

// 退出登录
async function logout() {
    try {
        await axios.post('/api/users/logout');
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login.html';
    } catch (error) {
        console.error('退出登录失败:', error);
    }
}

// 添加事件监听器
document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    
    // 退出登录按钮
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            logout();
        });
    }
}); 