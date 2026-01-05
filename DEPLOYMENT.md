# 🚀 Deployment Guide - Google Cloud Run

This guide walks you through deploying the MaVille backend to Google Cloud Run with Cloud SQL PostgreSQL.

## 📋 Prerequisites

1. **Google Cloud Account** with $300 free credits (GitHub Student Pack)
2. **Google Cloud CLI** installed: https://cloud.google.com/sdk/docs/install
3. **Docker** installed locally (for testing)
4. **Maven** installed (for building)

---

## 🛠️ Step 1: Setup Google Cloud Project

### 1.1 Create a New Project

```bash
# Login to Google Cloud
gcloud auth login

# Create a new project
gcloud projects create maville-prod --name="MaVille Production"

# Set as active project
gcloud config set project maville-prod

# Enable billing (required for Cloud Run)
# Go to: https://console.cloud.google.com/billing
```

### 1.2 Enable Required APIs

```bash
# Enable Cloud Run API
gcloud services enable run.googleapis.com

# Enable Cloud SQL Admin API
gcloud services enable sqladmin.googleapis.com

# Enable Container Registry API
gcloud services enable containerregistry.googleapis.com

# Enable Cloud Build API (optional, for CI/CD)
gcloud services enable cloudbuild.googleapis.com
```

---

## 🐘 Step 2: Setup Cloud SQL PostgreSQL

### 2.1 Create PostgreSQL Instance

```bash
# Create Cloud SQL instance (smallest tier for free credits)
gcloud sql instances create maville-db \
  --database-version=POSTGRES_15 \
  --tier=db-f1-micro \
  --region=us-central1 \
  --root-password=YOUR_ROOT_PASSWORD_HERE

# Create database
gcloud sql databases create maville --instance=maville-db

# Create user
gcloud sql users create maville_user \
  --instance=maville-db \
  --password=YOUR_USER_PASSWORD_HERE
```

### 2.2 Get Connection Details

```bash
# Get instance connection name
gcloud sql instances describe maville-db --format="value(connectionName)"
# Output: maville-prod:us-central1:maville-db
```

---

## 🐳 Step 3: Test Docker Build Locally

```bash
# Build the Docker image
docker build -t maville-backend .

# Test locally (optional)
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/maville \
  -e DATABASE_USER=maville_user \
  -e DATABASE_PASSWORD=maville_password \
  maville-backend

# Test health endpoint
curl http://localhost:8080/api/health
```

---

## ☁️ Step 4: Deploy to Cloud Run

### 4.1 Build and Push Docker Image

```bash
# Configure Docker to use gcloud as credential helper
gcloud auth configure-docker

# Build and tag image
docker build -t gcr.io/maville-prod/maville-backend:v1 .

# Push to Google Container Registry
docker push gcr.io/maville-prod/maville-backend:v1
```

### 4.2 Deploy to Cloud Run

```bash
# Deploy with Cloud SQL connection
gcloud run deploy maville-backend \
  --image gcr.io/maville-prod/maville-backend:v1 \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --add-cloudsql-instances maville-prod:us-central1:maville-db \
  --set-env-vars "DATABASE_URL=jdbc:postgresql:///maville?cloudSqlInstance=maville-prod:us-central1:maville-db&socketFactory=com.google.cloud.sql.postgres.SocketFactory" \
  --set-env-vars "DATABASE_USER=maville_user" \
  --set-env-vars "DATABASE_PASSWORD=YOUR_USER_PASSWORD_HERE" \
  --set-env-vars "CORS_ORIGINS=https://your-app.vercel.app,http://localhost:3000" \
  --memory 512Mi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 10 \
  --timeout 300
```

### 4.3 Get Your Service URL

```bash
# Get the deployed URL
gcloud run services describe maville-backend \
  --platform managed \
  --region us-central1 \
  --format "value(status.url)"

# Output: https://maville-backend-xxxxx-uc.a.run.app
```

---

## 🔧 Step 5: Update Frontend (Vercel)

Update your Next.js frontend environment variables in Vercel:

1. Go to Vercel Dashboard → Your Project → Settings → Environment Variables
2. Add/Update:
   - `NEXT_PUBLIC_API_URL` = `https://maville-backend-xxxxx-uc.a.run.app`
3. Redeploy frontend

---

## ✅ Step 6: Test Your Deployment

```bash
# Test health endpoint
curl https://maville-backend-xxxxx-uc.a.run.app/api/health

# Test Swagger docs
# Open in browser: https://maville-backend-xxxxx-uc.a.run.app/swagger-ui.html

# Test API endpoint
curl https://maville-backend-xxxxx-uc.a.run.app/api/residents/travaux
```

---

## 🔄 Continuous Deployment (Optional)

### Setup GitHub Actions for Auto-Deploy

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Cloud Run

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Cloud SDK
        uses: google-github-actions/setup-gcloud@v1
        with:
          service_account_key: ${{ secrets.GCP_SA_KEY }}
          project_id: maville-prod
      
      - name: Build and Deploy
        run: |
          gcloud builds submit --config cloudbuild.yaml
```

---

## 💰 Cost Monitoring

### Check Your Usage

```bash
# View Cloud Run metrics
gcloud run services describe maville-backend \
  --platform managed \
  --region us-central1 \
  --format="table(status.traffic)"

# View billing
# Go to: https://console.cloud.google.com/billing
```

### Expected Costs (with $300 credit)

- **Cloud Run**: $0/month (within free tier for low traffic)
- **Cloud SQL (db-f1-micro)**: ~$7-10/month
- **Total**: ~$100/year (covered by your $300 credit for 3 years!)

---

## 🐛 Troubleshooting

### View Logs

```bash
# Stream logs
gcloud run services logs tail maville-backend \
  --platform managed \
  --region us-central1

# View in Cloud Console
# https://console.cloud.google.com/run
```

### Common Issues

1. **Cold Start Delays**: First request may take 2-3 seconds
   - Solution: Set `--min-instances 1` (costs ~$5/month)

2. **Database Connection Timeout**:
   - Check Cloud SQL instance is running
   - Verify connection string format
   - Check firewall rules

3. **CORS Errors**:
   - Update `CORS_ORIGINS` environment variable
   - Redeploy service

---

## 🎉 Success!

Your MaVille backend is now live on Google Cloud Run! 🚀

**Architecture:**
- ✅ Backend: Google Cloud Run (auto-scaling, pay-per-use)
- ✅ Database: Cloud SQL PostgreSQL
- ✅ Frontend: Vercel (Next.js)
- ✅ Monitoring: Google Cloud Logging
- ✅ Cost: ~$0-10/month (covered by student credits)

**Next Steps:**
1. Add custom domain (optional)
2. Setup monitoring alerts
3. Configure CI/CD with GitHub Actions
4. Add Cloud CDN for better performance

