<script setup>
import Hearder from './components/Hearder.vue';
import { RouterView } from 'vue-router'
</script>

<template>
    <hearder></hearder>
    <div>
        <router-view></router-view>
    </div>
</template>

<style scoped>
</style>
