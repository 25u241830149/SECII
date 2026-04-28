<template>
  <section class="page-surface">
    <div class="section-heading">
      <div>
        <h1 class="section-title">我的足迹</h1>
        <p class="section-subtitle">当前先用前端聚合视图展示浏览、接单和互动记录。</p>
      </div>
    </div>

    <el-timeline>
      <el-timeline-item v-for="item in timeline" :key="item.title" :timestamp="item.time" placement="top">
        <div class="timeline-item">
          <strong>{{ item.title }}</strong>
          <p class="muted" style="margin-top: 8px;">{{ item.description }}</p>
        </div>
      </el-timeline-item>
    </el-timeline>
  </section>
</template>

<script setup lang="ts">
const timeline = [
  {
    title: '浏览了快递代取任务',
    description: '你查看了“今晚帮忙代取东区驿站快递”的详情页。',
    time: '今天 18:30',
  },
  {
    title: '完成一笔学习辅导订单',
    description: '订单已进入待评价状态，可前往评价页继续。',
    time: '昨天 21:10',
  },
  {
    title: '收藏了一篇论坛帖子',
    description: '已加入“求推荐期末复习时间管理方法”的收藏列表。',
    time: '3 天前',
  },
]
</script>