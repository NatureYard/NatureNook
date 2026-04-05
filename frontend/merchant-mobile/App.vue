<template>
  <view>
    <slot />
  </view>
</template>

<script setup>
</script>

<style>
page {
  --brand-primary: #ff6b4a;
  --brand-primary-strong: #ff8a5c;
  --brand-secondary: #2f8f7d;
  --brand-secondary-soft: #61c5a4;
  --text-primary: #2f2a24;
  --text-secondary: #7d6d60;
  --text-tertiary: #8c796b;
  --surface-base: #fff9f2;
  --surface-card: #ffffff;
  --surface-muted: #f5f7f8;
  --surface-soft: #e2e8f0;
  --surface-highlight-start: #ecfeff;
  --surface-highlight-success: rgba(47, 143, 125, 0.08);
  --border-subtle: #e5e7eb;
  --state-success-bg: #d1fae5;
  --state-success-fg: #065f46;
  --state-warning-bg: #fef3c7;
  --state-warning-fg: #92400e;
  --state-risk-bg: #fee2e2;
  --state-risk-fg: #b91c1c;
  background: var(--surface-base);
  color: var(--text-primary);
}
</style>

