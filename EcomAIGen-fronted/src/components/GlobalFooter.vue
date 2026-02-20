<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <p class="copyright">
        <a
          href="https://github.com/bluefateludi/EcomAIGen"
          target="_blank"
          rel="noopener noreferrer"
          class="author-link"
        >
          电商网站生成项目 by bluefateludi
        </a>
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的响应式数据
</script>

<style scoped>
.footer {
  background: var(--bg-glass);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  text-align: center;
  padding: var(--spacing-xl);
  margin-top: var(--spacing-2xl);
  border-top: 1px solid var(--border-light);
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
}

.copyright {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
  font-family: var(--font-body, 'Open Sans', sans-serif);
}

.author-link {
  color: var(--primary-blue);
  text-decoration: none;
  font-weight: 500;
  transition: color var(--transition-fast);
  background: linear-gradient(135deg, var(--primary-blue), var(--accent-cyan));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.author-link:hover {
  opacity: 0.8;
}
</style>
