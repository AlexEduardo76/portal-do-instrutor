import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { VitePWA } from "vite-plugin-pwa";

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: "autoUpdate",
      includeAssets: [
        "img/logo-senac.png",
        "icons/icon-192.png",
        "icons/icon-512.png",
        "icons/icon-maskable-512.png"
      ],
      manifest: {
        id: "/",
        name: "Portal do Instrutor",
        short_name: "Portal Instrutor",
        description: "Gestão acadêmica para instrutores Senac.",
        lang: "pt-BR",
        theme_color: "#004A8D",
        background_color: "#F4F7FB",
        display: "standalone",
        display_override: ["standalone", "minimal-ui"],
        orientation: "portrait",
        start_url: "/#/dashboard",
        scope: "/",
        categories: ["education", "productivity"],
        shortcuts: [
          {
            name: "Frequência",
            short_name: "Frequência",
            url: "/#/frequencia",
            icons: [{ src: "/icons/icon-192.png", sizes: "192x192" }]
          },
          {
            name: "Notas",
            short_name: "Notas",
            url: "/#/notas",
            icons: [{ src: "/icons/icon-192.png", sizes: "192x192" }]
          },
          {
            name: "Turmas",
            short_name: "Turmas",
            url: "/#/turmas",
            icons: [{ src: "/icons/icon-192.png", sizes: "192x192" }]
          }
        ],
        icons: [
          {
            src: "/icons/icon-192.png",
            sizes: "192x192",
            type: "image/png",
            purpose: "any"
          },
          {
            src: "/icons/icon-512.png",
            sizes: "512x512",
            type: "image/png",
            purpose: "any"
          },
          {
            src: "/icons/icon-maskable-512.png",
            sizes: "512x512",
            type: "image/png",
            purpose: "maskable"
          }
        ]
      },
      workbox: {
        navigateFallback: "/index.html",
        navigateFallbackDenylist: [/^\/api/],
        globPatterns: ["**/*.{js,css,html,png,jpeg,svg,webmanifest}"],
        runtimeCaching: [
          {
            urlPattern: /\/api\//,
            handler: "NetworkOnly"
          }
        ]
      }
    })
  ],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: process.env.VITE_PROXY_TARGET || "http://localhost:8080",
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: "dist",
    sourcemap: false
  }
});
