<script setup>
import { useRouter } from 'vue-router'
import { ref, onMounted, computed } from 'vue'
import { jwtDecode } from 'jwt-decode'
import { addItem } from '@/libs/fetchUtilsOur'
import Cookies from 'js-cookie'
import { theme, toggleTheme, applyInitialTheme } from '@/stores/themeStore.js'

const router = useRouter()
const isLoggedIn = ref(false)
const userProfile = ref({ name: 'User', image: 'https://cdn-icons-png.flaticon.com/512/3135/3135715.png' })

// --- Existing logic for authentication ---
const checkLoginStatus = () => {
    const token = Cookies.get('access_token')
    if (token) {
        isLoggedIn.value = true
        try {
            const decodedToken = jwtDecode(token)
            userProfile.value.name = decodedToken.nickname || 'User'
        } catch (error) {
            console.error("Failed to decode JWT token:", error)
            isLoggedIn.value = false
        }
    } else {
        isLoggedIn.value = false
    }
}

const goToLogin = () => router.push('/signin')
const goToRegister = () => router.push('/registers')
const goToProfile = () => router.push('/profile')
const goToOrder = () => router.push('/order')

const logout = async () => {
    try {
        const response = await addItem(`${import.meta.env.VITE_BACKEND}/v2/auth/logout`)
        if (response.status === 204) {
            setTimeout(() => {
                Cookies.remove('access_token', { path: '/' })
                Cookies.remove('refresh_token', { path: '/' })
                localStorage.removeItem('total_cart_count')
                localStorage.removeItem('CartData')
                isLoggedIn.value = false
                router.push('/signin').then(() => {
                    window.location.reload()
                })
            }, 1000)
        }
    } catch (error) {
        console.error('Logout failed:', error)
    }
}

const iconComponent = computed(() => {
    return theme.value === 'dark'
        ? `<svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" /></svg>`
        : `<svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" /></svg>`
})

onMounted(() => {
    checkLoginStatus()
    applyInitialTheme()
})
</script>

<template>
  <header 
    :class="theme === 'dark' ? 'bg-gray-950' : 'bg-white'" 
    class="sticky top-0 z-50 backdrop-blur-lg px-8 md:px-24 py-5 flex justify-between items-center border-b" 
    :style="theme === 'dark' ? 'border-color: rgba(255, 255, 255, 0.1)' : 'border-color: rgba(0, 0, 0, 0.1)'">
    
    <div class="text-xl font-bold tracking-wider" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'" @click="() => router.push('/')">ITB MSHOP</div>
    
    <nav class="space-x-8 hidden md:flex items-center text-sm font-medium" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">
      <router-link to="/" class="hover:text-blue-500 transition-colors">Home</router-link>
      <router-link to="/sale-items" class="hover:text-blue-500 transition-colors">Phones</router-link>
      <a href="#" @click.prevent="scrollTo(services)" class="hover:text-blue-500 transition-colors">Services</a>
      <a href="#" @click.prevent="scrollTo(contact)" class="hover:text-blue-500 transition-colors">Contact</a>
      </nav>
    
    <div class="flex items-center space-x-4">
      
      <div v-if="!isLoggedIn" class="flex items-center space-x-4">
        <button @click="goToLogin" class="text-sm font-medium" :class="theme === 'dark' ? 'text-gray-300 hover:text-white' : 'text-gray-600 hover:text-black'">
          Login
        </button>
        <button 
          class="px-5 py-2 border rounded-full text-sm font-semibold transition-all duration-300"
          :class="theme === 'dark' ? 'border-white/40 text-white hover:bg-white hover:text-black' : 'border-black/40 text-black hover:bg-black hover:text-white'"
          @click="goToRegister">
          Sign Up
        </button>
      </div>

      <div v-else class="relative group">
        <div class="flex items-center space-x-3 cursor-pointer">
          <img :src="userProfile.image" alt="Profile" class="w-9 h-9 rounded-full border-2 object-cover" :class="theme === 'dark' ? 'border-white/20' : 'border-black/20'" />
        </div>
        
        <div class="absolute top-full right-0 mt-3 w-48 rounded-lg shadow-lg py-1 z-50 
                     invisible opacity-0 translate-y-2 group-hover:visible group-hover:opacity-100 group-hover:translate-y-0
                     transition-all duration-300"
             :class="theme === 'dark' ? 'bg-gray-800 ring-1 ring-white/10' : 'bg-white ring-1 ring-black/5'">
          <div class="px-4 py-2 border-b" :class="theme === 'dark' ? 'border-white/10' : 'border-black/10'">
            <p class="text-sm font-medium truncate" :class="theme === 'dark' ? 'text-white' : 'text-gray-900'">{{ userProfile.name }}</p>
          </div>
          <button @click="goToProfile" class="block w-full text-left px-4 py-2 text-sm" :class="theme === 'dark' ? 'text-gray-300 hover:bg-gray-700' : 'text-gray-700 hover:bg-gray-100'">View Profile</button>
          <button @click="goToOrder" class="block w-full text-left px-4 py-2 text-sm" :class="theme === 'dark' ? 'text-gray-300 hover:bg-gray-700' : 'text-gray-700 hover:bg-gray-100'">Your Orders</button>
          <button @click="logout" class="block w-full text-left px-4 py-2 text-sm text-red-500" :class="theme === 'dark' ? 'hover:bg-gray-700' : 'hover:bg-gray-100'">Logout</button>
        </div>
      </div>
      <button @click="toggleTheme" 
        class="p-2 rounded-full transition-colors" 
        :class="theme === 'dark' ? 'text-gray-400 hover:bg-white/10' : 'text-gray-500 hover:bg-black/10'"
        v-html="iconComponent">
      </button>

      </div>
  </header>
</template>

<style scoped>
/* No styles needed here, everything is handled by Tailwind CSS */
</style>