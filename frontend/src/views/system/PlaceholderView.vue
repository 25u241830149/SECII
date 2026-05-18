<template>
  <section class="placeholder-view">
    <p>{{ route.meta.module || 'CampusHub' }}</p>
    <h1>{{ route.meta.title || '功能建设中' }}</h1>
    <span>当前路由与布局已接入，后续 Sprint 可在这里继续补齐业务页面。</span>
  </section>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<style scoped>
.placeholder-view {
  min-height: 360px;
  padding: 34px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.055);
}

p {
  margin: 0 0 8px;
  color: #1268ed;
  font-weight: 800;
}

h1 {
  margin: 0 0 12px;
  color: #111827;
  font-size: 28px;
}

span {
  color: #687386;
}
</style>
