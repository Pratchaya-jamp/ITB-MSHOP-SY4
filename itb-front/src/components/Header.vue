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
    const response = await addItem(
      `${import.meta.env.VITE_BACKEND}/v2/auth/logout`
    )

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
    class="sticky top-0 z-50 backdrop-blur-lg px-8 md:px-24 py-4 flex justify-between items-center border-b" 
    :style="theme === 'dark' ? 'border-color: rgba(255, 255, 255, 0.1)' : 'border-color: rgba(0, 0, 0, 0.1)'">
    
    <div class="text-xl font-bold tracking-wider cursor-pointer" :class="theme === 'dark' ? 'text-white' : 'text-slate-900'" @click="() => router.push('/')">ITB MSHOP</div>
    
    <nav class="space-x-8 hidden md:flex items-center text-sm font-medium" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
      <router-link to="/" class="hover:text-indigo-500 dark:hover:text-indigo-400 transition-colors">Home</router-link>
      <router-link to="/sale-items" class="hover:text-indigo-500 dark:hover:text-indigo-400 transition-colors">Phones</router-link>
      <a href="#" @click.prevent="scrollTo(services)" class="hover:text-indigo-500 dark:hover:text-indigo-400 transition-colors">Services</a>
      <a href="#" @click.prevent="scrollTo(contact)" class="hover:text-indigo-500 dark:hover:text-indigo-400 transition-colors">Contact</a>
    </nav>
    
    <div class="flex items-center space-x-4">
      
      <div v-if="!isLoggedIn" class="flex items-center space-x-4">
        <button @click="goToLogin" class="text-sm font-medium" :class="theme === 'dark' ? 'text-slate-300 hover:text-white' : 'text-slate-600 hover:text-black'">
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
        <div class="flex items-center space-x-3 cursor-pointer p-2 rounded-full transition-colors"
             :class="theme === 'dark' ? 'hover:bg-white/10' : 'hover:bg-black/5'">
          <img :src="userProfile.image" alt="Profile" class="w-8 h-8 rounded-full object-cover ring-2" :class="theme === 'dark' ? 'ring-white/20' : 'ring-black/10'" />
          <span class="text-sm font-medium hidden md:block" :class="theme === 'dark' ? 'text-slate-200' : 'text-slate-800'">
            {{ userProfile.name }}
          </span>
        </div>
        
        <div class="absolute top-full right-0 mt-2 w-56 rounded-xl shadow-lg p-2 z-50 
                     invisible opacity-0 translate-y-2 group-hover:visible group-hover:opacity-100 group-hover:translate-y-0
                     transition-all duration-300"
             :class="theme === 'dark' ? 'bg-gray-800 ring-1 ring-white/10' : 'bg-white ring-1 ring-black/5'">
          
          <div class="space-y-1">
            <button @click="goToProfile" class="w-full text-left px-3 py-2 text-sm flex items-center space-x-3 rounded-lg" :class="theme === 'dark' ? 'text-slate-300 hover:bg-indigo-500/20 hover:text-white' : 'text-slate-700 hover:bg-indigo-500/10 hover:text-indigo-600'">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" /></svg>
              <span>View Profile</span>
            </button>
            <button @click="goToOrder" class="w-full text-left px-3 py-2 text-sm flex items-center space-x-3 rounded-lg" :class="theme === 'dark' ? 'text-slate-300 hover:bg-indigo-500/20 hover:text-white' : 'text-slate-700 hover:bg-indigo-500/10 hover:text-indigo-600'">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor"><path d="M4 3a2 2 0 100 4h12a2 2 0 100-4H4z" /><path fill-rule="evenodd" d="M3 8h14v7a2 2 0 01-2 2H5a2 2 0 01-2-2V8zm5 3a1 1 0 011-1h2a1 1 0 110 2H9a1 1 0 01-1-1z" clip-rule="evenodd" /></svg>
              <span>Your Orders</span>
            </button>
          </div>
          
          <div class="mt-2 pt-2 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-black/10'">
            <button @click="logout" class="w-full text-left px-3 py-2 text-sm flex items-center space-x-3 rounded-lg text-red-500" :class="theme === 'dark' ? 'hover:bg-red-500/20 hover:text-red-400' : 'hover:bg-red-500/10 hover:text-red-600'">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z" clip-rule="evenodd" /></svg>
              <span>Logout</span>
            </button>
          </div>

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
</style>