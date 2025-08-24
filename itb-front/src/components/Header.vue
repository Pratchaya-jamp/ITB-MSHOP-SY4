<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
    <header class="sticky top-0 z-50 bg-gray-950 backdrop-blur-sm px-6 md:px-20 py-4 flex justify-between items-center transition-all duration-300">
      <div class="text-2xl font-extrabold text-white">ITB MSHOP</div>
      <nav class="space-x-6 hidden md:flex items-center">
        <router-link to="/" class="text-gray-300 hover:text-white transition-colors duration-300">Home</router-link>
        <router-link to="/sale-items" class="text-gray-300 hover:text-white transition-colors duration-300">Phones</router-link>
        <a href="#" @click.prevent="scrollTo(services)" class="text-gray-300 hover:text-white transition-colors duration-300">Services</a>
        <a href="#" @click.prevent="scrollTo(contact)" class="text-gray-300 hover:text-white transition-colors duration-300">Contact</a>
      </nav>
      <button class="px-6 py-2 bg-gradient-to-r from-orange-500 to-red-500 text-white rounded-full text-sm font-semibold hover:from-orange-600 hover:to-red-600 transition-all duration-300"
      @click="goToLogin">Login</button>
    </header>
</template>
<style scoped>
</style>
