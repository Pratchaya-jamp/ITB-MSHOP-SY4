<script setup>
import { RouterView, useRoute } from 'vue-router'
import Footer from './components/Footer.vue'
import Hearder from './components/Header.vue'

const route = useRoute()

</script>

<template>

     <Hearder />
    <div>
        <router-view></router-view>
    </div>
    <Footer/>
</template>

<style scoped>
</style>
