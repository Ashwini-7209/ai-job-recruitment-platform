#!/bin/bash
set -e

if [ -n "$DB_URL" ]; then
    DB_URL=$(echo "$DB_URL" | sed 's|file:ca.pem|file:/app/ca.jks|g')
    DB_URL=$(echo "$DB_URL" | sed 's|trustCertificateKeyStoreType=PEM|trustCertificateKeyStoreType=JKS|g')
    export DB_URL
    echo "[entrypoint] SSL keystore configured."
fi

echo "[entrypoint] Starting application..."
exec java -jar /app/app.jar
