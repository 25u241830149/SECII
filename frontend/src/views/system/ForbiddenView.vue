<template>
  <div class="page-center">
    <div class="page-center-card">
      <h1 class="section-title">无权访问</h1>
      <p class="panel-description">该页面需要管理员权限，当前骨架已在路由守卫中做基础拦截。</p>
      <div class="form-actions" style="justify-content: center;">
        <RouterLink to="/">
          <el-button type="primary" round>返回首页</el-button>
        </RouterLink>
      </div>
    </div>
  </div>
</template>