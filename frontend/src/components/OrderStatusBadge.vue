<template>
  <span :class="['status-badge', tone]">
    {{ orderStatusLabels[status] }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import { orderStatusLabels } from '@/types'
import type { OrderStatus } from '@/types'

const props = defineProps<{
  status: OrderStatus
}>()

const tone = computed(() => {
  switch (props.status) {
    case 'CONFIRMED':
      return 'green'
    case 'COMPLETED':
      return 'blue'
    case 'CANCELLED':
      return 'red'
    case 'WAITING_REVIEW':
      return 'purple'
    default:
      return 'orange'
  }
})
</script>

<style scoped>
.status-badge {
  display: inline-flex;
  min-height: 30px;
  align-items: center;
  justify-content: center;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
}

.status-badge.orange {
  background: #fff4e5;
  color: #b45309;
}

.status-badge.green {
  background: #eaf8ef;
  color: #15803d;
}

.status-badge.blue {
  background: #eaf1ff;
  color: #1d4ed8;
}

.status-badge.red {
  background: #fff1f2;
  color: #dc2626;
}

.status-badge.purple {
  background: #f3efff;
  color: #7c3aed;
}
</style>
