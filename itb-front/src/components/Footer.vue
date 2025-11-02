<script setup>
import { useRouter } from 'vue-router';
import { theme } from '@/stores/themeStore.js';

const router = useRouter();
</script>

<template>
  <footer :class="theme === 'dark' ? 'bg-gray-950 text-slate-300' : 'bg-slate-100 text-slate-600'" 
          class="transition-colors duration-300">
    <div class="container mx-auto px-8 md:px-24 py-16">
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-12 text-center sm:text-left">
        
        <div class="sm:col-span-2 md:col-span-1">
          <h3 class="text-xl font-bold tracking-wider" :class="theme === 'dark' ? 'text-white' : 'text-slate-900'">
            ITB MSHOP
          </h3>
          <p class="mt-4 text-sm" :class="theme === 'dark' ? 'text-slate-400' : 'text-slate-500'">
            Your trusted partner in the world of mobile technology.
          </p>
        </div>

        <div>
          <h4 class="font-semibold tracking-wider" :class="theme === 'dark' ? 'text-slate-200' : 'text-slate-700'">
            Quick Links
          </h4>
          <div class="mt-4 space-y-3 text-sm">
            <router-link to="/" class="block hover:text-indigo-500 dark:hover:text-indigo-400 transition-colors">Home</router-link>
            <router-link to="/sale-items" class="block hover:text-indigo-500 dark:hover:text-indigo-400 transition-colors">All Phones</router-link>
            <router-link to="/registers" class="block hover:text-indigo-500 dark:hover:text-indigo-400 transition-colors">Register</router-link>
          </div>
        </div>

        <div>
          <h4 class="font-semibold tracking-wider" :class="theme === 'dark' ? 'text-slate-200' : 'text-slate-700'">
            Company
          </h4>
          <div class="mt-4 space-y-3 text-sm">
            <router-link to="/about" class="block hover:text-indigo-500 dark:hover:text-indigo-400 transition-colors">About Us</router-link>
            <router-link to="/contact" class="block hover:text-indigo-500 dark:hover:text-indigo-400 transition-colors">Contact Us</router-link>
            <router-link to="/privacy-policy" class="block hover:text-indigo-500 dark:hover:text-indigo-400 transition-colors">Privacy Policy</router-link>
            <router-link to="/terms-of-service" class="block hover:text-indigo-500 dark:hover:text-indigo-400 transition-colors">Terms of Service</router-link>
            <router-link to="/questions" class="block hover:text-indigo-500 dark:hover:text-indigo-400 transition-colors">FAQ</router-link>
          </div>
        </div>

        <div>
          <h4 class="font-semibold tracking-wider" :class="theme === 'dark' ? 'text-slate-200' : 'text-slate-700'">
            Follow Us
          </h4>
          <div class="mt-4 flex justify-center sm:justify-start space-x-5">
            <a href="#" class="hover:text-indigo-500 dark:hover:text-indigo-400 transition-transform transform hover:scale-110">
              <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M9 8h-3v4h3v12h5v-12h3.642l.358-4h-4v-1.667c0-.955.192-1.333 1.115-1.333h2.885v-5h-3.808c-3.596 0-5.192 1.583-5.192 4.615v2.385z"/></svg>
            </a>
            <a href="#" class="hover:text-indigo-500 dark:hover:text-indigo-400 transition-transform transform hover:scale-110">
              <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M24 4.557c-.883.392-1.832.656-2.828.775 1.017-.609 1.798-1.574 2.165-2.724-.951.564-2.005.974-3.127 1.195-.897-.957-2.178-1.555-3.594-1.555-3.179 0-5.515 2.966-4.797 6.045-4.091-.205-7.719-2.165-10.148-5.144-1.29 2.213-.669 5.108 1.523 6.574-.806-.026-1.566-.247-2.229-.616-.054 2.281 1.581 4.415 3.949 4.89-.693.188-1.452.232-2.224.084.626 1.956 2.444 3.379 4.6 3.419-2.07 1.623-4.678 2.348-7.29 2.04 2.179 1.397 4.768 2.212 7.548 2.212 9.142 0 14.307-7.721 13.995-14.646.962-.695 1.797-1.562 2.457-2.549z"/></svg>
            </a>
            <a href="#" class="hover:text-indigo-500 dark:hover:text-indigo-400 transition-transform transform hover:scale-110">
              <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.85s-.012 3.584-.07 4.85c-.148 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07s-3.584-.012-4.85-.07c-3.252-.148-4.771-1.691-4.919-4.919-.058-1.265-.069-1.645-.069-4.85s.012-3.584.07-4.85c.148-3.225 1.664-4.771 4.919-4.919 1.266-.057 1.644-.069 4.85-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948s.014 3.667.072 4.947c.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072s3.667-.014 4.947-.072c4.358-.2 6.78-2.618 6.98-6.98.059-1.281.073-1.689.073-4.948s-.014-3.667-.072-4.947c-.2-4.358-2.618-6.78-6.98-6.98-1.281-.059-1.689-.073-4.948-.073zm0 5.838c-3.403 0-6.162 2.759-6.162 6.162s2.759 6.162 6.162 6.162 6.162-2.759 6.162-6.162-2.759-6.162-6.162-6.162zm0 10.162c-2.209 0-4-1.79-4-4s1.791-4 4-4 4 1.79 4 4-1.791 4-4 4zm6.406-11.845c-.796 0-1.441.645-1.441 1.44s.645 1.44 1.441 1.44 1.441-.645 1.441-1.44-.645-1.44-1.441-1.44z"/></svg>
            </a>
          </div>
        </div>
      </div>

      <div class="mt-16 pt-8 border-t text-center text-sm" 
           :class="theme === 'dark' ? 'border-white/10 text-slate-400' : 'border-black/10 text-slate-500'">
        &copy; {{ new Date().getFullYear() }} ITB MSHOP. All Rights Reserved.
      </div>
    </div>
  </footer>
</template>

<style scoped>
</style>