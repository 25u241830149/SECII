<template>
  <main class="help-manual-page">
    <header class="help-manual-header">
      <RouterLink class="help-brand" to="/login">
        <CampusHubLogo size="md" />
        <div>
          <strong>CampusHub {{ helpDocument.eyebrow }}</strong>
          <span>校园互助平台参考手册</span>
        </div>
      </RouterLink>

      <RouterLink class="back-link" to="/login">返回登录</RouterLink>
    </header>

    <section class="help-manual-hero help-manual-hero--auth-surface">
      <figure class="help-manual-hero-illustration" aria-hidden="true">
        <img :src="authIllustration" alt="" />
      </figure>
      <p class="hero-eyebrow">{{ helpDocument.eyebrow }}</p>
      <h1>{{ helpDocument.title }}</h1>
      <span>{{ helpDocument.summary }}</span>
      <div class="hero-tags">
        <span>注册与认证</span>
        <span>任务与订单</span>
        <span>信用与举报</span>
      </div>
    </section>

    <section class="help-manual-shell">
      <aside class="help-manual-sidebar">
        <div class="sidebar-card">
          <p>目录</p>
          <RouterLink
            v-for="section in indexedSections"
            :key="section.id"
            class="help-toc-link"
            :class="{ active: activeSectionId === section.id }"
            :to="{ name: route.name, hash: `#${section.id}` }"
          >
            {{ section.title }}
          </RouterLink>
        </div>
      </aside>

      <article class="help-manual-content">
        <section
          v-for="section in indexedSections"
          :id="section.id"
          :key="section.id"
          class="help-manual-section"
        >
          <h2>{{ section.title }}</h2>

          <p v-for="paragraph in section.paragraphs" :key="paragraph" class="section-paragraph">
            {{ paragraph }}
          </p>

          <ol v-if="section.steps?.length" class="section-list ordered">
            <li v-for="step in section.steps" :key="step">{{ step }}</li>
          </ol>

          <ul v-if="section.bullets?.length" class="section-list">
            <li v-for="bullet in section.bullets" :key="bullet">{{ bullet }}</li>
          </ul>

          <div v-if="section.table" class="section-table">
            <table>
              <thead>
                <tr>
                  <th v-for="header in section.table.headers" :key="header" scope="col">{{ header }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in section.table.rows" :key="row.join('-')">
                  <td v-for="cell in row" :key="cell">{{ cell }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </article>
    </section>

    <LegalDocumentDialog
      :model-value="isLegalDialogOpen"
      :document-key="legalDialogKey"
      @close="closeLegalDialog"
    />
  </main>
</template>

<script setup lang="ts">
import { computed, nextTick, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import authIllustration from '@/assets/illustation.png'
import CampusHubLogo from '@/components/CampusHubLogo.vue'

import LegalDocumentDialog from './LegalDocumentDialog.vue'
import { publicDocuments, type PublicDocumentKey } from './publicDocuments'

const route = useRoute()
const router = useRouter()

const helpDocument = computed(() => {
  const key = route.meta.publicDocumentKey as PublicDocumentKey | undefined
  return key ? publicDocuments[key] : publicDocuments['help-center']
})

const slugify = (title: string) =>
  title
    .toLowerCase()
    .replace(/[^\w\u4e00-\u9fa5]+/g, '-')
    .replace(/^-+|-+$/g, '')

const indexedSections = computed(() =>
  helpDocument.value.sections.map((section, index) => ({
    ...section,
    id: `section-${index + 1}-${slugify(section.title)}`,
  })),
)

const normalizeHash = (hash: string) => decodeURIComponent(hash.replace(/^#/, ''))

const activeSectionId = computed(() => normalizeHash(route.hash) || indexedSections.value[0]?.id || '')

const legalDialogKey = computed<PublicDocumentKey | null>(() => {
  const dialog = route.query.dialog
  return dialog === 'privacy-policy' || dialog === 'terms-of-service' ? dialog : null
})

const isLegalDialogOpen = computed(() => legalDialogKey.value !== null)

const scrollToSection = async (hash: string) => {
  const sectionId = normalizeHash(hash)
  if (!sectionId) {
    return
  }

  await nextTick()
  document.getElementById(sectionId)?.scrollIntoView({
    block: 'start',
    behavior: 'smooth',
  })
}

watch(
  () => route.hash,
  (hash) => {
    void scrollToSection(hash)
  },
  { immediate: true },
)

const closeLegalDialog = () => {
  const nextQuery = { ...route.query }
  delete nextQuery.dialog
  router.push({
    path: route.path,
    hash: route.hash,
    query: nextQuery,
  })
}
</script>

<style scoped>
.help-manual-page {
  min-height: 100vh;
  padding: 28px;
  background:
    radial-gradient(circle at top left, rgba(57, 118, 255, 0.12), transparent 24%),
    linear-gradient(180deg, #f6f9ff 0%, #fbfcff 100%);
}

.help-manual-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  width: min(100%, 1380px);
  margin: 0 auto 20px;
}

.help-brand {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  color: inherit;
  text-decoration: none;
}

.help-brand strong,
.help-brand span {
  display: block;
}

.help-brand strong {
  color: #0f172a;
  font-size: 22px;
  font-weight: 800;
}

.help-brand span {
  color: #64748b;
  margin-top: 4px;
  font-size: 14px;
}

.back-link {
  color: #1268ed;
  font-weight: 700;
  text-decoration: none;
}

.help-manual-hero,
.help-manual-shell {
  width: min(100%, 1380px);
  margin: 0 auto;
}

.help-manual-hero {
  position: relative;
  overflow: hidden;
  min-height: 340px;
  padding: 34px 38px;
  border: 1px solid rgba(216, 225, 239, 0.9);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24px 72px rgba(15, 23, 42, 0.1);
}

.help-manual-hero--auth-surface::before,
.help-manual-hero--auth-surface::after {
  position: absolute;
  inset: 0;
  content: "";
  pointer-events: none;
}

.help-manual-hero--auth-surface::before {
  background:
    radial-gradient(circle at 12% 20%, rgba(28, 111, 255, 0.1), transparent 24%),
    radial-gradient(circle at 82% 18%, rgba(125, 85, 255, 0.08), transparent 22%);
}

.help-manual-hero--auth-surface::after {
  background: none;
}

.help-manual-hero-illustration {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 55%;
  margin: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
  border-radius: 0 28px 28px 0;
}

.help-manual-hero-illustration::before,
.help-manual-hero-illustration::after {
  position: absolute;
  inset: 0;
  z-index: 1;
  content: "";
  pointer-events: none;
}

.help-manual-hero-illustration::before {
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0.5) 20%, rgba(255, 255, 255, 0) 50%);
}

.help-manual-hero-illustration::after {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.48) 0%, rgba(255, 255, 255, 0.06) 20%, rgba(255, 255, 255, 0) 44%),
    linear-gradient(0deg, rgba(255, 255, 255, 0.54) 0%, rgba(255, 255, 255, 0.06) 20%, rgba(255, 255, 255, 0) 44%);
}

.help-manual-hero-illustration img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center center;
  opacity: 0.92;
  filter: saturate(0.96) brightness(1.02);
}

.help-manual-hero > *:not(figure) {
  position: relative;
  z-index: 1;
}

.hero-eyebrow {
  margin: 0 0 10px;
  color: #1268ed;
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 0.04em;
}

.help-manual-hero h1 {
  margin: 0;
  color: #0f172a;
  font-size: 42px;
  line-height: 1.15;
}

.help-manual-hero > span {
  display: block;
  max-width: 860px;
  margin-top: 16px;
  color: #52627a;
  font-size: 18px;
  line-height: 1.75;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 24px;
}

.hero-tags span {
  padding: 8px 14px;
  border: 1px solid #dbe7ff;
  border-radius: 999px;
  background: #f7faff;
  color: #1d4ed8;
  font-size: 13px;
  font-weight: 700;
}

.help-manual-shell {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 26px;
  align-items: start;
  margin-top: 22px;
}

.help-manual-sidebar {
  position: sticky;
  top: 28px;
}

.sidebar-card,
.help-manual-content {
  border: 1px solid #dce6f5;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.06);
}

.sidebar-card {
  display: grid;
  gap: 8px;
  padding: 20px;
  border-radius: 24px;
}

.sidebar-card p {
  margin: 0 0 4px;
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.help-toc-link {
  padding: 10px 12px;
  border-radius: 14px;
  color: #475569;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.5;
  text-decoration: none;
  transition:
    background 0.2s ease,
    color 0.2s ease,
    transform 0.2s ease;
}

.help-toc-link:hover,
.help-toc-link.active {
  background: #eff6ff;
  color: #1d4ed8;
  transform: translateX(2px);
}

.help-manual-content {
  padding: 30px 34px;
  border-radius: 28px;
}

.help-manual-section + .help-manual-section {
  margin-top: 34px;
  padding-top: 34px;
  border-top: 1px solid #e9eef6;
}

.help-manual-section h2 {
  margin: 0 0 14px;
  color: #0f172a;
  font-size: 28px;
  line-height: 1.3;
}

.section-paragraph {
  margin: 0;
  color: #475569;
  font-size: 17px;
  line-height: 1.9;
}

.section-paragraph + .section-paragraph {
  margin-top: 10px;
}

.section-list {
  margin: 14px 0 0;
  padding-left: 24px;
  color: #475569;
  font-size: 17px;
  line-height: 1.9;
}

.ordered {
  padding-left: 26px;
}

.section-table {
  margin-top: 16px;
  overflow-x: auto;
}

.section-table table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #dbe4f1;
  border-radius: 14px;
  overflow: hidden;
}

.section-table th,
.section-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #dbe4f1;
  text-align: left;
  vertical-align: top;
}

.section-table th {
  background: #f4f8ff;
  color: #1e3a8a;
  font-size: 14px;
  font-weight: 700;
}

.section-table td {
  color: #475569;
  font-size: 15px;
}

.section-table tr:last-child td {
  border-bottom: 0;
}

@media (max-width: 1080px) {
  .help-manual-shell {
    grid-template-columns: 1fr;
  }

  .help-manual-sidebar {
    position: static;
  }

  .sidebar-card {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .sidebar-card p {
    grid-column: 1 / -1;
  }
}

@media (max-width: 720px) {
  .help-manual-page {
    padding: 16px;
  }

  .help-manual-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .help-manual-hero {
    min-height: 280px;
    padding: 24px 22px;
    border-radius: 22px;
  }

  .help-manual-hero-illustration {
    right: 18px;
    top: 18px;
    width: 40%;
    height: 40%;
    border-radius: 18px;
  }

  .help-manual-hero h1 {
    font-size: 30px;
  }

  .help-manual-hero > span,
  .section-paragraph,
  .section-list {
    font-size: 15px;
  }

  .sidebar-card {
    grid-template-columns: 1fr;
    padding: 16px;
    border-radius: 20px;
  }

  .help-manual-content {
    padding: 22px 20px;
    border-radius: 22px;
  }

  .help-manual-section h2 {
    font-size: 24px;
  }
}
</style>
