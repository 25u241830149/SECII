<template>
  <div class="public-layout">
    <div class="public-backdrop"></div>
    <section class="auth-hero">
      <span class="eyebrow">CampusHub</span>
      <h1 class="display-title">校园互助，像刷帖一样自然发生。</h1>
      <p class="display-copy">这个前端骨架围绕贴吧式任务信息流来搭建，先把登录、首页、发布、消息、论坛和个人中心的高频路径稳定下来。</p>
      <div class="feature-grid">
        <div class="feature-card">
          <strong>高频入口集中</strong>
          <span>顶部搜索、发布、消息、头像区一次到位。</span>
        </div>
        <div class="feature-card">
          <strong>页面与模块同构</strong>
          <span>views、router、api、store 均按文档拆分，便于并行开发。</span>
        </div>
      </div>
    </section>

    <section class="public-panel">
      <div class="panel-card">
        <RouterView />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>