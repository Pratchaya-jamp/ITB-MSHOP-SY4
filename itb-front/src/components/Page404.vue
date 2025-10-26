<script setup>
import { useRouter } from 'vue-router';
import { theme } from '@/stores/themeStore.js'; 
import { computed } from 'vue';

const router = useRouter();

const themeClass = computed(() => {
    return theme.value === 'dark'
        ? 'bg-gray-950 text-white'
        : 'bg-white text-gray-950'
})
</script>

<template>
  <div :class="themeClass" class="relative min-h-screen font-sans flex items-center justify-center text-center p-6 overflow-hidden transition-colors duration-300">
    <div class="absolute inset-0 w-full h-full opacity-10 pointer-events-none">
      <div class="absolute w-96 h-96 bg-indigo-500 rounded-full blur-3xl opacity-30 top-1/4 left-1/4 animate-blob"></div>
      <div class="absolute w-80 h-80 bg-sky-500 rounded-full blur-3xl opacity-30 bottom-1/4 right-1/4 animate-blob animation-delay-2000"></div>
    </div>

    <div class="relative z-10 animate-fade-in-up">
      <h1 class="text-8xl md:text-9xl font-extrabold tracking-tighter text-transparent bg-clip-text bg-gradient-to-r from-indigo-500 to-sky-500">
        404
      </h1>
      <h2 class="mt-4 text-3xl md:text-4xl font-bold tracking-tight" :class="theme === 'dark' ? 'text-white' : 'text-slate-900'">
        Page Not Found
      </h2>
      <p class="mt-4 max-w-md mx-auto text-lg" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
        Sorry, the page you are looking for doesn’t exist or has been moved. Let's get you back on track.
      </p>
      <div class="mt-10">
        <button @click="() => router.push('/')" class="px-8 py-3 bg-indigo-600 text-white font-semibold rounded-full shadow-lg shadow-indigo-500/20 hover:bg-indigo-700 transition-all duration-300 transform hover:-translate-y-1">
          Go Back Home
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Animations for a smooth entrance */
@keyframes fade-in-up {
  from { 
    opacity: 0; 
    transform: translateY(30px) scale(0.98); 
  }
  to { 
    opacity: 1; 
    transform: translateY(0) scale(1); 
  }
}

.animate-fade-in-up { 
    animation: fade-in-up 0.8s ease-out forwards; 
}

@keyframes blob {
  0% { transform: scale(1) translate(0px, 0px); }
  33% { transform: scale(1.1) translate(30px, -50px); }
  66% { transform: scale(0.9) translate(-20px, 20px); }
  100% { transform: scale(1) translate(0px, 0px); }
}

.animate-blob { 
    animation: blob 8s infinite ease-in-out; 
}

.animation-delay-2000 { 
    animation-delay: 2s; 
}
</style>