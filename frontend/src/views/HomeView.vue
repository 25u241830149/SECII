<template>
  <main class="home-view">
    <section class="welcome-panel">
      <p class="eyebrow">CampusHub</p>
      <h1>校园互助服务平台</h1>
      <p class="summary">前端基础脚手架已就绪。</p>
      <el-button type="primary">开始开发</el-button>
    </section>
  </main>
</template>

<style scoped>
.home-view {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: 32px;
}

.welcome-panel {
  width: min(100%, 560px);
  padding: 32px;
  border: 1px solid #d8dee8;
  border-radius: 8px;
  background: #ffffff;
}

.eyebrow {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 14px;
  font-weight: 700;
}

h1 {
  margin: 0;
  font-size: 32px;
  line-height: 1.25;
}

.summary {
  margin: 12px 0 24px;
  color: #667085;
}
</style>
