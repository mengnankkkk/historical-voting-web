// 加载热门投票列表
async function loadHotVotingList() {
    const container = document.getElementById('hotVotingList');
    if (!container) return;

    try {
        const response = await axios.get('/api/events/hot');
        const events = response.data;

        if (events.length === 0) {
            container.innerHTML = '<p class="text-center">暂无热门投票</p>';
            return;
        }

        const html = events.map(event => `
            <div class="vote-item">
                <h5 class="mb-2">${event.title}</h5>
                <p class="text-muted mb-2">${event.description}</p>
                <div class="d-flex justify-content-between align-items-center">
                    <small class="text-muted">参与人数: ${event.participantCount}</small>
                    <a href="/event.html?id=${event.id}" class="btn btn-sm btn-primary">参与投票</a>
                </div>
            </div>
        `).join('');

        container.innerHTML = html;
    } catch (error) {
        console.error('加载热门投票失败:', error);
        container.innerHTML = '<p class="text-center text-danger">加载失败，请稍后重试</p>';
    }
}

// 加载用户排行榜
async function loadUserRankList() {
    const container = document.getElementById('userRankList');
    if (!container) return;

    try {
        const response = await axios.get('/api/users/rank');
        const users = response.data;

        if (users.length === 0) {
            container.innerHTML = '<p class="text-center">暂无排名数据</p>';
            return;
        }

        const html = users.map((user, index) => `
            <div class="rank-item">
                <span class="rank-number">${index + 1}</span>
                <img src="${user.avatar || '/img/default-avatar.png'}" alt="${user.nickname || user.username}" class="user-avatar me-2">
                <div>
                    <div>${user.nickname || user.username}</div>
                    <small class="text-muted">${user.userRank}</small>
                </div>
                <div class="ms-auto">
                    <small class="text-muted">经验值: ${user.experience}</small>
                </div>
            </div>
        `).join('');

        container.innerHTML = html;
    } catch (error) {
        console.error('加载用户排行榜失败:', error);
        container.innerHTML = '<p class="text-center text-danger">加载失败，请稍后重试</p>';
    }
}

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', () => {
    loadHotVotingList();
    loadUserRankList();
}); 