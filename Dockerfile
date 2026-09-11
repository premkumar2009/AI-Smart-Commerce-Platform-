FROM node:22-alpine AS build

WORKDIR /app

ARG VITE_API_BASE_URL=https://shopsense-backend-dcrl.onrender.com
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build


FROM nginx:1.27-alpine

COPY --from=build /app/dist /usr/share/nginx/html

COPY docker-entrypoint-render.sh /docker-entrypoint-render.sh

RUN chmod +x /docker-entrypoint-render.sh

EXPOSE 80

ENTRYPOINT ["/docker-entrypoint-render.sh"]