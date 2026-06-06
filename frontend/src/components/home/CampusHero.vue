<template>
  <section class="campus-hero">
    <div class="campus-hero__content">
      <h1 class="campus-hero__title">
        让校园互助更高效、更可信、更简单
      </h1>
      <p class="campus-hero__subtitle">
        发布需求、发现服务、完成协作，CampusHub 帮你把零散的校园互助连接起来。
      </p>

      <div class="campus-hero__actions">
        <button
          v-for="action in actions"
          :key="action.key"
          class="action-card"
          type="button"
          @click="handleAction(action.key)"
        >
          <span class="action-card__icon" :class="`action-card__icon--${action.color}`">
            <component :is="action.icon" />
          </span>

          <span class="action-card__text">
            <strong>{{ action.title }}</strong>
            <span v-for="line in action.description" :key="line">{{ line }}</span>
          </span>
        </button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, h } from 'vue'

type HeroActionKey = 'publish' | 'browse' | 'orders'

const emit = defineEmits<{
  publish: []
  browse: []
  orders: []
}>()

const PaperPlaneIcon = () =>
  h('svg', { viewBox: '0 0 24 24', fill: 'none' }, [
    h('path', {
      d: 'M22 2 11 13',
      stroke: 'currentColor',
      'stroke-width': '2.1',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }),
    h('path', {
      d: 'm22 2-7 20-4-9-9-4 20-7Z',
      stroke: 'currentColor',
      'stroke-width': '2.1',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    })
  ])

const SearchIcon = () =>
  h('svg', { viewBox: '0 0 24 24', fill: 'none' }, [
    h('circle', {
      cx: '11',
      cy: '11',
      r: '7',
      stroke: 'currentColor',
      'stroke-width': '2.1'
    }),
    h('path', {
      d: 'm20 20-4.2-4.2',
      stroke: 'currentColor',
      'stroke-width': '2.1',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    })
  ])

const OrderIcon = () =>
  h('svg', { viewBox: '0 0 24 24', fill: 'none' }, [
    h('path', {
      d: 'M8 4h8',
      stroke: 'currentColor',
      'stroke-width': '2.1',
      'stroke-linecap': 'round'
    }),
    h('path', {
      d: 'M9 2h6a2 2 0 0 1 2 2v1h1a2 2 0 0 1 2 2v13a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2h1V4a2 2 0 0 1 2-2Z',
      stroke: 'currentColor',
      'stroke-width': '2.1',
      'stroke-linejoin': 'round'
    }),
    h('path', {
      d: 'M8 11h8M8 15h8',
      stroke: 'currentColor',
      'stroke-width': '2.1',
      'stroke-linecap': 'round'
    })
  ])

const actions = computed(() => [
  {
    key: 'publish' as const,
    title: '发布互助需求',
    description: ['填写需求信息', '快速发布'],
    color: 'blue',
    icon: PaperPlaneIcon
  },
  {
    key: 'browse' as const,
    title: '浏览可接需求',
    description: ['发现合适的需求', '提供帮助'],
    color: 'purple',
    icon: SearchIcon
  },
  {
    key: 'orders' as const,
    title: '查看我的订单',
    description: ['管理订单进度', '查看历史记录'],
    color: 'green',
    icon: OrderIcon
  }
])

function handleAction(key: HeroActionKey) {
  if (key === 'publish') {
    emit('publish')
    return
  }

  if (key === 'browse') {
    emit('browse')
    return
  }

  emit('orders')
}
</script>

<style scoped>
.campus-hero {
  position: relative;
  height: 255px;
  border-radius: 14px;
  overflow: hidden;
  color: #ffffff;
  isolation: isolate;
  box-shadow: 0 20px 45px rgba(30, 91, 220, 0.18);
  background: linear-gradient(90deg, #087bff 0%, #167cff 32%, #4f78ff 58%, #7563ff 100%);
}


.campus-hero::before {
  content: "";
  position: absolute;
  inset: 0;
  z-index: 0;
  border-radius: inherit;
  background-image: url("../../assets/image.png");
  background-repeat: no-repeat;
  background-size: 56% auto;
  background-position: right 0 bottom -42px;
  opacity: 0.98;
  pointer-events: none;

  -webkit-mask-image: linear-gradient(
    90deg,
    transparent 0%,
    transparent 46%,
    rgba(0, 0, 0, 0.08) 56%,
    rgba(0, 0, 0, 0.52) 74%,
    #000 100%
  );
  mask-image: linear-gradient(
    90deg,
    transparent 0%,
    transparent 46%,
    rgba(0, 0, 0, 0.08) 56%,
    rgba(0, 0, 0, 0.52) 74%,
    #000 100%
  );
}

/* 右侧柔光层，让插画更像嵌入在渐变背景中，而不是生硬贴图 */
.campus-hero::after {
  content: "";
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background:
    radial-gradient(circle at 94% 78%, rgba(118, 92, 255, 0.14), transparent 32%),
    linear-gradient(
      90deg,
      transparent 0%,
      rgba(65, 112, 255, 0.03) 48%,
      rgba(94, 105, 255, 0.1) 72%,
      rgba(117, 99, 255, 0.12) 100%
    );
}

.campus-hero__content {
  position: relative;
  z-index: 2;
  height: 100%;
  padding: 28px 40px 24px;
}

.campus-hero__title {
  margin: 0 0 10px;
  font-size: 33px;
  line-height: 1.22;
  font-weight: 900;
  letter-spacing: 1px;
  text-shadow: 0 2px 8px rgba(20, 50, 120, 0.12);
}

.campus-hero__subtitle {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  opacity: 0.96;
}

.campus-hero__actions {
  position: absolute;
  left: 40px;
  right: 40px;
  bottom: 24px;
  display: grid;
  grid-template-columns: 230px 230px 230px;
  gap: 14px;
}

.action-card {
  height: 92px;
  padding: 16px 18px;
  border: 1px solid rgba(255, 255, 255, 0.82);
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255, 255, 255, 0.96);
  color: #111827;
  text-align: left;
  cursor: pointer;
  backdrop-filter: blur(12px);
  box-shadow:
    0 16px 30px rgba(15, 52, 126, 0.18),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.action-card__icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  color: #ffffff;
  box-shadow: 0 8px 18px rgba(34, 84, 190, 0.2);
}

.action-card__icon :deep(svg) {
  width: 27px;
  height: 27px;
}

.action-card__icon--blue {
  background: linear-gradient(135deg, #2d8bff, #2c6fff);
}

.action-card__icon--purple {
  background: linear-gradient(135deg, #9b6cff, #7b4cff);
}

.action-card__icon--green {
  background: linear-gradient(135deg, #2ecf92, #18b67d);
}

.action-card__text {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.action-card__text strong {
  margin-bottom: 3px;
  font-size: 18px;
  line-height: 1.25;
  font-weight: 800;
  color: #111827;
}

.action-card__text span {
  font-size: 14px;
  line-height: 1.25;
  color: #4b5563;
  font-weight: 500;
}

@media (max-width: 900px) {
  .campus-hero {
    height: auto;
    min-height: 420px;
  }

  .campus-hero::before {
    background-size: 96% auto;
    background-position: right bottom;
    opacity: 0.48;
    -webkit-mask-image: linear-gradient(
      180deg,
      transparent 0%,
      rgba(0, 0, 0, 0.58) 36%,
      #000 100%
    );
    mask-image: linear-gradient(
      180deg,
      transparent 0%,
      rgba(0, 0, 0, 0.58) 36%,
      #000 100%
    );
  }

  .campus-hero__content {
    padding: 28px 24px;
  }

  .campus-hero__title {
    font-size: 28px;
  }

  .campus-hero__actions {
    position: relative;
    left: auto;
    right: auto;
    bottom: auto;
    margin-top: 28px;
    grid-template-columns: 1fr;
  }
}
</style>
