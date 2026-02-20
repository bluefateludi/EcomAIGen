<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

// 优化提示词功能已移除

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功')
      // 跳转到对话页面，确保ID是字符串类型
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }

  try {
    const res = await listMyAppVoByPage({
      pageNum: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const res = await listGoodAppVoByPage({
      pageNum: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
  }
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

// 格式化时间函数已移除，不再需要显示创建时间

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()

  // 鼠标跟随光效
  const handleMouseMove = (e: MouseEvent) => {
    const { clientX, clientY } = e
    const { innerWidth, innerHeight } = window
    const x = (clientX / innerWidth) * 100
    const y = (clientY / innerHeight) * 100

    document.documentElement.style.setProperty('--mouse-x', `${x}%`)
    document.documentElement.style.setProperty('--mouse-y', `${y}%`)
  }

  document.addEventListener('mousemove', handleMouseMove)

  // 清理事件监听器
  return () => {
    document.removeEventListener('mousemove', handleMouseMove)
  }
})
</script>

<template>
  <div id="homePage">
    <div class="container">
      <!-- 网站标题和描述 -->
      <div class="hero-section">
        <h1 class="hero-title">AI 应用生成平台</h1>
        <p class="hero-description">一句话轻松创建网站应用</p>
      </div>

      <!-- 用户提示词输入框 -->
      <div class="input-section">
        <a-textarea
          v-model:value="userPrompt"
          placeholder="帮我创建个人博客网站"
          :rows="4"
          :maxlength="1000"
          class="prompt-input"
        />
        <div class="input-actions">
          <a-button type="primary" size="large" @click="createApp" :loading="creating">
            <template #icon>
              <span>↑</span>
            </template>
          </a-button>
        </div>
      </div>

      <!-- 快捷按钮 -->
      <div class="quick-actions">
        <a-button
          type="default"
          @click="
            setPrompt(
              '创建一个现代化的个人博客网站，包含文章列表、详情页、分类标签、搜索功能、评论系统和个人简介页面。采用简洁的设计风格，支持响应式布局，文章支持Markdown格式，首页展示最新文章和热门推荐。',
            )
          "
          >个人博客网站</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '设计一个专业的企业官网，包含公司介绍、产品服务展示、新闻资讯、联系我们等页面。采用商务风格的设计，包含轮播图、产品展示卡片、团队介绍、客户案例展示，支持多语言切换和在线客服功能。',
            )
          "
          >企业官网</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '构建一个功能完整的在线商城，包含商品展示、购物车、用户注册登录、订单管理、支付结算等功能。设计现代化的商品卡片布局，支持商品搜索筛选、用户评价、优惠券系统和会员积分功能。',
            )
          "
          >在线商城</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '制作一个精美的作品展示网站，适合设计师、摄影师、艺术家等创作者。包含作品画廊、项目详情页、个人简历、联系方式等模块。采用瀑布流或网格布局展示作品，支持图片放大预览和作品分类筛选。',
            )
          "
          >作品展示网站</a-button
        >
      </div>

      <!-- 我的作品 -->
      <div class="section">
        <h2 class="section-title">我的作品</h2>
        <div class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="myAppsPage.current"
            v-model:page-size="myAppsPage.pageSize"
            :total="myAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个应用`"
            @change="loadMyApps"
          />
        </div>
      </div>

      <!-- 精选案例 -->
      <div class="section">
        <h2 class="section-title">精选案例</h2>
        <div class="featured-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
            :featured="true"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="featuredAppsPage.current"
            v-model:page-size="featuredAppsPage.pageSize"
            :total="featuredAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个案例`"
            @change="loadFeaturedApps"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#homePage {
  width: 100%;
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background: var(--bg-secondary);
  position: relative;
  overflow: hidden;
}

/* Subtle gradient background */
#homePage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 20% 20%, rgba(3, 105, 161, 0.06) 0%, transparent 40%),
    radial-gradient(circle at 80% 80%, rgba(6, 182, 212, 0.05) 0%, transparent 40%);
  pointer-events: none;
}

/* Mouse tracking light effect */
#homePage::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(
    500px circle at var(--mouse-x, 50%) var(--mouse-y, 50%),
    rgba(3, 105, 161, 0.05) 0%,
    transparent 70%
  );
  pointer-events: none;
  transition: opacity 0.3s ease;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-xl);
  position: relative;
  z-index: 2;
  width: 100%;
  box-sizing: border-box;
}

/* Hero Section */
.hero-section {
  text-align: center;
  padding: 60px 0 40px;
  margin-bottom: 32px;
  position: relative;
  overflow: hidden;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 16px;
  line-height: 1.2;
  font-family: var(--font-heading, 'Poppins', sans-serif);
  background: linear-gradient(135deg, var(--primary-blue) 0%, var(--accent-cyan) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
  position: relative;
  z-index: 2;
}

.hero-description {
  font-size: 18px;
  margin: 0;
  color: var(--text-secondary);
  position: relative;
  z-index: 2;
  font-family: var(--font-body, 'Open Sans', sans-serif);
}

/* Input Section */
.input-section {
  position: relative;
  margin: 0 auto 32px;
  max-width: 800px;
}

.prompt-input {
  border-radius: var(--radius-xl);
  border: 2px solid var(--border-light);
  font-size: 16px;
  padding: 20px 60px 20px 20px;
  background: var(--bg-glass);
  backdrop-filter: blur(12px);
  box-shadow: var(--shadow-glass);
  transition: all var(--transition-normal);
  font-family: var(--font-body, 'Open Sans', sans-serif);
  color: var(--text-primary);
}

.prompt-input:hover {
  border-color: var(--border-medium);
  box-shadow: var(--shadow-md);
}

.prompt-input:focus {
  background: var(--bg-primary);
  border-color: var(--primary-blue);
  box-shadow: 0 0 0 4px rgba(3, 105, 161, 0.1), var(--shadow-lg);
  transform: translateY(-2px);
}

.input-actions {
  position: absolute;
  bottom: 12px;
  right: 12px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.input-actions .ant-btn {
  background: linear-gradient(135deg, var(--primary-blue), var(--accent-cyan));
  border: none;
  border-radius: var(--radius-lg);
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-md);
  transition: all var(--transition-normal);
}

.input-actions .ant-btn:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-lg);
}

.input-actions .ant-btn span {
  font-size: 20px;
  color: var(--text-white);
}

/* Quick Actions */
.quick-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 48px;
  flex-wrap: wrap;
}

.quick-actions .ant-btn {
  border-radius: var(--radius-full);
  padding: 10px 20px;
  height: auto;
  background: var(--bg-glass);
  backdrop-filter: blur(12px);
  border: 1px solid var(--border-light);
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 14px;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-sm);
}

.quick-actions .ant-btn:hover {
  background: var(--bg-primary);
  border-color: var(--primary-blue);
  color: var(--primary-blue);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

/* Section Styles */
.section {
  margin-bottom: 48px;
  position: relative;
  z-index: 2;
}

.section-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 24px;
  color: var(--text-primary);
  font-family: var(--font-heading, 'Poppins', sans-serif);
}

/* Grid Layouts */
.app-grid,
.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.pagination-wrapper :deep(.ant-pagination) {
  display: flex;
  gap: 8px;
}

.pagination-wrapper :deep(.ant-pagination-item) {
  border-radius: var(--radius-md);
  border-color: var(--border-light);
  transition: all var(--transition-fast);
}

.pagination-wrapper :deep(.ant-pagination-item:hover) {
  border-color: var(--primary-blue);
  color: var(--primary-blue);
}

.pagination-wrapper :deep(.ant-pagination-item-active) {
  background: linear-gradient(135deg, var(--primary-blue), var(--accent-cyan));
  border-color: var(--primary-blue);
}

.pagination-wrapper :deep(.ant-pagination-item-active a) {
  color: var(--text-white);
}

/* Responsive Design */
@media (max-width: 768px) {
  .container {
    padding: var(--spacing-md);
  }

  .hero-title {
    font-size: 36px;
  }

  .hero-description {
    font-size: 16px;
  }

  .app-grid,
  .featured-grid {
    grid-template-columns: 1fr;
  }

  .input-section {
    max-width: 100%;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 28px;
  }

  .hero-description {
    font-size: 14px;
  }

  .quick-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .quick-actions .ant-btn {
    width: 100%;
  }
}
</style>
