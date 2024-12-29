// vite.config.js
import babel from 'vite-babel-plugin';
import react from '@vitejs/plugin-react';
import inject from "@rollup/plugin-inject";
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [
    inject({
      $: 'jquery',
      jQuery: 'jquery',
    }),
    babel(),
    react()
  ],
});
