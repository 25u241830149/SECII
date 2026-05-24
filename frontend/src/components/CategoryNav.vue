<template>
  <div class="category-nav">
    <button
      v-for="item in categories"
      :key="item.value"
      type="button"
      :class="{ active: modelValue === item.value }"
      @click="$emit('update:modelValue', item.value)"
    >
      <span>{{ item.icon }}</span>
      <strong>{{ item.label }}</strong>
    </button>
  </div>
</template>

<script setup lang="ts">
import type { TaskCategory } from '@/types'

defineProps<{
  modelValue: TaskCategory | 'ALL'
}>()

defineEmits<{
  'update:modelValue': [value: TaskCategory | 'ALL']
}>()

const categories: Array<{ value: TaskCategory | 'ALL'; label: string; icon: string }> = [
  { value: 'ALL', label: '全部', icon: '◌' },
  { value: 'EXPRESS', label: '快递代取', icon: '➜' },
  { value: 'STUDY', label: '学习辅导', icon: '✦' },
  { value: 'SECOND_HAND', label: '二手交易', icon: '◈' },
  { value: 'TEAM_UP', label: '组队匹配', icon: '◎' },
  { value: 'OTHER', label: '其他', icon: '•' },
]
</script>

<style scoped>
.category-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.category-nav button {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 40px;
  padding: 0 14px;
  border: 1px solid #dbe6f4;
  border-radius: 999px;
  background: #fff;
  color: #4b5563;
  font: inherit;
  cursor: pointer;
}

.category-nav button.active {
  border-color: #1d4ed8;
  background: #eaf1ff;
  color: #1d4ed8;
}

.category-nav span {
  font-size: 16px;
}
</style>
