import type { NextConfig } from "next"

const nextConfig: NextConfig = {
  eslint: {
    ignoreDuringBuilds: true,
  },
  typescript: {
    ignoreBuildErrors: true,
  },
  // Optimisations de production
  compress: true,
  poweredByHeader: false,
  
  // Lazy loading et code splitting
  experimental: {
    optimizePackageImports: ['lucide-react', '@radix-ui/react-icons'],
  },

  // Images
  images: {
    formats: ['image/avif', 'image/webp'],
    remotePatterns: [],
    unoptimized: process.env.NODE_ENV === 'development',
  },

  // Variables d'environnement
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:7000/api',
    NEXT_PUBLIC_WS_URL: process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:7000/ws',
  },

  // Headers de sécurité
  async headers() {
    return [
      {
        source: '/:path*',
        headers: [
          {
            key: 'X-DNS-Prefetch-Control',
            value: 'on'
          },
          {
            key: 'X-Frame-Options',
            value: 'SAMEORIGIN'
          },
          {
            key: 'X-Content-Type-Options',
            value: 'nosniff'
          },
          {
            key: 'Referrer-Policy',
            value: 'strict-origin-when-cross-origin'
          },
        ],
      },
    ]
  },
}

export default nextConfig
