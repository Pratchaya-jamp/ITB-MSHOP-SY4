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
        <h1 class="text-4xl font-extrabold tracking-tight" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">
          Privacy Policy
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
          <h2 class="text-2xl font-bold mb-4">1. Introduction</h2>
          <p class="leading-relaxed mb-4" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
            This Privacy Policy explains how ITB MSHOP ("we," "us," or "our") collects, uses, and discloses your information.
          </p>
          <div class="p-4 rounded-lg" :class="theme === 'dark' ? 'bg-yellow-900/30 border border-yellow-500/30' : 'bg-yellow-50 border border-yellow-200'">
            <p class="font-semibold" :class="theme === 'dark' ? 'text-yellow-300' : 'text-yellow-700'">
              **Academic Project Notice:** This website is an academic project for **INT222 Integrated Project II** at **KMUTT**. It is not a real commercial service. Please do not use real, sensitive personal information.
            </p>
          </div>
        </section>

        <section class="pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
          <h2 class="text-2xl font-bold mb-4">2. Information We Collect</h2>
          <p class="mb-4" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
            We collect the following information solely for the purpose of demonstrating the functionality of this project:
          </p>
          <ul class="list-disc list-inside space-y-2 text-sm" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
            <li>**Account Information:** Your email address, hashed password, nickname, and full name.</li>
            <li>**Seller Information (if applicable):** Bank name and bank account number (for demonstration purposes only).</li>
            <li>**Order Information:** Shipping address and items purchased (for demonstrating order history).</li>
          </ul>
        </section>
        
        <section class="pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
          <h2 class="text-2xl font-bold mb-4">3. How We Use Your Information</h2>
          <p class="mb-4" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
            Your information is used exclusively to:
          </p>
          <ul class="list-disc list-inside space-y-2 text-sm" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
            <li>Authenticate your login and verify your identity via One-Time Password (OTP).</li>
            <li>Allow you to post items for sale (as a Seller) or purchase items (as a Buyer).</li>
            <li>Display your profile and order history.</li>
            <li>Demonstrate the "Remember Me" functionality.</li>
          </ul>
        </section>

        <section class="pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
          <h2 class="text-2xl font-bold mb-4">4. Data Sharing and Security</h2>
          <p class="mb-4" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
            We do not share your information with any third-party advertisers or external companies. All data is stored within the project's database for grading and demonstration. Passwords are always hashed and never stored in plain text.
          </p>
        </section>

        <section class="pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
          <h2 class="text-2xl font-bold mb-4">5. Cookies and Tokens</h2>
          <p class="mb-4" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
            We use essential tokens (Access, Refresh, RememberMe) to manage your session security. We do not use tracking or analytical cookies. For more details, please see our <router-link to="/cookie-policy" class="font-semibold text-indigo-500 dark:text-indigo-400 hover:underline">Cookie Policy</router-link>.
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