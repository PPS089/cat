FROM node:20-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json ./
ARG NPM_REGISTRY=https://registry.npmmirror.com
RUN npm config set registry "${NPM_REGISTRY}" && npm ci

COPY . .

ARG VITE_API_BASE
ARG VITE_APP_BASE_API=/api
ENV VITE_API_BASE=${VITE_API_BASE}
ENV VITE_APP_BASE_API=${VITE_APP_BASE_API}

RUN npm run build

FROM nginx:1.25-alpine

ENV API_UPSTREAM=http://backend:8080
ENV WS_UPSTREAM=http://backend:8080

COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
