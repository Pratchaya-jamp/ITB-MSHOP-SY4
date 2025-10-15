<script setup>
import { useRouter } from 'vue-router'
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { theme } from '@/stores/themeStore.js'

const router = useRouter()
const services = ref(null)
const contact = ref(null)
const randomSaleItems = ref([])
const slidePosition = ref(0) // ตำแหน่งการสไลด์
let slideInterval = null // ตัวแปรสำหรับเก็บ interval
const mainImage = ref("/sy4/phone/iPhone.png")

// --- Script ถูกปรับให้เหลือแค่การรับค่า Theme มาใช้งาน ---
// const theme = ref(localStorage.getItem('theme') || 'dark')

// // Listener to update the theme if it's changed in another tab
// window.addEventListener('storage', () => {
//     const newTheme = localStorage.getItem('theme') || 'dark'
//     if (newTheme !== theme.value) {
//         theme.value = newTheme
//     }
// })

const themeClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-950 text-white'
        : 'bg-white text-gray-950'
})

// const applyTheme = (newTheme) => {
//   document.body.className = newTheme === 'dark' ? 'dark-theme' : ''
//   localStorage.setItem('theme', newTheme)
//   theme.value = newTheme
// }

// const toggleTheme = () => {
//   const newTheme = theme.value === 'dark' ? 'light' : 'dark'
//   applyTheme(newTheme)
// }
// --- สิ้นสุดส่วน Script ---

const fetchRandomSaleItems = async () => {
    try {
        const response = await fetch(`${import.meta.env.VITE_BACKEND}/v2/sale-items`)
        if (!response.ok) throw new Error('Failed to fetch sale items')
        const items = await response.json()
        
        if (items && items.length > 0) {
            const shuffled = items.sort(() => 0.5 - Math.random())
            randomSaleItems.value = shuffled.slice(0, 7)
            slidePosition.value = 0;
        }
    } catch (error) {
        console.error('Error fetching sale items:', error)
    }
}

const formatPrice = (price) => {
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

const startSliding = () => {
    if (slideInterval) {
        clearInterval(slideInterval);
    }
    slideInterval = setInterval(() => {
        if (slidePosition.value >= randomSaleItems.value.length - 3) {
            fetchRandomSaleItems();
        } else {
            slidePosition.value++;
        }
    }, 3000);
};

onMounted(() => {
    fetchRandomSaleItems();
});

onUnmounted(() => {
    if (slideInterval) {
        clearInterval(slideInterval);
    }
});

watch(randomSaleItems, (newItems) => {
    if (newItems.length > 0) {
        startSliding();
    }
}, { immediate: true });

const navigateToSaleItems = () => {
    router.push('/sale-items')
}
const navigateToBrands = () => {
    router.push('/sale-items/list')
}
const scrollTo = (target) => {
    target?.scrollIntoView({ behavior: 'smooth' })
}

const carouselTransform = computed(() => {
    return `translateX(-${slidePosition.value * (100 / 3)}%)`
})

const goToPhoneDetails = (id) => {
  router.push(`/sale-items/${id}`)
}

const goToSignUp = () => {
  router.push(`/registers`)
}

// const iconComponent = computed(() => {
//     return theme.value === 'dark'
//         ? `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" /></svg>`
//         : `<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" /></svg>`
// })
</script>

<template>
  <div :class="themeClass" class="relative min-h-screen font-sans overflow-x-hidden transition-colors duration-500">
    
    <main class="px-8 md:px-24">
      <section class="relative flex flex-col items-center text-center py-28 md:py-40">
        <div class="absolute inset-0 w-full h-full opacity-20 pointer-events-none">
          <div class="absolute w-96 h-96 bg-blue-500 rounded-full blur-[100px] opacity-40 top-1/4 left-1/4 animate-blob"></div>
          <div class="absolute w-80 h-80 bg-purple-500 rounded-full blur-[100px] opacity-40 bottom-1/4 right-1/4 animate-blob animation-delay-2000"></div>
        </div>
        <div class="relative z-10 max-w-4xl animate-fade-in-up">
          <h1 class="text-5xl md:text-7xl font-extrabold leading-tight tracking-tight">
            Discover Your Next
            <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-500">Smartphone</span>
          </h1>
          <p class="max-w-2xl mx-auto mt-6 text-lg md:text-xl" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">
            Explore our curated collection of the latest smartphones. Trusted brands, amazing prices, and lightning-fast delivery.
          </p>
          <div class="flex flex-col sm:flex-row items-center justify-center mt-10 space-y-4 sm:space-y-0 sm:space-x-6">
            <button @click="navigateToSaleItems" class="w-full sm:w-auto px-10 py-4 bg-gradient-to-r from-blue-500 to-purple-600 text-white font-semibold rounded-full shadow-lg transition-all duration-300 transform hover:scale-105">
              Shop Collection
            </button>
            <button @click="navigateToBrands" class="w-full sm:w-auto px-10 py-4 font-semibold rounded-full transition-all duration-300" :class="theme === 'dark' ? 'bg-white/10 text-white hover:bg-white/20' : 'bg-black/5 text-black hover:bg-black/10'">
              Start Selling
            </button>
          </div>
        </div>
        <div class="relative z-10 mt-20 animate-fade-in-down">
          <div class="relative" :class="theme === 'dark' ? 'shadow-[0px_0px_80px_rgba(173,216,230,0.1)]' : 'shadow-[0px_0px_80px_rgba(0,0,0,0.1)]' " style="border-radius: 30px;">
            <img src="/phone/Thumbnail.png" alt="Smartphone Showcase" class="w-80 md:w-[450px] rounded-[30px] transition-transform duration-500">
          </div>
        </div>
      </section>

      <section class="py-24">
        <div class="text-center mb-16">
          <h2 class="text-4xl md:text-5xl font-bold">Explore Our Collection</h2>
          <p class="mt-4 text-lg" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Handpicked devices just for you.</p>
        </div>
        
        <div class="overflow-hidden relative w-full">
          <div class="flex transition-transform duration-1000 ease-in-out" :style="{ transform: carouselTransform }">
            <div v-for="item in randomSaleItems" :key="item.id"
              class="group relative flex-none w-full md:w-1/3 p-4 mx-2 flex flex-col items-center justify-between cursor-pointer"
              @click="goToPhoneDetails(item.id)">
              
              <div class="w-full h-full absolute inset-0 rounded-3xl transition-all duration-300" :class="theme === 'dark' ? 'bg-white/5 group-hover:bg-white/10' : 'bg-black/5 group-hover:bg-black/10'"></div>

              <div class="relative p-6 flex flex-col items-center text-center w-full">
                <img :src="mainImage" :alt="item.productName" class="w-48 h-48 object-contain mb-6 transition-transform duration-500 group-hover:scale-105"/>
                <div class="flex-grow">
                  <h3 class="text-xl font-bold mb-1">{{ item.model }}</h3>
                  <p class="text-sm mb-2" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">{{ item.brandName }}</p>
                  <p class="text-sm mb-4" :class="theme === 'dark' ? 'text-gray-500' : 'text-gray-500'">{{ item.storageGb }} GB / {{ item.ramGb }} GB</p>
                  <p class="text-2xl font-bold text-blue-400">{{ formatPrice(item.price) }} ฿</p>
                </div>
                </div>
            </div>
          </div>
        </div>
      </section>

      <section ref="services" class="py-24">
        <div class="text-center mb-16">
          <h2 class="text-4xl md:text-5xl font-bold">Why Choose Us?</h2>
          <p class="mt-4 text-lg" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Your satisfaction is our top priority.</p>
        </div>
        <div class="grid md:grid-cols-3 gap-8 text-center md:text-left">
          <div class="p-8 rounded-3xl" :class="theme === 'dark' ? 'bg-white/5' : 'bg-black/5'">
            <h3 class="text-xl font-semibold mb-3 text-blue-400">Top-Quality Devices</h3>
            <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">We offer only genuine, reliable smartphones from trusted brands, 100% guaranteed.</p>
          </div>
          <div class="p-8 rounded-3xl" :class="theme === 'dark' ? 'bg-white/5' : 'bg-black/5'">
            <h3 class="text-xl font-semibold mb-3 text-purple-400">Unbeatable Prices</h3>
            <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Get the best deals with daily discounts, bundle offers, and flexible payment plans.</p>
          </div>
          <div class="p-8 rounded-3xl" :class="theme === 'dark' ? 'bg-white/5' : 'bg-black/5'">
            <h3 class="text-xl font-semibold mb-3 text-cyan-400">Fast & Trusted Service</h3>
            <p :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Enjoy fast shipping, friendly support, and hassle-free warranties for a seamless experience.</p>
          </div>
        </div>
      </section>

      <section ref="contact" class="py-24">
        <div class="grid md:grid-cols-2 gap-16 items-center">
          <div class="animate-fade-in-up">
            <h2 class="text-4xl md:text-5xl font-bold">Get In Touch</h2>
            <p class="mt-4 text-lg" :class="theme === 'dark' ? 'text-gray-400' : 'text-gray-600'">Have questions or need assistance? Our team is here to help. Drop us a message, and we'll get back to you shortly.</p>
          </div>
          <form class="w-full space-y-6 animate-fade-in-up">
            <input type="text" placeholder="Your Name" class="w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all outline-none" :class="theme === 'dark' ? 'bg-white/5' : 'bg-black/5'" />
            <input type="email" placeholder="Your Email" class="w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all outline-none" :class="theme === 'dark' ? 'bg-white/5' : 'bg-black/5'" />
            <textarea rows="5" placeholder="Your Message" class="w-full p-4 rounded-xl placeholder-gray-500 focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all outline-none" :class="theme === 'dark' ? 'bg-white/5' : 'bg-black/5'"></textarea>
            <button class="w-full bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 text-white px-8 py-4 rounded-full font-semibold transition-all transform hover:scale-105">
              Send Message
            </button>
          </form>
        </div>
      </section>
    </main>

    </div>
</template>

<style scoped>
/* @keyframes และอื่นๆ เหมือนเดิม */
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