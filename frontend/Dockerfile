FROM node:22-alpine AS deps

WORKDIR /app

COPY package*.json .npmrc ./
RUN npm ci

FROM deps AS validator
COPY . .
RUN npm run lint

FROM deps AS builder
COPY . .
RUN npm run build

FROM nginx:1.29-alpine

COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
COPY generated-docs /usr/share/nginx/html/docs

EXPOSE 80
