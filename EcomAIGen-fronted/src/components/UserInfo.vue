<template>
  <div class="user-info">
    <a-avatar :src="avatarSrc" :size="size">
      {{ userInitial }}
    </a-avatar>
    <span v-if="showName" class="user-name">{{ user?.userName || '未知用户' }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getUserAvatar, getUserInitial } from '@/utils/avatar'

interface Props {
  user?: API.UserVO
  size?: number | 'small' | 'default' | 'large'
  showName?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'default',
  showName: true,
})

const avatarSrc = computed(() => getUserAvatar(props.user?.userAvatar))
const userInitial = computed(() => getUserInitial(props.user?.userName))
</script>

<style scoped>
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}
</style>
