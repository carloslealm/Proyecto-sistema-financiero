# ============================================================
#  LoanManager Frontend — Dockerfile
#  Multi-stage: Node para compilar, Nginx para servir
# ============================================================

# ── Etapa 1: Build Angular ───────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# Copiar package.json primero para cache de dependencias
COPY package*.json ./
RUN npm ci --silent

# Copiar código y compilar para producción
COPY . .
RUN npm run build -- --configuration=production


# ── Etapa 2: Servir con Nginx ────────────────────────────────
# Nginx es el servidor web más usado para SPAs en producción.
# Es extremadamente liviano y eficiente.
FROM nginx:alpine

# Copiar los archivos compilados de Angular al directorio de Nginx
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# Copiar configuración personalizada de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
