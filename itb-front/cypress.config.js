import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    specPattern: 'cypress/e2e/**/*.{cy,spec}.{js,jsx,ts,tsx}',
    baseUrl: 'http://ip24sy4.sit.kmutt.ac.th:5173',
    baseAPI: 'http://ip24sy4.sit.kmutt.ac.th:8080',
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
});
