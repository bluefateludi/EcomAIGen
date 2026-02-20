<template>
  <a-layout-header class="header">
    <a-row :wrap="false">
      <!-- 左侧：Logo和标题 -->
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="header-left">
            <img class="logo" src="@/assets/logo.png" alt="Logo" />
            <h1 class="site-title">电商网站生成</h1>
          </div>
        </RouterLink>
      </a-col>
      <!-- 中间：导航菜单 -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </a-col>
      <!-- 右侧：用户操作区域 -->
      <a-col>
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="userAvatarSrc">
                  {{ userInitial }}
                </a-avatar>
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { type MenuProps, message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import { LogoutOutlined, HomeOutlined } from '@ant-design/icons-vue'
import { getUserAvatar, getUserInitial } from '@/utils/avatar'

const loginUserStore = useLoginUserStore()
const router = useRouter()

// Computed avatar properties
const userAvatarSrc = computed(() => getUserAvatar(loginUserStore.loginUser.userAvatar))
const userInitial = computed(() => getUserInitial(loginUserStore.loginUser.userName))
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 监听路由变化，更新当前选中菜单
router.afterEach((to, from, next) => {
  selectedKeys.value = [to.path]
})

// 菜单配置项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/appManage',
    label: '应用管理',
    title: '应用管理',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://github.com/bluefateludi/EcomAIGen', target: '_blank' }, 'GitHub'),
    title: 'GitHub',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

// 退出登录
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.header {
  background: var(--bg-glass);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 0 var(--spacing-xl);
  box-shadow: var(--shadow-sm);
  border-bottom: 1px solid var(--border-light);
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  transition: all var(--transition-normal);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  transition: opacity var(--transition-fast);
}

.header-left:hover {
  opacity: 0.8;
}

.logo {
  height: 40px;
  width: 40px;
  border-radius: var(--radius-md);
}

.site-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  font-family: var(--font-heading, 'Poppins', sans-serif);
  background: linear-gradient(135deg, var(--primary-blue), var(--accent-cyan));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:deep(.ant-menu-horizontal) {
  border-bottom: none !important;
  background: transparent;
  line-height: 64px;
}

:deep(.ant-menu-item) {
  border-radius: var(--radius-md);
  margin: 0 4px;
  transition: all var(--transition-fast);
  font-weight: 500;
}

:deep(.ant-menu-item:hover) {
  background: rgba(3, 105, 161, 0.08);
  color: var(--primary-blue);
}

:deep(.ant-menu-item-selected) {
  background: rgba(3, 105, 161, 0.12);
  color: var(--primary-blue);
}

:deep(.ant-menu-item a) {
  color: inherit;
}

.user-login-status {
  padding: 0 var(--spacing-md);
}

.user-login-status :deep(.ant-space) {
  cursor: pointer;
  padding: 8px 12px;
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);
}

.user-login-status :deep(.ant-space:hover) {
  background: rgba(3, 105, 161, 0.08);
}

.user-login-status :deep(.ant-avatar) {
  background: linear-gradient(135deg, var(--primary-blue), var(--accent-cyan));
  border: 2px solid var(--border-light);
}

.user-login-status :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, var(--primary-blue), var(--accent-cyan));
  border: none;
  border-radius: var(--radius-lg);
  font-weight: 500;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.user-login-status :deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

:deep(.ant-dropdown-menu) {
  border-radius: var(--radius-lg);
  padding: var(--spacing-sm);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-light);
}

:deep(.ant-dropdown-menu-item) {
  border-radius: var(--radius-md);
  padding: 10px 16px;
  transition: all var(--transition-fast);
}

:deep(.ant-dropdown-menu-item:hover) {
  background: rgba(3, 105, 161, 0.08);
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .header {
    padding: 0 var(--spacing-md);
  }

  .site-title {
    font-size: 16px;
  }

  .logo {
    height: 32px;
    width: 32px;
  }
}
</style>
