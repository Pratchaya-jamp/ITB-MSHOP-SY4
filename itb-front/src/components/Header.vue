<script setup>
</script>

<template>
<header class="bg-gray-900 text-white px-6 md:px-20 py-4 flex justify-between items-center">
  <div class="text-xl font-bold">🌟ITB MSHOP</div>
  <nav class="space-x-6 hidden md:block">
    <a href="#" class="hover:text-yellow-400">
      <router-link to="/">Home</router-link>
    </a>
    <a href="#" class="hover:text-yellow-400">
      <router-link to="/sale-items">Phones</router-link>
    </a>
    <!-- <a href="#" class="hover:text-yellow-400">
      <router-link to="/brands">Deals</router-link>
    </a> -->
    <a href="#" class="hover:text-yellow-400">Services</a>
    <a href="#" class="hover:text-yellow-400">Contact</a>
  </nav>
  <button class="bg-yellow-400 text-black px-4 py-1 rounded-full text-sm hover:bg-yellow-300 transition">Login</button>
</header>
</template>

<style scoped>
</style>
