<template>
  <div class="page-center">
    <div class="page-center-card">
      <h1 class="section-title">页面不存在</h1>
      <p class="panel-description">当前访问的路径还没有对应页面，可以从首页或个人中心继续浏览。</p>
      <div class="form-actions" style="justify-content: center;">
        <RouterLink to="/">
          <el-button type="primary" round>去首页</el-button>
        </RouterLink>
      </div>
    </div>
  </div>
</template>