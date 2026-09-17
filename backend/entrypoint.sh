#!/bin/bash
set -e

# Convert ca.pem to Java Keystore for Aiven MySQL SSL
if [ -n "$DB_URL" ] && echo "$DB_URL" | grep -q "trustCertificateKeyStoreUrl=file:ca.pem"; then
    echo "[entrypoint] Converting ca.pem to Java Keystore..."
    keytool -importcert -alias aiven-ca -file /app/ca.pem \
        -keystore /app/ca.jks -storepass changeit -noprompt 2>/dev/null || true

    if [ -f /app/ca.jks ]; then
        DB_URL=$(echo "$DB_URL" | sed 's|file:ca.pem|file:/app/ca.jks|g')
        DB_URL=$(echo "$DB_URL" | sed 's|trustCertificateKeyStoreType=PEM|trustCertificateKeyStoreType=JKS|g')
        export DB_URL
        echo "[entrypoint] SSL keystore ready. DB_URL updated."
    else
        echo "[entrypoint] WARNING: Keystore conversion failed. Falling back to SSL without certificate verification."
        DB_URL=$(echo "$DB_URL" | sed 's|sslMode=VERIFY_CA|sslMode=PREFERRED|g' | sed 's|&trustCertificateKeyStoreUrl=file:ca.pem||g' | sed 's|&trustCertificateKeyStoreType=PEM||g')
        export DB_URL
    fi
fi

echo "[entrypoint] Starting application..."
exec java -jar /app/app.jar
