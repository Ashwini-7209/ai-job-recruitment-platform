#!/bin/bash
set -e

# Update JDBC URL to point to the pre-built JKS keystore
if [ -n "$DB_URL" ] && echo "$DB_URL" | grep -q "trustCertificateKeyStoreUrl=file:ca.pem"; then
    DB_URL=$(echo "$DB_URL" | sed 's|file:ca.pem|file:/app/ca.jks|g')
    DB_URL=$(echo "$DB_URL" | sed 's|trustCertificateKeyStoreType=PEM|trustCertificateKeyStoreType=JKS|g')
    export DB_URL
    echo "[entrypoint] SSL keystore configured."
fi

echo "[entrypoint] Starting application..."
exec java -jar /app/app.jar
