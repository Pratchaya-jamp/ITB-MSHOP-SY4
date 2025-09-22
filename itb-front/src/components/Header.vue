<script setup>
import { useRouter } from 'vue-router'
import { ref, onMounted, onUnmounted } from 'vue'
import { jwtDecode } from 'jwt-decode'
// import Cookies from 'js-cookie'

const router = useRouter()
const isLoggedIn = ref(false)
const userProfile = ref({ name: 'User', image: 'https://cdn-icons-png.flaticon.com/512/3135/3135715.png' })

const checkLoginStatus = () => {
    const token = localStorage.getItem('access_token')
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

const goToLogin = () => {
    router.push('/signin')
}

const goToRegister = () => {
    router.push('/registers')
}

const goToProfile = () => {
    router.push('/profile')
}

const logout = () => {
    // 4. ลบ token ทั้งสองตัวออกจาก localStorage
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
    
    isLoggedIn.value = false
    
    // เด้งไปหน้า Login และรีเฟรช
    router.push('/signin').then(() => {
        window.location.reload();
    });
}

onMounted(() => {
    checkLoginStatus()
})
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
        <div class="flex space-x-4 relative">
            <div v-if="!isLoggedIn" class="flex space-x-4">
                <button class="px-6 py-2 bg-gradient-to-r from-orange-500 to-red-500 text-white rounded-full text-sm font-semibold hover:from-orange-600 hover:to-red-600 transition-all duration-300 hover:cursor-pointer"
                        @click="goToRegister">Register</button>
                <button class="px-6 py-2 bg-gradient-to-r from-orange-500 to-red-500 text-white rounded-full text-sm font-semibold hover:from-orange-600 hover:to-red-600 transition-all duration-300 hover:cursor-pointer"
                        @click="goToLogin">Login</button>
            </div>
            
            <div v-else class="relative group">
                <div class="flex items-center space-x-2 cursor-pointer">
                    <img :src="userProfile.image" alt="Profile" class="w-10 h-10 rounded-full border-2 border-orange-500 object-cover" />
                    <span class="text-white hidden md:block">{{ userProfile.name }}</span>
                </div>
                
                <div class="absolute top-full right-0 mt-2 w-48 bg-gray-800 rounded-md shadow-lg py-1 z-50 
                            invisible opacity-0 translate-y-2 group-hover:visible group-hover:opacity-100 group-hover:translate-y-0
                            transition-all duration-300">
                    <button @click="goToProfile" class="block px-4 py-2 text-sm text-gray-300 hover:bg-gray-700 w-full text-left">View Profile</button>
                    <button @click="logout" class="block px-4 py-2 text-sm text-red-500 hover:bg-gray-700 w-full text-left">Logout</button>
                </div>
            </div>
        </div>
    </header>
</template>

<style scoped>
</style>