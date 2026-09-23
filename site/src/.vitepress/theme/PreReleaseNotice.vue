<script setup lang="ts">
import {useData} from 'vitepress'
import {computed} from 'vue'

const {frontmatter, page} = useData()

const show = computed(() =>
  frontmatter.value.prerelease !== false &&
  page.value.relativePath.startsWith('modkit/')
)
</script>

<template>
  <div v-if="show" class="pre-release-notice">
    <div class="warning custom-block">
      <p class="custom-block-title">Pre-release</p>
      <p>Modkit has not yet reached version 1. These docs describe the intended experience, and are definitely incomplete; breaking changes may occur.</p>
    </div>
  </div>
</template>

<style scoped>
.pre-release-notice {
  margin-bottom: 32px;
}
</style>
