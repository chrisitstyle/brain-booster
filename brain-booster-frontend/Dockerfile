FROM node:24-alpine3.23 AS deps

WORKDIR /app

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./

RUN corepack enable && pnpm install --frozen-lockfile


FROM node:24-alpine3.23 AS builder

WORKDIR /app

COPY --from=deps /app/node_modules ./node_modules
COPY . .

ARG NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL

RUN corepack enable && pnpm build


FROM node:24-alpine3.23 AS runner

WORKDIR /app

ENV NODE_ENV=production
ENV PORT=3000
ENV HOSTNAME=0.0.0.0

# the 'node' user and group are built into the image, so skip creation
# copy built files from the builder stage and set ownership to the built-in 'node' user / u:g
COPY --chown=node:node --from=builder /app/public ./public
COPY --chown=node:node --from=builder /app/.next/standalone ./
COPY --chown=node:node --from=builder /app/.next/static ./.next/static

# switch to the non-root user before running the application
USER node

EXPOSE 3000

CMD ["node", "server.js"]