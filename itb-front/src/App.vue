<script setup>
import { RouterView, useRoute } from 'vue-router'
import Footer from './components/Footer.vue'
import Hearder from './components/Header.vue'
// ✨ 1. Import CookieBar component
import CookieBar from './components/CookieBar.vue' 

const route = useRoute()

</script>

<template>
  <div class="relative min-h-screen flex flex-col">
    <Hearder />
    <main class="flex-grow">
      <router-view></router-view>
    </main>
    <Footer/>
  </div>

  <CookieBar />
</template>

<style scoped>
/* ✨ 3. ลบ CSS ของ .slide-up ออกไป (ย้ายไปไว้ใน CookieBar.vue) */

/* ทำให้ Header, Main, Footer ทำงานร่วมกัน (Flexbox) */
.min-h-screen {
  display: flex;
  flex-direction: column;
}
.flex-grow {
  flex-grow: 1;
}
</style>