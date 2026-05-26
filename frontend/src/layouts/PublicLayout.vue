<template>
  <main class="public-layout">
    <section class="public-card" :class="{ 'public-card--register': isRegisterPage }">
      <figure class="campus-art" aria-hidden="true">
        <img :src="authIllustration" alt="" />
      </figure>

      <div class="brand-panel">
        <RouterLink class="brand" to="/">
          <CampusHubLogo size="lg" />
          <span class="brand-name">CampusHub</span>
          <span class="brand-subtitle">校园互助平台</span>
        </RouterLink>

        <div class="hero-copy">
          <h1>{{ route.meta.publicEyebrow || '欢迎回来' }}</h1>
          <p>{{ route.meta.publicTitle || '校园互助，让学习更轻松，让生活更美好' }}</p>
        </div>

        <div class="feature-strip">
          <div class="feature-item">
            <span class="feature-icon shield">
              <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M12 3.5 18.5 6v5.2c0 4.1-2.6 7.6-6.5 9.3-3.9-1.7-6.5-5.2-6.5-9.3V6L12 3.5Z" />
                <path d="m9.1 12.1 2 2 4-4.2" />
              </svg>
            </span>
            <div>
              <strong>安全可靠</strong>
              <p>多重安全防护，保障账号安全</p>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-icon heart">
              <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M20 8.6c0 4.9-8 9.4-8 9.4s-8-4.5-8-9.4A4.3 4.3 0 0 1 11.2 5l.8.9.8-.9A4.3 4.3 0 0 1 20 8.6Z" />
              </svg>
            </span>
            <div>
              <strong>互助友爱</strong>
              <p>连接校园互助，温暖彼此</p>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-icon bolt">
              <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M13.5 2.8 5.8 13h5.8l-1.1 8.2 7.7-10.4h-5.7l1-8Z" />
              </svg>
            </span>
            <div>
              <strong>高效便捷</strong>
              <p>快速发布需求，及时获得帮助</p>
            </div>
          </div>
        </div>
      </div>

      <section class="auth-panel">
        <RouterView />
      </section>
    </section>

    <footer class="public-footer">
      <span>© 2026 CampusHub 校园互助平台</span>
      <span>帮助中心</span>
      <span>隐私政策</span>
      <span>服务条款</span>
    </footer>
  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

import authIllustration from '@/assets/illustation.png'
import CampusHubLogo from '@/components/CampusHubLogo.vue'

const route = useRoute()
const isRegisterPage = computed(() => route.name === 'register')
</script>

<style scoped>
.public-layout {
  display: grid;
  min-height: 100vh;
  grid-template-rows: 1fr auto;
  padding: 44px;
  background:
    radial-gradient(circle at 12% 16%, rgba(28, 111, 255, 0.14), transparent 26%),
    radial-gradient(circle at 82% 22%, rgba(125, 85, 255, 0.1), transparent 24%),
    #f5f8ff;
}

.public-card {
  position: relative;
  display: grid;
  width: min(100%, 1480px);
  min-height: 704px;
  grid-template-columns: minmax(0, 1.35fr) minmax(420px, 0.75fr);
  gap: 54px;
  align-self: center;
  justify-self: center;
  padding: 58px 68px 46px;
  border: 1px solid rgba(216, 225, 239, 0.9);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24px 72px rgba(15, 23, 42, 0.1);
  overflow: hidden;
}

.public-card::after {
  position: absolute;
  z-index: 1;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.22) 0%, rgba(255, 255, 255, 0.05) 47%, rgba(255, 255, 255, 0.82) 66%, rgba(255, 255, 255, 0.98) 100%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 255, 255, 0.34) 34%, rgba(255, 255, 255, 0.02) 76%);
  content: "";
  pointer-events: none;
}

.brand-panel {
  position: relative;
  z-index: 2;
  display: flex;
  min-width: 0;
  flex-direction: column;
  min-height: 600px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  color: inherit;
  text-decoration: none;
}

.brand-name {
  color: #1168e8;
  font-size: 30px;
  font-weight: 800;
}

.brand-subtitle {
  color: #111827;
  font-size: 23px;
  font-weight: 700;
}

.hero-copy {
  position: relative;
  z-index: 1;
  width: min(100%, 720px);
  margin-top: 54px;
}

.hero-copy h1 {
  margin: 0;
  color: #0f1b33;
  font-size: 60px;
  line-height: 1.08;
  font-weight: 800;
}

.hero-copy p {
  margin: 22px 0 0;
  color: #53627a;
  font-size: 22px;
  font-weight: 600;
}

.campus-art {
  position: absolute;
  z-index: 0;
  bottom: 80px;
  left: 0;
  width: 66%;
  height: 67%;
  margin: 0;
  overflow: hidden;
  pointer-events: none;
}

.campus-art::before,
.campus-art::after {
  position: absolute;
  z-index: 1;
  content: "";
  pointer-events: none;
}

.campus-art::before {
  top: 0;
  right: 0;
  left: 0;
  height: 26%;
  background: linear-gradient(180deg, #fff 0%, rgba(255, 255, 255, 0) 100%);
}

.campus-art::after {
  top: 0;
  right: 0;
  bottom: 0;
  width: 24%;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0) 0%, #fff 100%);
}

.campus-art img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center bottom;
}

.feature-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  width: min(100%, 760px);
  gap: 32px;
  margin-top: auto;
  padding: 0 0 2px;
}

.feature-item {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 12px;
}

.feature-icon {
  display: grid;
  width: 46px;
  height: 46px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 50%;
}

.feature-icon svg {
  width: 24px;
  height: 24px;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.shield {
  color: #1268ed;
  background: #e8f1ff;
}

.heart {
  color: #7c3aed;
  background: #f1eaff;
}

.bolt {
  color: #13a87d;
  background: #e7f7f1;
}

.feature-item strong {
  display: block;
  color: #111827;
  font-size: 15px;
  font-weight: 800;
}

.feature-item p {
  margin: 4px 0 0;
  color: #687386;
  font-size: 13px;
}

.auth-panel {
  position: relative;
  z-index: 2;
  align-self: center;
  box-sizing: border-box;
  height: 650px;
  min-width: 0;
  padding: 42px;
  border: 1px solid #e5ebf4;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 24px 52px rgba(15, 23, 42, 0.1);
}

.public-card--register .auth-panel {
  align-self: center;
  padding: 34px 38px;
  overflow: hidden;
}

.public-card--register .auth-panel :deep(.auth-view) {
  display: flex;
  height: 100%;
  min-height: 0;
  flex-direction: column;
}

.public-card--register .auth-panel :deep(.auth-form) {
  min-height: 0;
  overflow-y: auto;
  padding-right: 8px;
  scrollbar-gutter: stable;
  scrollbar-color: transparent transparent;
  scrollbar-width: thin;
  transition: scrollbar-color 0.2s ease;
}

.public-card--register .auth-panel :deep(.auth-form::-webkit-scrollbar) {
  width: 6px;
}

.public-card--register .auth-panel :deep(.auth-form::-webkit-scrollbar-thumb) {
  border-radius: 999px;
  background: transparent;
  transition: background 0.2s ease;
}

.public-card--register .auth-panel :deep(.auth-form.is-scrolling),
.public-card--register .auth-panel :deep(.auth-form:hover),
.public-card--register .auth-panel :deep(.auth-form:focus-within) {
  scrollbar-color: #b6bfcd transparent;
}

.public-card--register .auth-panel :deep(.auth-form.is-scrolling::-webkit-scrollbar-thumb),
.public-card--register .auth-panel :deep(.auth-form:hover::-webkit-scrollbar-thumb),
.public-card--register .auth-panel :deep(.auth-form:focus-within::-webkit-scrollbar-thumb) {
  background: #b6bfcd;
}

.public-footer {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 18px;
  padding: 22px 0 0;
  color: #7a8495;
  font-size: 13px;
}

@media (max-width: 1120px) {
  .public-layout {
    padding: 24px;
  }

  .public-card {
    grid-template-columns: 1fr;
    gap: 32px;
    padding: 40px;
  }

  .campus-art {
    width: 100%;
    height: 48%;
    opacity: 0.32;
  }

  .auth-panel {
    width: min(100%, 560px);
    justify-self: center;
  }
}

@media (max-width: 720px) {
  .public-layout {
    padding: 12px;
  }

  .public-card {
    padding: 24px;
  }

  .brand {
    flex-wrap: wrap;
  }

  .hero-copy h1 {
    font-size: 38px;
  }

  .campus-art {
    display: none;
  }

  .feature-strip {
    grid-template-columns: 1fr;
  }

  .auth-panel {
    padding: 24px;
  }
}
</style>
