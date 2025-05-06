<script setup>
import Hearder from './components/Hearder.vue';
import { RouterView, useRoute } from 'vue-router'
import { computed } from 'vue'

const route = useRoute()
const showHeader = computed(() => route.path !== '/')
</script>

<template>
    <hearder v-if="showHeader"></hearder>
    <div>
        <router-view></router-view>
    </div>
</template>

<style scoped>
</style>
