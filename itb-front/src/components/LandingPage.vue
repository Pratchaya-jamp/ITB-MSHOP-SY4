<script setup>
import { useRouter } from 'vue-router'
import { ref } from 'vue'

const router = useRouter()

const navigateToSaleItems = () => {
  router.push('/sale-items')
}
const navigateToBrands = () => {
  router.push('/sale-items/list')
}
const services = ref(null)
const contact = ref(null)
const scrollTo = (target) => {
  target?.scrollIntoView({ behavior: 'smooth' })
}
</script>

<template>
<header class="bg-gray-900 text-white px-6 md:px-20 py-4 flex justify-between items-center">
  <div class="text-xl font-bold">🌟ITB MSHOP</div>
  <nav class="space-x-6 hidden md:block">
    <a href="#" class="hover:text-yellow-400">
      <router-link to="/">Home</router-link>
    </a>
    <a href="#" @click.prevent="scrollTo(services)" class="hover:text-yellow-400 transition">Services</a>
    <a href="#" @click.prevent="scrollTo(contact)" class="hover:text-yellow-400 transition">Contact</a>
  </nav>
  <button class="bg-yellow-400 text-black px-4 py-1 rounded-full text-sm hover:bg-yellow-300 transition">Login</button>
</header>
  <div class="max-h-screen bg-gradient-to-r from-teal-400 to-gray-800 text-white font-sans overflow-hidden">
    <!-- Hero Section -->
    <div class="flex flex-col-reverse md:flex-row items-center justify-between px-6 md:px-20 py-20 gap-10">
      <!-- Text Content -->
      <div class="text-center md:text-left max-w-xl space-y-6 animate-fade-in-up">
        <h1 class="text-5xl font-extrabold text-white leading-tight">
          Find Your Perfect <span class="text-yellow-400">Smartphone</span><br />
          Right Here
        </h1>
        <p class="text-lg opacity-80">
          Explore our curated collection of the latest smartphones. Trusted brands, amazing prices, and lightning-fast delivery — all in one place.
        </p>
        <div class="flex items-center mt-4">
          <button
            @click="navigateToSaleItems"
            class="itbms-shopnow px-8 py-3 bg-yellow-400 hover:bg-yellow-300 text-black font-semibold rounded-full shadow-lg transition duration-300 transform hover:-translate-y-1 hover:scale-105 mr-4"
          >
            Shop Now
          </button>
          <button
            @click="navigateToBrands"
            class="itbms-seller px-8 py-3 bg-white hover:bg-gray-100 text-gray-800 font-semibold rounded-full shadow-lg transition duration-300 transform hover:-translate-y-1 hover:scale-105"
          >
            Seller
          </button>
        </div>
      </div>

      <!-- Phone Image -->
      <div class="relative animate-fade-in-down">
        <img
          src="/phone/Thumbnail.png"
          alt="Mobile Phone"
          class="w-80 md:w-96 rounded-3xl shadow-2xl transform hover:scale-105 transition duration-500"
        />
        <div class="absolute top-4 left-4 bg-white/10 backdrop-blur-lg px-4 py-2 rounded-xl text-sm shadow-inner">
          NEW
        </div>
      </div>
    </div>

    <!-- Features -->
    <div ref="features" class="bg-white text-gray-900 px-6 md:px-20 py-20 rounded-t-3xl -mt-10 z-10 relative animate-fade-in-up">
      <h2 class="text-3xl md:text-4xl font-bold text-center mb-12">Why Shop With Us?</h2>
      <div class="grid md:grid-cols-3 gap-8">
        <div class="p-6 rounded-2xl bg-gradient-to-br from-indigo-100 to-white shadow-md transition-transform transform hover:-translate-y-2 hover:shadow-xl">
          <h3 class="text-xl font-semibold mb-2">Top-Quality Devices</h3>
          <p>We offer only the latest and most reliable smartphones from trusted brands — 100% genuine and guaranteed.</p>
        </div>
        <div class="p-6 rounded-2xl bg-gradient-to-br from-purple-100 to-white shadow-md transition-transform transform hover:-translate-y-2 hover:shadow-xl">
          <h3 class="text-xl font-semibold mb-2">Unbeatable Prices</h3>
          <p>Get the best deals on your favorite phones. Daily discounts, bundle offers, and flexible payment plans.</p>
        </div>
        <div class="p-6 rounded-2xl bg-gradient-to-br from-pink-100 to-white shadow-md transition-transform transform hover:-translate-y-2 hover:shadow-xl">
          <h3 class="text-xl font-semibold mb-2">Fast & Trusted Service</h3>
          <p>Enjoy fast shipping, friendly support, and hassle-free warranties. Your satisfaction is our top priority.</p>
        </div>
      </div>
    </div>
  </div>

  <div ref="services" class="bg-white text-gray-900 px-6 md:px-20 py-20 animate-fade-in-up">
  <h2 class="text-3xl md:text-4xl font-bold text-center mb-12">Our Services</h2>
  <div class="grid md:grid-cols-3 gap-8">
    <div class="p-6 rounded-2xl bg-gradient-to-br from-indigo-100 to-white shadow-md transition-transform transform hover:-translate-y-2 hover:shadow-xl">
      <h3 class="text-xl font-semibold mb-2">Expert Consultation</h3>
      <p>Talk to our tech experts to find the right phone for your needs.</p>
    </div>
    <div class="p-6 rounded-2xl bg-gradient-to-br from-purple-100 to-white shadow-md transition-transform transform hover:-translate-y-2 hover:shadow-xl">
      <h3 class="text-xl font-semibold mb-2">Device Protection Plans</h3>
      <p>Affordable warranty and insurance plans for peace of mind.</p>
    </div>
    <div class="p-6 rounded-2xl bg-gradient-to-br from-pink-100 to-white shadow-md transition-transform transform hover:-translate-y-2 hover:shadow-xl">
      <h3 class="text-xl font-semibold mb-2">Fast Delivery</h3>
      <p>Same-day or next-day delivery in select areas!</p>
    </div>
  </div>
</div>

<!-- Contact Section -->
<div ref="contact" class="bg-gray-100 text-gray-900 px-6 md:px-20 py-20 animate-fade-in-up">
  <h2 class="text-3xl md:text-4xl font-bold text-center mb-8">Contact Us</h2>
  <form class="max-w-xl mx-auto space-y-4">
    <input type="text" placeholder="Your Name" class="w-full p-3 rounded border border-gray-300" />
    <input type="email" placeholder="Your Email" class="w-full p-3 rounded border border-gray-300" />
    <textarea rows="4" placeholder="Your Message" class="w-full p-3 rounded border border-gray-300"></textarea>
    <button class="bg-yellow-400 hover:bg-yellow-300 text-black px-6 py-2 rounded font-semibold">Send Message</button>
  </form>
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

html {
  scroll-behavior: smooth;
}
</style>
