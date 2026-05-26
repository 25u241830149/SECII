<template>
  <main class="public-layout">
    <section class="public-card">
      <div class="brand-panel">
        <RouterLink class="brand" to="/">
          <span class="brand-logo">C</span>
          <span class="brand-name">CampusHub</span>
          <span class="brand-subtitle">校园互助平台</span>
        </RouterLink>

        <div class="hero-copy">
          <p>{{ route.meta.publicEyebrow || '欢迎回来' }}</p>
          <h1>{{ route.meta.publicTitle || '校园互助，让学习更轻松，让生活更美好' }}</h1>
        </div>

        <div class="campus-art" aria-hidden="true">
          <span class="paper-plane"></span>
          <span class="cloud cloud-a"></span>
          <span class="cloud cloud-b"></span>
          <span class="building building-a"></span>
          <span class="building building-b"></span>
          <span class="tower"></span>
          <span class="road"></span>
        </div>

        <div class="feature-strip">
          <div class="feature-item">
            <span class="feature-icon shield">✓</span>
            <div>
              <strong>安全可靠</strong>
              <p>多重安全防护，保障账号安全</p>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-icon heart">♡</span>
            <div>
              <strong>互助友爱</strong>
              <p>连接校园互助，温暖彼此</p>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-icon bolt">↯</span>
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
import { useRoute } from 'vue-router'

const route = useRoute()
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
  display: grid;
  width: min(100%, 1480px);
  min-height: 680px;
  grid-template-columns: minmax(0, 1.35fr) minmax(420px, 0.75fr);
  gap: 56px;
  align-self: center;
  justify-self: center;
  padding: 58px 64px;
  border: 1px solid rgba(216, 225, 239, 0.9);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24px 72px rgba(15, 23, 42, 0.1);
}

.brand-panel {
  position: relative;
  display: flex;
  min-width: 0;
  flex-direction: column;
  overflow: hidden;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  color: inherit;
  text-decoration: none;
}

.brand-logo {
  display: grid;
  width: 58px;
  height: 58px;
  place-items: center;
  border-radius: 8px;
  background: linear-gradient(135deg, #1478ff, #6b48ff);
  color: #fff;
  font-size: 26px;
  font-weight: 800;
  box-shadow: 0 14px 28px rgba(64, 110, 255, 0.28);
}

.brand-name {
  color: #1168e8;
  font-size: 28px;
  font-weight: 800;
}

.brand-subtitle {
  color: #111827;
  font-size: 22px;
  font-weight: 700;
}

.hero-copy {
  position: relative;
  z-index: 1;
  width: min(100%, 720px);
  margin-top: 58px;
}

.hero-copy p {
  margin: 0 0 14px;
  color: #53627a;
  font-size: 20px;
  font-weight: 600;
}

.hero-copy h1 {
  margin: 0;
  color: #0f1b33;
  font-size: 56px;
  line-height: 1.14;
}

.campus-art {
  position: relative;
  flex: 1;
  min-height: 320px;
  margin-top: 18px;
}

.campus-art::before {
  position: absolute;
  right: 0;
  bottom: 18px;
  left: 0;
  height: 138px;
  border-radius: 58% 42% 0 0;
  background: linear-gradient(180deg, rgba(221, 232, 255, 0.72), rgba(255, 255, 255, 0.95));
  content: "";
}

.paper-plane {
  position: absolute;
  top: 14px;
  right: 130px;
  width: 0;
  height: 0;
  border-top: 28px solid transparent;
  border-bottom: 12px solid transparent;
  border-left: 70px solid rgba(119, 143, 255, 0.45);
  transform: rotate(-28deg);
}

.paper-plane::after {
  position: absolute;
  top: -3px;
  left: -68px;
  width: 92px;
  border-top: 3px dashed rgba(124, 143, 240, 0.34);
  border-radius: 999px;
  content: "";
  transform: translate(-92px, 48px) rotate(18deg);
}

.cloud {
  position: absolute;
  width: 58px;
  height: 18px;
  border-radius: 99px;
  background: rgba(185, 202, 255, 0.32);
}

.cloud::before,
.cloud::after {
  position: absolute;
  bottom: 4px;
  border-radius: 50%;
  background: inherit;
  content: "";
}

.cloud::before {
  left: 8px;
  width: 22px;
  height: 22px;
}

.cloud::after {
  right: 9px;
  width: 28px;
  height: 28px;
}

.cloud-a {
  top: 116px;
  left: 180px;
}

.cloud-b {
  top: 92px;
  right: 64px;
}

.building,
.tower {
  position: absolute;
  bottom: 58px;
  border: 4px solid rgba(118, 145, 235, 0.28);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.86), rgba(173, 195, 255, 0.34));
  box-shadow: 0 18px 36px rgba(64, 96, 180, 0.12);
}

.building-a {
  left: 155px;
  width: 156px;
  height: 124px;
}

.building-b {
  left: 300px;
  width: 180px;
  height: 164px;
}

.tower {
  left: 510px;
  width: 78px;
  height: 218px;
}

.tower::before {
  position: absolute;
  top: -54px;
  left: -4px;
  width: 78px;
  height: 54px;
  clip-path: polygon(50% 0, 100% 100%, 0 100%);
  background: rgba(127, 143, 241, 0.55);
  content: "";
}

.road {
  position: absolute;
  right: 120px;
  bottom: 0;
  width: 360px;
  height: 92px;
  border-radius: 50% 50% 0 0;
  border-top: 30px solid rgba(128, 147, 232, 0.3);
  transform: skewX(-20deg);
}

.feature-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 28px;
  padding-top: 22px;
}

.feature-item {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 14px;
}

.feature-icon {
  display: grid;
  width: 44px;
  height: 44px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 50%;
  font-size: 22px;
  font-weight: 800;
}

.shield {
  color: #1268ed;
  background: #e9f2ff;
}

.heart {
  color: #7c3aed;
  background: #f2ecff;
}

.bolt {
  color: #12a673;
  background: #e9f8f1;
}

.feature-item strong {
  display: block;
  color: #111827;
  font-size: 15px;
}

.feature-item p {
  margin: 4px 0 0;
  color: #687386;
  font-size: 13px;
}

.auth-panel {
  align-self: center;
  min-width: 0;
  padding: 42px;
  border: 1px solid #e5ebf4;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 24px 52px rgba(15, 23, 42, 0.1);
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
    min-height: 260px;
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
