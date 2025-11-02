<script setup>
import { computed, ref } from 'vue'; // ✨ FIX: เพิ่ม ref
import { useRouter } from 'vue-router';
import { theme } from '@/stores/themeStore.js';

const router = useRouter();

// --- Theming (มาจาก Store) ---
const themeClass = computed(() => {
  return theme.value === "dark"
    ? "dark bg-gray-900 text-slate-200"
    : "bg-slate-50 text-slate-800";
});

// --- ✨ FIX: เพิ่ม Mock Data สำหรับสมาชิกทีม ---
const teamMembers = ref([
  { id: 1, name: "Pratchaya Champates", role: "UX/UI / Infrastructure / Project Lead", imageUrl: "/sy4/profile/058.jpg" },
  { id: 2, name: "Pongsathorn Samklin", role: "Backend Developer", imageUrl: "/sy4/profile/118.jpg" },
  { id: 3, name: "Pawin Nacharean", role: "Frontend Developer", imageUrl: "/sy4/profile/121.jpg" },
]);
// --- (จบส่วน FIX) ---

</script>

<template>
  <div :class="themeClass" class="min-h-screen font-sans">
    <div class="container mx-auto px-6 py-12 max-w-4xl animate-fade-in-up">

      <div class="mb-12">
        <h1 class="text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white">
          About ITB MSHOP
        </h1>
        <p class="mt-4 text-xl" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
          A Modern Marketplace for Mobile Technology.
        </p>
      </div>

      <div 
        class="p-8 md:p-10 rounded-2xl space-y-8" 
        :class="theme === 'dark' ? 'bg-gray-800/30' : 'bg-white shadow-sm ring-1 ring-black/5'"
      >
        
        <section>
          <h2 class="text-2xl font-bold mb-4">Our Mission</h2>
          <p :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'" class="leading-relaxed">
            Welcome to ITB MSHOP, your new destination for mobile technology. Our mission is to create a clean, simple, and secure platform that connects buyers with sellers. We focus on providing a seamless user experience, whether you're here to find your next device or to manage your own storefront.
          </p>
        </section>

        <section class="pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
          <h2 class="text-2xl font-bold mb-6">How Our Platform Works</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="p-6 rounded-lg" :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'">
              <div class="flex items-center gap-3 mb-3">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-indigo-500 dark:text-indigo-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
                <h3 class="text-lg font-semibold">For Buyers</h3>
              </div>
              <ul class="list-disc list-inside space-y-2 text-sm" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
                <li>Browse a curated selection of products.</li>
                <li>Filter items by brand, price, and storage.</li>
                <li>Add items to your personal cart.</li>
                <li>Manage your profile and order history.</li>
                <li>Securely log in with Two-Factor Authentication (OTP).</li>
              </ul>
            </div>
            <div class="p-6 rounded-lg" :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'">
              <div class="flex items-center gap-3 mb-3">
                 <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-indigo-500 dark:text-indigo-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                   <path stroke-linecap="round" stroke-linejoin="round" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
                 </svg>
                <h3 class="text-lg font-semibold">For Sellers</h3>
              </div>
              <ul class="list-disc list-inside space-y-2 text-sm" :class="theme === 'dark' ? 'text-slate-300' : 'text-slate-600'">
                <li>Register and manage your seller profile.</li>
                <li>Add, edit, and delete your product listings.</li>
                <li>Manage your brand information.</li>
                <li>View incoming orders from buyers.</li>
                <li>Securely manage your store with a dedicated dashboard.</li>
              </ul>
            </div>
          </div>
        </section>

        <section class="pt-6 border-t" :class="theme === 'dark' ? 'border-white/10' : 'border-slate-200'">
          <h2 class="text-2xl font-bold mb-6">Meet the Team</h2>
          <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
            
            <div 
              v-for="member in teamMembers" 
              :key="member.id" 
              class="text-center p-6 rounded-lg"
              :class="theme === 'dark' ? 'bg-gray-700/50' : 'bg-slate-100'"
            >
              <img 
                :src="member.imageUrl" 
                :alt="member.name"
                class="w-24 h-24 rounded-full mx-auto mb-4 object-cover ring-4"
                :class="theme === 'dark' ? 'ring-gray-600' : 'ring-slate-200'"
              >
              <h3 class="font-bold text-lg" :class="theme === 'dark' ? 'text-white' : 'text-slate-900'">
                {{ member.name }}
              </h3>
              <p class="text-sm text-indigo-500 dark:text-indigo-400">
                {{ member.role }}
              </p>
            </div>

          </div>
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