<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { theme } from '@/stores/themeStore.js';

const router = useRouter();

// --- Theming (มาจาก Store) ---
const themeClass = computed(() => {
  return theme.value === "dark"
    ? "dark bg-gray-900 text-slate-200"
    : "bg-slate-50 text-slate-800";
});

// --- Mock Data สำหรับ Services ---
const services = ref([
  { 
    id: 1, 
    title: "Seller Dashboard", 
    description: "Manage your products, track sales, and view incoming orders all in one place.",
    icon: `<svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M9 17v-2m3 2v-4m3 4v-6m-9 4H5a2 2 0 00-2 2v6a2 2 0 002 2h14a2 2 0 002-2v-6a2 2 0 00-2-2h-2.5" /></svg>`
  },
  { 
    id: 2, 
    title: "Secure Payments", 
    description: "All transactions are processed securely. We handle payments with industry-standard encryption.",
    icon: `<svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" /></svg>`
  },
  { 
    id: 3, 
    title: "24/7 Support", 
    description: "Our team is here to help. Contact us anytime for questions about your orders or your account.",
    icon: `<svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" /></svg>`
  }
]);

</script>

<template>
  <div :class="themeClass" class="min-h-screen font-sans">
    <div class="container mx-auto px-6 py-12 max-w-4xl animate-fade-in-up">

      <div class="mb-12">
        <h1 class="text-4xl font-extrabold tracking-tight" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">
          Our Services
        </h1>
        <p class="mt-4 text-xl" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
          What ITB MSHOP provides for you.
        </p>
      </div>

      <div 
        class="p-8 md:p-10 rounded-2xl space-y-8" 
        :class="theme === 'dark' ? 'bg-gray-800/30' : 'bg-white shadow-sm ring-1 ring-black/5'"
      >
        
        <section>
          <h2 class="text-2xl font-bold mb-6">Platform Features</h2>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            
            <div 
              v-for="service in services" 
              :key="service.id" 
              class="p-6 rounded-lg transition-all duration-300"
              :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'"
            >
              <div 
                class="w-12 h-12 rounded-lg flex items-center justify-center mb-4"
                :class="theme === 'dark' ? 'bg-indigo-500/10 text-indigo-400' : 'bg-indigo-100 text-indigo-600'"
                v-html="service.icon"
              >
              </div>
              <h3 class="font-bold text-lg mb-2" :class="theme === 'dark' ? 'text-white' : 'text-slate-900'">
                {{ service.title }}
              </h3>
              <p class="text-sm" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-600'">
                {{ service.description }}
              </p>
            </div>

          </div>
        </section>

        <section class="pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
          <h2 class="text-2xl font-bold mb-4">Future Development</h2>
          <p :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'" class="leading-relaxed">
            We are constantly working to improve our platform. New features, including advanced analytics for sellers and personalized recommendations for buyers, are currently in development.
          </p>
        </section>

        <div class="pt-8 border-t text-center" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
            <button 
                @click="router.push('/sale-items')" 
                class="w-full sm:w-auto px-8 py-3 bg-indigo-600 text-white font-semibold rounded-full shadow-lg shadow-indigo-500/20 transition-all duration-300 transform hover:-translate-y-1">
                Start Shopping
            </button>
        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in-up {
  animation: fade-in-up 0.8s ease-out forwards;
}
</style>