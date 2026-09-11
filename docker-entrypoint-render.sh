#!/bin/sh
set -eu

if [ -z "${BACKEND_URL:-}" ]; then
    echo "ERROR: BACKEND_URL environment variable is required"
    exit 1
fi

cat > /etc/nginx/conf.d/default.conf <<EOF
server {
    listen 80;
    server_name _;

    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files \$uri \$uri/ /index.html;
    }

    location /api/ {
        proxy_pass ${BACKEND_URL};

        proxy_ssl_server_name on;
        proxy_ssl_name shopsense-backend-dcrl.onrender.com;
        proxy_ssl_protocols TLSv1.2 TLSv1.3;

        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

exec nginx -g 'daemon off;'
