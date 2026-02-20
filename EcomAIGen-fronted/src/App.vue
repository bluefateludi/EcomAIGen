<script setup lang="ts">
import { ConfigProvider, theme } from 'ant-design-vue'
import BasicLayout from '@/layouts/BasicLayout.vue'
import { antdTheme } from '@/styles/antd-theme'
</script>

<template>
  <ConfigProvider :theme="antdTheme">
    <BasicLayout />
  </ConfigProvider>
</template>

<style>
/* Minimal global overrides - most styles are in theme.css */
html {
  overflow-x: hidden;
}
</style>
