<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { theme } from '@/stores/themeStore.js';

const router = useRouter();

// --- Theming (มาจาก Store) ---
const themeClass = computed(() => {
  return theme.value === "dark"
    ? "dark bg-gray-900 text-slate-200"
    : "bg-slate-50 text-slate-800";
});
</script>

<template>
  <div :class="themeClass" class="min-h-screen font-sans">
    <div class="container mx-auto px-6 py-12 max-w-4xl animate-fade-in-up">

      <div class="mb-12">
        <h1 class="text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white">
          Terms of Service
        </h1>
        <p class="mt-4 text-xl" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
          Last updated: November 2, 2025
        </p>
      </div>

      <div 
        class="p-8 md:p-10 rounded-2xl space-y-8" 
        :class="theme === 'dark' ? 'bg-gray-800/30' : 'bg-white shadow-sm ring-1 ring-black/5'"
      >
        
        <section>
          <h2 class="text-2xl font-bold mb-4 text-yellow-500 dark:text-yellow-400">1. Important Notice: Academic Project</h2>
          <p :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'" class="leading-relaxed font-semibold">
            This website ("ITB MSHOP") is a non-commercial, academic project created for the course **INT222 Integrated Project II** at the Faculty of Information Technology (IT), King Mongkut's University of Technology Thonburi (KMUTT).
          </p>
          <p :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'" class="leading-relaxed mt-4">
            All data, products, prices, and user information on this site are for demonstration and educational purposes only. **No real transactions** are processed, and **no real products** are sold or shipped. Do not enter any real personal information (e.g., credit card numbers, real addresses) other than what is required for testing purposes.
          </p>
        </section>

        <section class="pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
          <h2 class="text-2xl font-bold mb-4">2. Acceptance of Terms</h2>
          <p :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'" class="leading-relaxed">
            By accessing or using this website, you agree to be bound by these Terms of Service. If you disagree with any part of the terms, you may not access the service.
          </p>
        </section>

        <section class="pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
          <h2 class="text-2xl font-bold mb-4">3. User Accounts</h2>
          <p :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'" class="leading-relaxed">
            As this is an academic project, you may be required to create an account for testing purposes (e.g., as a "Buyer" or "Seller"). You are responsible for any activity that occurs under your test account.
          </p>
        </section>
        
        <section class="pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
          <h2 class="text-2xl font-bold mb-4">4. Disclaimer of Warranties</h2>
          <p :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'" class="leading-relaxed">
            This website is provided **"AS IS"** and **"AS AVAILABLE"** for educational purposes. We make no warranties, expressed or implied, regarding the operation or availability of the site, or the information, content, or materials included herein. We do not warrant that the site will be uninterrupted, error-free, or secure.
          </p>
        </section>

        <section class="pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
          <h2 class="text-2xl font-bold mb-4">5. Limitation of Liability</h2>
          <p :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'" class="leading-relaxed">
            In no event shall the development team or King Mongkut's University of Technology Thonburi (KMUTT) be liable for any claims, damages, or losses (including data loss or site downtime) arising from the use of this project website.
          </p>
        </section>

        <div class="pt-8 border-t text-center" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
            <button 
                @click="router.go(-1)" 
                class="w-full sm:w-auto px-8 py-3 bg-indigo-600 text-white font-semibold rounded-full shadow-lg shadow-indigo-500/20 transition-all duration-300 transform hover:-translate-y-1">
                Go Back
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