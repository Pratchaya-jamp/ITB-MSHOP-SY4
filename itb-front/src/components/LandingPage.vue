<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const navigateToSaleItems = () => {
  router.push('/sale-items')
}
</script>

<template>
    <div class="bg-gray-100 border border-gray-300 rounded-md p-8 text-center flex flex-col items-center justify-center min-h-screen">
        <h1 class="text-3xl font-bold text-gray-800 mb-4">Welcome to ITB-MSHOP!</h1>
        <p class="text-lg text-gray-600 mb-8">Find the ideal phone for your needs and budget. We offer a diverse range of smartphones from top brands.</p>
        <button class="bg-orange-400 hover:bg-orange-500 text-white font-bold py-3 px-6 rounded-md shadow-md transition duration-300 ease-in-out cursor-pointer"
        @click="navigateToSaleItems">
        Shop Now
        </button>
    </div>
</template>

<style scoped>
</style>
