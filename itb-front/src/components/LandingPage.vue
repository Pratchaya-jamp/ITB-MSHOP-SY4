<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const navigateToSaleItems = () => {
  router.push('/sale-items')
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-r from-teal-400 to-gray-800 text-white font-sans overflow-hidden">
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
        <button
          @click="navigateToSaleItems"
          class="mt-4 px-8 py-3 bg-yellow-400 hover:bg-yellow-300 text-black font-semibold rounded-full shadow-lg transition duration-300 transform hover:-translate-y-1 hover:scale-105"
        >
          Shop Now
        </button>
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
</style>
