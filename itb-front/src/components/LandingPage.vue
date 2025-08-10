<script setup>
import { useRouter } from 'vue-router'
import { ref, computed, onMounted } from 'vue'

const router = useRouter()
const services = ref(null)
const contact = ref(null)

const theme = ref(localStorage.getItem('theme') || 'dark')

const applyTheme = (newTheme) => {
  document.body.className = newTheme === 'dark' ? 'dark-theme' : ''
  localStorage.setItem('theme', newTheme)
  theme.value = newTheme
}

const toggleTheme = () => {
  const newTheme = theme.value === 'dark' ? 'light' : 'dark'
  applyTheme(newTheme)
}

onMounted(() => {
  applyTheme(theme.value)
})

const navigateToSaleItems = () => {
  router.push('/sale-items')
}
const navigateToBrands = () => {
  router.push('/sale-items/list')
}
const scrollTo = (target) => {
  target?.scrollIntoView({ behavior: 'smooth' })
}

const themeClass = computed(() => {
  return theme.value === 'dark'
    ? 'bg-gray-950 text-white'
    : 'bg-white text-gray-950'
})

const iconComponent = computed(() => {
  return theme.value === 'dark'
    ? `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" /></svg>` // sun icon
    : `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" /></svg>` // moon icon
})
</script>

<template>
  <div :class="themeClass" class="relative min-h-screen font-sans overflow-x-hidden">
    <header :class="theme === 'dark' ? 'bg-gray-950/80' : 'bg-white/80'" class="sticky top-0 z-50 backdrop-blur-sm px-6 md:px-20 py-4 flex justify-between items-center transition-all duration-300">
      <div class="text-2xl font-extrabold" :class="theme === 'dark' ? 'text-white' : 'text-gray-950'">ITB MSHOP</div>
      <nav class="space-x-6 hidden md:flex items-center">
        <router-link to="/" :class="theme === 'dark' ? 'text-gray-300 hover:text-white' : 'text-gray-600 hover:text-black'" class="transition-colors duration-300">Home</router-link>
        <a href="#" @click.prevent="scrollTo(services)" :class="theme === 'dark' ? 'text-gray-300 hover:text-white' : 'text-gray-600 hover:text-black'" class="transition-colors duration-300">Services</a>
        <a href="#" @click.prevent="scrollTo(contact)" :class="theme === 'dark' ? 'text-gray-300 hover:text-white' : 'text-gray-600 hover:text-black'" class="transition-colors duration-300">Contact</a>
      </nav>
      <button class="px-6 py-2 bg-gradient-to-r from-orange-500 to-red-500 text-white rounded-full text-sm font-semibold hover:from-orange-600 hover:to-red-600 transition-all duration-300">Login</button>
    </header>

    <div class="relative flex flex-col-reverse md:flex-row items-center justify-between px-6 md:px-20 py-24 gap-10 min-h-screen">
      <div class="absolute inset-0 w-full h-full opacity-10 pointer-events-none">
        <div class="absolute w-96 h-96 bg-gradient-to-r from-orange-500 to-pink-500 rounded-full blur-3xl opacity-50 top-1/4 left-1/4 animate-blob"></div>
        <div class="absolute w-80 h-80 bg-gradient-to-r from-teal-400 to-blue-500 rounded-full blur-3xl opacity-50 bottom-1/4 right-1/4 animate-blob animation-delay-2000"></div>
      </div>
      <div class="relative z-10 text-center md:text-left max-w-xl space-y-6 animate-fade-in-up">
        <h1 class="text-6xl font-extrabold leading-tight">
          Find Your Perfect <span class="text-transparent bg-clip-text bg-gradient-to-r from-orange-500 to-pink-500">Smartphone</span><br />
          Right Here
        </h1>
        <p class="text-lg opacity-90" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-700'">
          Explore our curated collection of the latest smartphones. Trusted brands, amazing prices, and lightning-fast delivery — all in one place.
        </p>
        <div class="flex flex-col sm:flex-row items-center mt-6 space-y-4 sm:space-y-0 sm:space-x-4">
          <button @click="navigateToSaleItems" class="itbms-shopnow w-full sm:w-auto px-10 py-3 bg-gradient-to-r from-orange-500 to-red-500 text-white font-semibold rounded-full shadow-lg transition-all duration-300 transform hover:-translate-y-1 hover:scale-105">
            Shop Now
          </button>
          <button @click="navigateToBrands" class="itbms-seller w-full sm:w-auto px-10 py-3 font-semibold rounded-full transition-all duration-300 transform hover:bg-white/10 hover:border-white/50" :class="theme === 'dark' ? 'border-2 border-white/20 text-white hover:bg-white/10 hover:border-white/50' : 'border-2 border-gray-400 text-gray-800 hover:bg-gray-200/50 hover:border-gray-500'">
            Seller
          </button>
        </div>
      </div>
      <div class="relative z-10 animate-fade-in-down">
        <img src="/phone/Thumbnail.png" alt="Mobile Phone" class="w-80 md:w-[450px] rounded-3xl shadow-2xl transition-transform duration-500 hover:scale-105">
        <div class="absolute -top-6 -right-6 bg-gradient-to-br from-white/20 to-white/5 backdrop-blur-lg px-6 py-3 rounded-full text-lg font-bold text-white shadow-inner border border-white/10 transform rotate-6">
          NEW
        </div>
      </div>
    </div>
    
    <div ref="services" :class="theme === 'dark' ? 'bg-gray-900 text-gray-200' : 'bg-gray-100 text-gray-800'" class="features-section px-6 md:px-20 py-24 rounded-t-[5rem] -mt-16 relative z-20">
      <h2 class="text-4xl md:text-5xl font-bold text-center mb-16">Why Shop With Us?</h2>
      <div class="grid md:grid-cols-3 gap-10">
        <div :class="theme === 'dark' ? 'bg-gray-800 shadow-xl border border-gray-700' : 'bg-gray-200 shadow-xl border border-gray-300'" class="feature-card animate-fade-in-up p-8 rounded-[2rem] transition-all transform hover:-translate-y-3 hover:shadow-2xl">
          <h3 class="text-2xl font-semibold mb-4 text-orange-400">Top-Quality Devices</h3>
          <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">We offer only the latest and most reliable smartphones from trusted brands — 100% genuine and guaranteed.</p>
        </div>
        <div :class="theme === 'dark' ? 'bg-gray-800 shadow-xl border border-gray-700' : 'bg-gray-200 shadow-xl border border-gray-300'" class="feature-card animate-fade-in-up p-8 rounded-[2rem] transition-all transform hover:-translate-y-3 hover:shadow-2xl">
          <h3 class="text-2xl font-semibold mb-4 text-red-400">Unbeatable Prices</h3>
          <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Get the best deals on your favorite phones. Daily discounts, bundle offers, and flexible payment plans.</p>
        </div>
        <div :class="theme === 'dark' ? 'bg-gray-800 shadow-xl border border-gray-700' : 'bg-gray-200 shadow-xl border border-gray-300'" class="feature-card animate-fade-in-up p-8 rounded-[2rem] transition-all transform hover:-translate-y-3 hover:shadow-2xl">
          <h3 class="text-2xl font-semibold mb-4 text-teal-400">Fast & Trusted Service</h3>
          <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Enjoy fast shipping, friendly support, and hassle-free warranties. Your satisfaction is our top priority.</p>
        </div>
      </div>
    </div>
    
    <div class="services-section px-6 md:px-20 py-24" :class="theme === 'dark' ? 'bg-gray-950 text-gray-200' : 'bg-white text-gray-800'">
      <h2 class="text-4xl md:text-5xl font-bold text-center mb-16">Our Services</h2>
      <div class="grid md:grid-cols-3 gap-10">
        <div :class="theme === 'dark' ? 'bg-gray-900 shadow-xl border border-gray-800' : 'bg-gray-100 shadow-xl border border-gray-200'" class="service-card animate-fade-in-up p-8 rounded-[2rem] transition-all transform hover:-translate-y-3 hover:shadow-2xl">
          <h3 class="text-2xl font-semibold mb-4 text-orange-400">Expert Consultation</h3>
          <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Talk to our tech experts to find the right phone for your needs.</p>
        </div>
        <div :class="theme === 'dark' ? 'bg-gray-900 shadow-xl border border-gray-800' : 'bg-gray-100 shadow-xl border border-gray-200'" class="service-card animate-fade-in-up p-8 rounded-[2rem] transition-all transform hover:-translate-y-3 hover:shadow-2xl">
          <h3 class="text-2xl font-semibold mb-4 text-red-400">Device Protection Plans</h3>
          <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Affordable warranty and insurance plans for peace of mind.</p>
        </div>
        <div :class="theme === 'dark' ? 'bg-gray-900 shadow-xl border border-gray-800' : 'bg-gray-100 shadow-xl border border-gray-200'" class="service-card animate-fade-in-up p-8 rounded-[2rem] transition-all transform hover:-translate-y-3 hover:shadow-2xl">
          <h3 class="text-2xl font-semibold mb-4 text-teal-400">Fast Delivery</h3>
          <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Same-day or next-day delivery in select areas!</p>
        </div>
      </div>
    </div>
    
    <div ref="contact" class="contact-section px-6 md:px-20 py-24 rounded-b-[5rem]" :class="theme === 'dark' ? 'bg-gray-900 text-gray-200' : 'bg-gray-100 text-gray-800'">
      <h2 class="text-4xl md:text-5xl font-bold text-center mb-16">Contact Us</h2>
      <form class="max-w-xl mx-auto space-y-6 animate-fade-in-up">
        <input type="text" placeholder="Your Name" class="w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all" :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-black'" />
        <input type="email" placeholder="Your Email" class="w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all" :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-black'" />
        <textarea rows="6" placeholder="Your Message" class="w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-orange-500 focus:border-transparent transition-all" :class="theme === 'dark' ? 'bg-gray-800 border border-gray-700 text-white' : 'bg-white border border-gray-300 text-black'"></textarea>
        <button class="w-full bg-gradient-to-r from-orange-500 to-red-500 hover:from-orange-600 hover:to-red-600 text-white px-8 py-4 rounded-full font-semibold transition-all transform hover:-translate-y-1">
          Send Message
        </button>
      </form>
    </div>

    <button @click="toggleTheme" class="fixed bottom-6 right-6 p-4 rounded-full backdrop-blur-sm shadow-lg transition-all duration-300 z-50" :class="theme === 'dark' ? 'bg-gray-700/80 hover:bg-gray-600/80 text-white' : 'bg-gray-200/80 hover:bg-gray-300/80 text-black'" v-html="iconComponent">
    </button>
  </div>
</template>

<style scoped>
@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fade-in-down {
  from {
    opacity: 0;
    transform: translateY(-40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  animation: fade-in-up 1s ease-out forwards;
}

.animate-fade-in-down {
  animation: fade-in-down 1s ease-out forwards;
}

@keyframes blob {
  0% { transform: scale(1) translate(0px, 0px); }
  33% { transform: scale(1.1) translate(30px, -50px); }
  66% { transform: scale(0.9) translate(-20px, 20px); }
  100% { transform: scale(1) translate(0px, 0px); }
}

.animate-blob {
  animation: blob 7s infinite ease-in-out;
}

.animation-delay-2000 {
  animation-delay: 2s;
}
</style>
