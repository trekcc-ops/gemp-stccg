// vite.config.js
import react from '@vitejs/plugin-react';
import { resolve } from 'path';
import { defineConfig } from 'vite';

export default defineConfig({
  base: "/gemp-module/",
  server: {
    port: "17001"
  },
  build: {
    sourcemap: true,
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html'),
        hall: resolve(__dirname, 'hall.html'),
        game: resolve(__dirname, 'game.html'),
        deckBuild: resolve(__dirname, 'deckBuild.html'),
        soloDraft: resolve(__dirname, 'soloDraft.html')
      },
    },
  },
  plugins: [
    react()
  ],
});