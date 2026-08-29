import type { NextConfig } from "next";

/*
 * Workaround for Next.js 16.3 with Vercel:
 * `output: "standalone"` can cause the Vercel build to fail because
 * `.next/next-server.js.nft.json` is missing during the build.
 * Keep standalone output for Docker/local builds, but disable it on Vercel.
 */
const nextConfig: NextConfig = {
  output: process.env.VERCEL ? undefined : "standalone",
};

export default nextConfig;
