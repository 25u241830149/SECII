<template>
  <article class="public-document-content">
    <header class="document-header">
      <p>{{ document.eyebrow }}</p>
      <h1>{{ document.title }}</h1>
      <span>{{ document.summary }}</span>

    </header>

    <section v-for="section in document.sections" :key="section.title" class="document-section">
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
</template>

<script setup lang="ts">
import type { PublicDocument } from './publicDocuments'

defineProps<{
  document: PublicDocument
}>()
</script>

<style scoped>
.public-document-content {
  color: #334155;
  line-height: 1.8;
}

.document-header {
  margin-bottom: 28px;
}

.document-header p {
  margin: 0 0 8px;
  color: #1268ed;
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 0.04em;
}

.document-header h1 {
  margin: 0;
  color: #111827;
  font-size: 30px;
  line-height: 1.2;
}

.document-header span {
  display: block;
  margin-top: 12px;
  color: #64748b;
}

.document-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 20px;
  margin-top: 16px;
  color: #475569;
  font-size: 13px;
}

.document-section + .document-section {
  margin-top: 24px;
}

.document-section h2 {
  margin: 0 0 12px;
  color: #0f172a;
  font-size: 18px;
  line-height: 1.45;
}

.section-paragraph {
  margin: 0;
}

.section-paragraph + .section-paragraph {
  margin-top: 10px;
}

.section-list {
  margin: 12px 0 0;
  padding-left: 20px;
}

.ordered {
  padding-left: 22px;
}

.section-table {
  margin-top: 14px;
  overflow-x: auto;
}

.section-table table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #dbe4f1;
  border-radius: 8px;
  overflow: hidden;
}

.section-table th,
.section-table td {
  padding: 12px 14px;
  border-bottom: 1px solid #dbe4f1;
  text-align: left;
  vertical-align: top;
}

.section-table th {
  background: #f4f8ff;
  color: #1e3a8a;
  font-weight: 700;
}

.section-table tr:last-child td {
  border-bottom: 0;
}
</style>
