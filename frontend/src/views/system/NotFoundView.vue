<template>
  <main class="system-page">
    <p>404</p>
    <h1>页面不存在</h1>
    <RouterLink to="/">返回首页</RouterLink>
  </main>
</template>

<style scoped>
.system-page {
  display: grid;
  min-height: 100vh;
  place-content: center;
  gap: 12px;
  background: #f5f7fb;
  text-align: center;
}

p {
  margin: 0;
  color: #1268ed;
  font-size: 72px;
  font-weight: 900;
}

h1 {
  margin: 0;
  color: #111827;
}

a {
  color: #1268ed;
  font-weight: 700;
  text-decoration: none;
}
</style>
