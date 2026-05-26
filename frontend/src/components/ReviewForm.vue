<template>
  <form class="review-form" @submit.prevent="submitReview">
    <header>
      <h2>评价 {{ targetUserName }}</h2>
      <p>订单完成后双方各可评价一次，评分会同步影响信用分。</p>
    </header>

    <label class="field">
      <span>评分</span>
      <el-rate v-model="rating" :max="5" />
    </label>

    <label class="field">
      <span>评价内容</span>
      <el-input
        v-model="content"
        type="textarea"
        :rows="4"
        maxlength="500"
        show-word-limit
        placeholder="描述本次协作体验"
      />
    </label>

    <div class="actions">
      <el-button type="primary" native-type="submit" :loading="submitting">提交评价</el-button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

import { createReview } from '@/api/review'
import type { EntityId, ReviewDTO } from '@/types'

const props = defineProps<{
  orderId: EntityId
  targetUserId: EntityId
  targetUserName: string
}>()

const emit = defineEmits<{
  submitted: [review: ReviewDTO]
}>()

const rating = ref(5)
const content = ref('')
const submitting = ref(false)

const submitReview = async () => {
  if (rating.value < 1) {
    ElMessage.warning('请选择评分')
    return
  }

  submitting.value = true
  try {
    const review = await createReview({
      orderId: props.orderId,
      targetUserId: props.targetUserId,
      rating: rating.value,
      content: content.value.trim(),
    })
    ElMessage.success('评价已提交')
    content.value = ''
    rating.value = 5
    emit('submitted', review)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.review-form {
  display: grid;
  gap: 16px;
  padding: 22px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.07);
}

.review-form h2,
.review-form p {
  margin: 0;
}

.review-form h2 {
  color: #111827;
  font-size: 20px;
}

.review-form p {
  margin-top: 5px;
  color: #64748b;
  font-size: 13px;
}

.field {
  display: grid;
  gap: 8px;
}

.field span {
  color: #374151;
  font-weight: 700;
}

.actions {
  display: flex;
  justify-content: flex-end;
}
</style>
