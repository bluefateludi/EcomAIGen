import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { ConfigProvider } from 'ant-design-vue'

import App from './App.vue'
import router from './router'

import Antd from 'ant-design-vue'

// Custom theme imports
import '@/styles/theme.css'
import '@/styles/variables.css'
import { antdTheme } from '@/styles/antd-theme'

import '@/access'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)

// Configure Ant Design theme
app.provide('antd-theme', antdTheme)

// Add ConfigProvider as a global component
app.component('AConfigProvider', ConfigProvider)

app.mount('#app')
