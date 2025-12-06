import { Roboto_Mono } from "next/font/google";
import "./globals.css";
import { Metadata } from "next";
import { V0Provider } from "@/lib/v0-context";
import { AuthProvider } from "@/contexts/AuthContext";
import localFont from "next/font/local";
import { DashboardLayout } from "@/components/DashboardLayout";
import { Toaster } from "@/components/ui/sonner";
import { WebSocketProvider } from "@/components/WebSocketProvider";
import { QueryProvider } from "@/providers/QueryProvider";

const robotoMono = Roboto_Mono({
  variable: "--font-roboto-mono",
  subsets: ["latin"],
});

const rebelGrotesk = localFont({
  src: "../public/fonts/Rebels-Fett.woff2",
  variable: "--font-rebels",
  display: "swap",
});

const isV0 = process.env["VERCEL_URL"]?.includes("vusercontent.net") ?? false;

export const metadata: Metadata = {
  title: {
    template: "%s – MaVille",
    default: "MaVille Dashboard",
  },
  description:
    "MaVille - Public Works Management System for Montreal",
    generator: 'v0.app'
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="dark">
      <head>
        <link
          rel="preload"
          href="/fonts/Rebels-Fett.woff2"
          as="font"
          type="font/woff2"
          crossOrigin="anonymous"
        />
      </head>
      <body
        className={`${rebelGrotesk.variable} ${robotoMono.variable} antialiased`}
      >
        <V0Provider isV0={isV0}>
          <QueryProvider>
            <AuthProvider>
              <WebSocketProvider>
                <DashboardLayout>
                  {children}
                </DashboardLayout>
                <Toaster />
              </WebSocketProvider>
            </AuthProvider>
          </QueryProvider>
        </V0Provider>
      </body>
    </html>
  );
}
