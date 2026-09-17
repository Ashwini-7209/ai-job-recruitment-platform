# Deployment Guide

## Prerequisites

- Docker 20.10+ and Docker Compose v2
- A server with ports 80/443 and 8080 available
- Domain name (for production HTTPS)

## Quick Start (Docker)

```bash
# 1. Clone the repository
git clone <repo-url>
cd job-portal

# 2. Configure environment
cp .env.production.example .env.production
# Edit .env.production with real credentials:
#   - Strong JWT_SECRET (openssl rand -base64 48)
#   - Strong DB passwords
#   - Your domain in CORS_ALLOWED_ORIGINS

# 3. Build and start
docker compose --env-file .env.production up -d --build

# 4. Verify
docker compose ps
curl http://localhost:8080/api/health
```

## Architecture

```
Internet → HTTPS (Nginx/Caddy) → :3000 Frontend (Nginx)
                                       ↓ /api proxy
                                   :8080 Backend (Spring Boot)
                                       ↓
                                   MySQL (internal:3306)
```

- **Frontend**: Nginx serves static files + proxies `/api` to backend
- **Backend**: Spring Boot with Flyway auto-migration
- **Database**: MySQL 8.0, data persisted in Docker volume

## Reverse Proxy (HTTPS)

Use Nginx or Caddy as a reverse proxy in front of the Docker stack:

### Nginx Example

```nginx
server {
    listen 443 ssl http2;
    server_name yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

server {
    listen 80;
    server_name yourdomain.com;
    return 301 https://$host$request_uri;
}
```

### Caddy Example

```
yourdomain.com {
    reverse_proxy localhost:3000
}
```

## Environment Variables

### Required for Production

| Variable | How to Generate |
|---|---|
| `JWT_SECRET` | `openssl rand -base64 48` |
| `DB_ROOT_PASSWORD` | Strong random password |
| `DB_PASSWORD` | Strong random password |
| `CORS_ALLOWED_ORIGINS` | `https://yourdomain.com` |

### Optional

| Variable | Description |
|---|---|
| `AI_API_KEY` | OpenAI API key for AI features |
| `AI_MODEL` | Default: `gpt-4o-mini` |

## Database

- **Auto-migration**: Flyway runs on startup, applying V1–V19
- **No manual steps needed**: Schema updates automatically
- **Data persistence**: MySQL data stored in Docker volume `mysql_data`
- **Backups**: `docker compose exec mysql mysqldump -u root -p job_platform > backup.sql`

## Health Checks

```bash
# Backend health
curl http://localhost:8080/api/health

# Docker status
docker compose ps

# Logs
docker compose logs -f backend
docker compose logs -f frontend
```

## Updating

```bash
# Pull latest code
git pull

# Rebuild and restart
docker compose --env-file .env.production up -d --build

# Verify
docker compose ps
curl http://localhost:8080/api/health
```

## Troubleshooting

### Backend won't start
- Check database connectivity: `docker compose logs mysql`
- Verify environment variables in `.env.production`
- Ensure `JWT_SECRET` is at least 32 characters

### Frontend can't reach API
- Verify `CORS_ALLOWED_ORIGINS` includes your domain
- Check Nginx proxy configuration
- Ensure backend container is healthy

### Database connection refused
- MySQL may still be starting (healthcheck waits 30s)
- Check MySQL logs: `docker compose logs mysql`
- Verify DB credentials match between backend and MySQL

### AI features not working
- AI is optional — fallbacks are used without `AI_API_KEY`
- Set `AI_API_KEY` in `.env.production` to enable AI features
- Check backend logs for AI-related errors
