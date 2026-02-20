<template>
  <div class="app-card" :class="{ 'app-card--featured': featured }">
    <div class="app-preview">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
      <div v-else class="app-placeholder">
        <span>🤖</span>
      </div>
      <div class="app-overlay">
        <a-space>
          <a-button type="primary" @click="handleViewChat">查看对话</a-button>
          <a-button v-if="app.deployKey" type="default" @click="handleViewWork">查看作品</a-button>
        </a-space>
      </div>
    </div>
    <div class="app-info">
      <div class="app-info-left">
        <a-avatar :src="userAvatarSrc" :size="40">
          {{ userInitial }}
        </a-avatar>
      </div>
      <div class="app-info-right">
        <h3 class="app-title">{{ app.appName || '未命名应用' }}</h3>
        <p class="app-author">
          {{ app.user?.userName || (featured ? '官方' : '未知用户') }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getUserAvatar, getUserInitial } from '@/utils/avatar'

interface Props {
  app: API.AppVO
  featured?: boolean
}

interface Emits {
  (e: 'view-chat', appId: string | number | undefined): void
  (e: 'view-work', app: API.AppVO): void
}

const props = withDefaults(defineProps<Props>(), {
  featured: false,
})

const emit = defineEmits<Emits>()

// Computed avatar properties
const userAvatarSrc = computed(() => getUserAvatar(props.app.user?.userAvatar))
const userInitial = computed(() => getUserInitial(props.app.user?.userName))

const handleViewChat = () => {
  emit('view-chat', props.app.id)
}

const handleViewWork = () => {
  emit('view-work', props.app)
}
</script>

<style scoped>
.app-card {
  background: var(--bg-glass);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-glass);
  border: 1px solid var(--border-light);
  transition: all var(--transition-normal);
  cursor: pointer;
  position: relative;
}

.app-card--featured {
  border-color: var(--primary-blue);
  box-shadow: 0 8px 32px rgba(3, 105, 161, 0.15);
}

.app-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-glass-hover);
}

.app-card--featured:hover {
  box-shadow: 0 12px 48px rgba(3, 105, 161, 0.25);
}

.app-preview {
  height: 200px;
  background: var(--bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.app-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.app-card:hover .app-preview img {
  transform: scale(1.05);
}

.app-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--bg-tertiary) 0%, var(--bg-secondary) 100%);
  font-size: 56px;
}

.app-placeholder span {
  opacity: 0.5;
  filter: grayscale(0.3);
}

.app-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.app-card:hover .app-overlay {
  opacity: 1;
}

.app-overlay :deep(.ant-space) {
  display: flex;
  gap: 12px;
}

.app-overlay :deep(.ant-btn) {
  border-radius: var(--radius-lg);
  font-weight: 500;
  padding: 8px 20px;
  height: auto;
}

.app-overlay :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, var(--primary-blue), var(--accent-cyan));
  border: none;
}

.app-overlay :deep(.ant-btn-default) {
  background: var(--bg-primary);
  border-color: var(--border-medium);
  color: var(--text-primary);
}

.app-overlay :deep(.ant-btn-default:hover) {
  background: var(--bg-secondary);
  border-color: var(--primary-blue);
  color: var(--primary-blue);
}

.app-info {
  padding: var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  background: var(--bg-primary);
}

.app-info-left {
  flex-shrink: 0;
}

.app-info-left :deep(.ant-avatar) {
  background: linear-gradient(135deg, var(--primary-blue), var(--accent-cyan));
  color: var(--text-white);
  font-weight: 600;
  border: 2px solid var(--border-light);
}

.app-info-right {
  flex: 1;
  min-width: 0;
}

.app-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px;
  color: var(--text-primary);
  font-family: var(--font-heading, 'Poppins', sans-serif);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-author {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: var(--font-body, 'Open Sans', sans-serif);
}

/* Featured badge */
.app-card--featured::before {
  content: '精选';
  position: absolute;
  top: 12px;
  right: 12px;
  background: linear-gradient(135deg, var(--primary-blue), var(--accent-cyan));
  color: var(--text-white);
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
  z-index: 2;
  box-shadow: var(--shadow-md);
}
</style>
