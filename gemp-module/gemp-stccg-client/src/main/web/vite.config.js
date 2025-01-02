// vite.config.js
import babel from 'vite-babel-plugin';
import react from '@vitejs/plugin-react';
import inject from "@rollup/plugin-inject";
import { resolve } from 'path';
import { defineConfig } from 'vite';

export default defineConfig({
  base: "/gemp-module/",
  build: {
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
    inject({
      $: 'jquery',
      jQuery: 'jquery',
    }),
    babel(),
    react()
  ],
});
