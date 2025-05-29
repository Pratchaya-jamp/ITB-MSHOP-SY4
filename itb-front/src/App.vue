<script setup>
import { RouterView, useRoute } from 'vue-router'
import { computed } from 'vue'
import Footer from './components/Footer.vue'
import Hearder from './components/Header.vue'

const route = useRoute()
const showHeader = computed(() => route.path !== '/')
</script>

<template>
    <hearder v-if="showHeader"></hearder>
    <div>
        <router-view></router-view>
    </div>
    <Footer/>
</template>

<style scoped>
</style>
