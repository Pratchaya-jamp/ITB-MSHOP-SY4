<script setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { theme } from '@/stores/themeStore.js';

const router = useRouter();

// --- Theming (มาจาก Store) ---
const themeClass = computed(() => {
  return theme.value === "dark"
    ? "dark bg-gray-900 text-slate-200"
    : "bg-slate-50 text-slate-800";
});

// --- Mock Data สำหรับสมาชิกทีม ---
const teamMembers = ref([
  { 
    id: 1, 
    name: "Pratchaya Champates", 
    role: "UX/UI / Infrastructure / Project Lead", 
    imageUrl: "/sy4/profile/058.jpg",
    socials: {
      github: "https://github.com/Pratchaya-jamp",
      facebook: "https://www.facebook.com/pratchjp",
      instagram: "https://www.instagram.com/pratch.jp/"
    }
  },
  { 
    id: 2, 
    name: "Pongsathorn Samklin", 
    role: "Backend Developer", 
    imageUrl: "/sy4/profile/118.jpg",
    socials: {
      github: "https://github.com/ayeyuzu34",
      facebook: "https://www.facebook.com/pongsathorn.samklin",
      instagram: "https://www.instagram.com/samklin34/"
    }
  },
  { 
    id: 3, 
    name: "Pawin Nacharean", 
    role: "Frontend Developer", 
    imageUrl: "/sy4/profile/121.jpg",
    socials: {
      github: "https://github.com/Pawin-nacharean",
      facebook: "https://www.facebook.com/ph.wi.nth.na.ceriy",
      instagram: "https://www.instagram.com/pa_win147/"
    }
  },
]);

</script>

<template>
  <div :class="themeClass" class="min-h-screen font-sans">
    <div class="container mx-auto px-6 py-12 max-w-4xl animate-fade-in-up">

      <div class="mb-12">
        <h1 class="text-4xl font-extrabold tracking-tight" :class="theme === 'dark' ? 'text-gray-300' : 'text-gray-600'">
          Contact Us
        </h1>
        <p class="mt-4 text-xl" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
          Get in touch with the team behind ITB MSHOP.
        </p>
      </div>

      <div 
        class="p-8 md:p-10 rounded-2xl space-y-8" 
        :class="theme === 'dark' ? 'bg-gray-800/30' : 'bg-white shadow-sm ring-1 ring-black/5'"
      >
        <section>
          <h2 class="text-2xl font-bold mb-6">Meet the Team</h2>
          <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
            
            <div 
              v-for="member in teamMembers" 
              :key="member.id" 
              class="text-center p-6 rounded-lg transition-all duration-300"
              :class="theme === 'dark' ? 'bg-gray-700/50 hover:bg-gray-700' : 'bg-slate-100 hover:shadow-lg'"
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
              <p class="text-sm text-indigo-500 dark:text-indigo-400 mb-4">
                {{ member.role }}
              </p>
              
              <div class="flex items-center justify-center gap-4">
                <a v-if="member.socials.github" :href="member.socials.github" target="_blank" 
                   class="p-2 rounded-full transition-colors"
                   :class="theme === 'dark' ? 'text-slate-400 hover:text-white hover:bg-gray-600' : 'text-slate-500 hover:text-slate-900 hover:bg-slate-200'">
                  <svg class="w-5 h-5" role="img" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"/></svg>
                </a>
                <a v-if="member.socials.facebook" :href="member.socials.facebook" target="_blank" 
                   class="p-2 rounded-full transition-colors"
                   :class="theme === 'dark' ? 'text-slate-400 hover:text-white hover:bg-gray-600' : 'text-slate-500 hover:text-slate-900 hover:bg-slate-200'">
                  <svg class="w-5 h-5" role="img" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/></svg>
                </a>
                <a v-if="member.socials.instagram" :href="member.socials.instagram" target="_blank" 
                   class="p-2 rounded-full transition-colors"
                   :class="theme === 'dark' ? 'text-slate-400 hover:text-white hover:bg-gray-600' : 'text-slate-500 hover:text-slate-900 hover:bg-slate-200'">
                  <svg class="w-5 h-5" role="img" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path d="M12 0C8.74 0 8.333.015 7.053.072 5.775.132 4.905.333 4.14.63c-.789.306-1.459.717-2.126 1.384S.935 3.35.63 4.14C.333 4.905.131 5.775.072 7.053.012 8.333 0 8.74 0 12s.015 3.667.072 4.947c.06 1.277.261 2.148.558 2.913.306.788.717 1.459 1.384 2.126.667.666 1.336 1.079 2.126 1.384.766.296 1.636.499 2.913.558C8.333 23.988 8.74 24 12 24s3.667-.015 4.947-.072c1.277-.06 2.148-.262 2.913-.558.788-.306 1.459-.718 2.126-1.384.666-.667 1.079-1.335 1.384-2.126.296-.765.499-1.636.558-2.913.06-1.28.072-1.687.072-4.947s-.015-3.667-.072-4.947c-.06-1.277-.262-2.148-.558-2.913-.306-.789-.718-1.459-1.384-2.126C21.314.935 20.644.523 19.854.218 19.09.08 18.22.06 16.947.003 15.667 0 15.26 0 12 0zm0 2.16c3.203 0 3.585.016 4.85.071 1.17.055 1.805.249 2.227.415.562.217.96.477 1.382.896.419.42.679.819.896 1.381.164.422.36 1.057.413 2.227.057 1.266.07 1.646.07 4.85s-.015 3.585-.074 4.85c-.056 1.17-.249 1.805-.413 2.227-.217.562-.477.96-.896 1.381-.42.419-.82.679-1.38.896-.423.164-1.057.36-2.227.413-1.266.057-1.646.07-4.85.07s-3.585-.015-4.85-.07c-1.17-.056-1.805-.249-2.227-.413-.563-.217-.96-.477-1.381-.896-.419-.42-.679-.82-1.381-.896-.164-.423-.36-1.057-.413-2.227-.057-1.266-.07-1.646-.07-4.85s.015-3.585.07-4.85c.056-1.17.249-1.805.413-2.227.217-.563.477-.96.896-1.381.42-.419.82-.679 1.381-.896.423-.164 1.057-.36 2.227-.413 1.266-.057 1.646-.07 4.85-.07zM12 5.838a6.162 6.162 0 100 12.324 6.162 6.162 0 000-12.324zM12 16c-2.21 0-4-1.79-4-4s1.79-4 4-4 4 1.79 4 4-1.79 4-4 4zm7.846-10.405a1.441 1.441 0 11-2.88 0 1.441 1.441 0 012.88 0z"/></svg>
                </a>
              </div>
            </div>

          </div>
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