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

          <el-form-item :label="rewardLabel">
            <el-input-number v-model="form.reward" :min="0" :step="1" :precision="2" />
          </el-form-item>

          <el-form-item class="span-2" label="标题">
            <el-input v-model="form.title" maxlength="128" show-word-limit />
          </el-form-item>

          <el-form-item class="span-2" label="详细描述">
            <el-input v-model="form.description" type="textarea" :rows="6" maxlength="800" show-word-limit />
          </el-form-item>

          <el-form-item v-if="form.category !== 'TEAM_UP'" class="span-2" :label="form.category === 'SECOND_HAND' ? '交易地点' : '地点'">
            <el-input v-model="form.location" placeholder="例如：南门菜鸟驿站 / 理学楼 307" />
          </el-form-item>

          <template v-if="form.category === 'SECOND_HAND'">
            <el-form-item label="物品原价">
              <el-input-number v-model="form.originalPrice" :min="0" :step="1" :precision="2" />
            </el-form-item>
            <el-form-item class="span-2" label="物品图片">
              <div class="image-upload-row">
                <el-upload
                  :auto-upload="false"
                  :show-file-list="false"
                  accept="image/jpeg,image/png,image/webp"
                  @change="handleTaskImageChange"
                >
                  <el-button :loading="uploadingImage">上传图片</el-button>
                </el-upload>
                <img v-if="form.itemImageUrl" :src="resolveAssetUrl(form.itemImageUrl)" alt="物品图片预览" />
                <span v-else>支持 JPG、PNG、WebP，上传后会在详情页展示。</span>
              </div>
            </el-form-item>
          </template>

          <template v-if="form.category === 'TEAM_UP'">
            <el-form-item label="团队总人数">
              <el-input-number v-model="form.teamTotalMembers" :min="1" :step="1" />
            </el-form-item>
            <el-form-item label="当前已有成员人数">
              <el-input-number v-model="form.teamCurrentMembers" :min="0" :step="1" />
            </el-form-item>
            <el-form-item label="活动时间">
              <el-date-picker
                v-model="form.activityTime"
                type="datetime"
                placeholder="选择活动时间"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="活动地点">
              <el-input v-model="form.location" placeholder="例如：东区体育馆 / 操场" />
            </el-form-item>
            <el-form-item class="span-2" label="活动说明 / 要求">
              <el-input v-model="form.activityNote" type="textarea" :rows="4" maxlength="800" show-word-limit />
            </el-form-item>
          </template>
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
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadFile, UploadProps } from 'element-plus'
import { useRouter } from 'vue-router'

import { createTask } from '@/api/task'
import { uploadTaskImage } from '@/api/upload'
import { resolveAssetUrl } from '@/utils/asset'
import type { TaskCategory, TaskMutationPayload } from '@/types'

const router = useRouter()
const submitting = ref(false)
const uploadingImage = ref(false)

const form = reactive<TaskMutationPayload>({
  title: '',
  description: '',
  category: 'EXPRESS',
  reward: 0,
  location: '',
  itemImageUrl: null,
  originalPrice: null,
  teamTotalMembers: 2,
  teamCurrentMembers: 1,
  activityTime: null,
  activityNote: '',
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
const rewardLabel = computed(() => (form.category === 'SECOND_HAND' ? '现价' : '报酬'))

watch(
  () => form.category,
  (category) => {
    if (category !== 'SECOND_HAND') {
      form.itemImageUrl = null
      form.originalPrice = null
    }
    if (category !== 'TEAM_UP') {
      form.teamTotalMembers = null
      form.teamCurrentMembers = null
      form.activityTime = null
      form.activityNote = null
    } else {
      form.teamTotalMembers = form.teamTotalMembers ?? 2
      form.teamCurrentMembers = form.teamCurrentMembers ?? 1
    }
  },
)

const handleTaskImageChange: UploadProps['onChange'] = async (uploadFile: UploadFile) => {
  const rawFile = uploadFile.raw
  if (!rawFile) return
  if (!rawFile.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }

  uploadingImage.value = true
  try {
    const result = await uploadTaskImage(rawFile)
    form.itemImageUrl = result.fileUrl
    ElMessage.success('图片已上传')
  } finally {
    uploadingImage.value = false
  }
}

const buildPayload = (): TaskMutationPayload => ({
  title: form.title,
  description: form.description,
  category: form.category,
  reward: form.reward,
  location: form.location,
  itemImageUrl: form.category === 'SECOND_HAND' ? form.itemImageUrl || null : null,
  originalPrice: form.category === 'SECOND_HAND' ? form.originalPrice ?? null : null,
  teamTotalMembers: form.category === 'TEAM_UP' ? form.teamTotalMembers ?? null : null,
  teamCurrentMembers: form.category === 'TEAM_UP' ? form.teamCurrentMembers ?? null : null,
  activityTime: form.category === 'TEAM_UP' ? form.activityTime || null : null,
  activityNote: form.category === 'TEAM_UP' ? form.activityNote || null : null,
})

const handleSubmit = async () => {
  submitting.value = true
  try {
    const detail = await createTask(buildPayload())
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
  height: calc(100vh - 125px);
  min-height: 0;
}

.publish-card {
  display: flex;
  min-height: 0;
  flex-direction: column;
  overflow: hidden;
  padding: 28px;
  border: 1px solid #e7edf7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.08);
}

.publish-card :deep(.el-form) {
  min-height: 0;
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;
  scrollbar-gutter: stable;
}

.publish-card :deep(.el-form::-webkit-scrollbar) {
  width: 8px;
}

.publish-card :deep(.el-form::-webkit-scrollbar-thumb) {
  border-radius: 999px;
  background: #cbd5e1;
}

.publish-card :deep(.el-form::-webkit-scrollbar-track) {
  background: transparent;
}

.header {
  flex: 0 0 auto;
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

.image-upload-row {
  display: flex;
  align-items: center;
  gap: 14px;
}

.image-upload-row img {
  width: 96px;
  height: 72px;
  border-radius: 8px;
  object-fit: cover;
}

.image-upload-row span {
  color: #64748b;
  font-size: 13px;
}

.actions {
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
  padding-top: 12px;
  background: #fff;
}

@media (max-width: 900px) {
  .publish-page {
    height: auto;
  }

  .publish-card {
    overflow: visible;
  }

  .publish-card :deep(.el-form) {
    overflow: visible;
    padding-right: 0;
  }

  .header,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .span-2 {
    grid-column: auto;
  }
}
</style>
