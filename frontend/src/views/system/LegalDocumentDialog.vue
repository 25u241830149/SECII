<template>
  <el-dialog
    :model-value="modelValue"
    width="min(700px, calc(100vw - 48px))"
    top="14vh"
    :teleported="false"
    destroy-on-close
    class="public-legal-dialog"
    @close="$emit('close')"
  >
    <div class="legal-dialog-body">
      <PublicDocumentContent v-if="document" :document="document" />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import PublicDocumentContent from './PublicDocumentContent.vue'
import { publicDocuments, type PublicDocumentKey } from './publicDocuments'

const props = defineProps<{
  modelValue: boolean
  documentKey: PublicDocumentKey | null
}>()

defineEmits<{
  close: []
}>()

const document = computed(() => (props.documentKey ? publicDocuments[props.documentKey] : null))
</script>

<style>
.legal-dialog-body {
  max-height: min(68vh, 720px);
  overflow-y: auto;
  padding-right: 4px;
  scrollbar-gutter: stable;
}

.public-legal-dialog .el-dialog__body {
  padding-top: 8px;
}

.public-legal-dialog.el-dialog {
  overflow: hidden;
  border: 1px solid rgba(226, 232, 240, 0.82);
  border-radius: 36px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.99) 0%, rgba(248, 251, 255, 0.98) 100%);
  box-shadow:
    0 20px 48px rgba(15, 23, 42, 0.05),
    0 56px 160px rgba(15, 23, 42, 0.12);
}

.public-legal-dialog .el-dialog__header {
  padding: 10px 16px 0;
  background: transparent;
  border-bottom: 0;
}

.public-legal-dialog .el-dialog__title {
  color: #0f172a;
  font-size: 24px;
  font-weight: 800;
  line-height: 1.35;
}

.public-legal-dialog .el-dialog__body {
  padding: 8px 36px 36px;
}

@media (max-width: 720px) {
  .legal-dialog-body {
    padding-right: 0;
  }

  .public-legal-dialog.el-dialog {
    width: min(calc(100vw - 20px), 700px);
    border-radius: 32px;
  }

  .public-legal-dialog .el-dialog__body {
    padding: 8px 22px 24px;
  }
}
</style>
