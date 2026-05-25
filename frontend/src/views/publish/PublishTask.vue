<template>
  <section class="publish-page">
    <article class="publish-card">
      <div class="header">
        <div>
          <p class="eyebrow">Publish Task</p>
          <h1>发布任务</h1>
          <span>按模板填写核心信息，提交后会立即进入任务流。</span>
        </div>
        <aside class="template-tip">
          <strong>{{ templateTitle }}</strong>
          <p>{{ templateTip }}</p>
        </aside>
      </div>

      <el-form :model="form" label-position="top" @submit.prevent>
        <div class="form-grid">
          <el-form-item label="任务分类">
            <el-select v-model="form.category">
              <el-option label="快递代取" value="EXPRESS" />
              <el-option label="学习辅导" value="STUDY" />
              <el-option label="二手交易" value="SECOND_HAND" />
              <el-option label="组队匹配" value="TEAM_UP" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>

          <el-form-item label="报酬">
            <el-input-number v-model="form.reward" :min="0" :step="1" :precision="2" />
          </el-form-item>

          <el-form-item class="span-2" label="标题">
            <el-input v-model="form.title" maxlength="128" show-word-limit />
          </el-form-item>

          <el-form-item class="span-2" label="详细描述">
            <el-input v-model="form.description" type="textarea" :rows="6" maxlength="800" show-word-limit />
          </el-form-item>

          <el-form-item class="span-2" label="地点">
            <el-input v-model="form.location" placeholder="例如：南门菜鸟驿站 / 理学楼 307" />
          </el-form-item>
        </div>

        <div class="actions">
          <el-button @click="router.back()">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">发布任务</el-button>
        </div>
      </el-form>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import { createTask } from '@/api/task'
import type { TaskCategory, TaskMutationPayload } from '@/types'

const router = useRouter()
const submitting = ref(false)

const form = reactive<TaskMutationPayload>({
  title: '',
  description: '',
  category: 'EXPRESS',
  reward: 0,
  location: '',
})

const templateTipMap: Record<TaskCategory, { title: string; tip: string }> = {
  EXPRESS: {
    title: '快递代取模板',
    tip: '建议写清取件点、宿舍楼和时间窗口，能明显提升接单效率。',
  },
  STUDY: {
    title: '学习辅导模板',
    tip: '描述课程、知识点和预期时长，方便匹配真正能解决问题的人。',
  },
  SECOND_HAND: {
    title: '二手交易模板',
    tip: '补充成色、交易方式和是否支持当面验货，减少沟通成本。',
  },
  TEAM_UP: {
    title: '组队匹配模板',
    tip: '写清活动时间、人数缺口和水平要求，能更快招到合适队友。',
  },
  OTHER: {
    title: '通用任务模板',
    tip: '标题尽量直白，描述里写清交付结果、地点和时效要求。',
  },
}

const templateTitle = computed(() => templateTipMap[form.category].title)
const templateTip = computed(() => templateTipMap[form.category].tip)

const handleSubmit = async () => {
  submitting.value = true
  try {
    const detail = await createTask(form)
    ElMessage.success('任务已发布')
    router.replace(`/tasks/${detail.taskId}`)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.publish-page {
  display: grid;
}

.publish-card {
  padding: 28px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.08);
}

.header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 18px;
  margin-bottom: 24px;
}

.eyebrow {
  margin: 0;
  color: #0f766e;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.header h1 {
  margin: 10px 0 8px;
  font-size: 32px;
}

.header span,
.template-tip p {
  color: #64748b;
}

.template-tip {
  padding: 18px;
  border-radius: 8px;
  background: linear-gradient(145deg, #eff6ff, #fff);
}

.template-tip strong {
  color: #1d4ed8;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.span-2 {
  grid-column: 1 / -1;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

@media (max-width: 900px) {
  .header,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .span-2 {
    grid-column: auto;
  }
}
</style>
