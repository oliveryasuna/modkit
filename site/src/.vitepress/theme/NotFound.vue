<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {useData, withBase} from 'vitepress';

const quotes = [
  'But if you keep digging straight down, you may end up where you are heading.',
  'This chunk never generated. Try walking back the way you came.',
  'You followed the coordinates exactly. They were from a different seed.',
  'This page despawned. It was left on the ground too long.',
  'Nothing here but bedrock. Whatever you were looking for is somewhere above you.'
]

const {theme} = useData();
const quote = ref(quotes[0])
const ready = ref(false)

onMounted(() => {
  quote.value = quotes[Math.floor(Math.random() * quotes.length)]
  ready.value = true
})
</script>

<template>
  <div class="NotFound">
    <p class="code">{{ theme.notFound?.code ?? '404' }}</p>
    <h1 class="title">{{ theme.notFound?.title ?? 'PAGE NOT FOUND' }}</h1>
    <div class="divider" />
    <blockquote class="quote" :style="{ visibility: ready ? 'visible' : 'hidden' }">
      {{ quote }}
    </blockquote>
    <div class="action">
      <a
        class="link"
        :href="withBase(theme.notFound?.link ?? '/')"
        :aria-label="theme.notFound?.linkLabel ?? 'go to home'"
      >
        {{ theme.notFound?.linkText ?? 'Take me home' }}
      </a>
    </div>
  </div>
</template>

<style scoped>
.NotFound { padding: 4rem 1.5rem 6rem; text-align: center; }
@media (min-width: 48rem) { .NotFound { padding: 6rem 2rem 10.5rem; } }
.code { line-height: 1; font-size: 4rem; font-weight: 600; }
.title { padding-top: 0.75rem; letter-spacing: 0.1em; line-height: 1; font-size: 1.25rem; font-weight: 700; }
.divider { margin: 1.5rem auto 1.125rem; width: 4rem; height: 1px; background-color: var(--vp-c-divider); }
.quote { margin: 0 auto; max-width: 16rem; font-size: 0.875rem; font-weight: 500; color: var(--vp-c-text-2); }
.action { padding-top: 1.25rem; }
.link {
  display: inline-block; border: 1px solid var(--vp-c-brand-1); border-radius: 1rem;
  padding: 0.1875rem 1rem; font-size: 0.875rem; font-weight: 500; color: var(--vp-c-brand-1);
  transition: border-color 0.25s, color 0.25s;
}
.link:hover { border-color: var(--vp-c-brand-2); color: var(--vp-c-brand-2); }
</style>
