# ⚡ Google Cloud Run - Quick Start Guide

## 🎯 What You Need

1. **Google Cloud Account** - Sign up at https://cloud.google.com
2. **GitHub Student Pack** - Get $300 free credits at https://education.github.com/pack
3. **gcloud CLI** - Download from https://cloud.google.com/sdk/docs/install

---

## 🚀 Deploy in 3 Steps

### Step 1: Install gcloud CLI

**Windows:**
Download and run the installer from https://cloud.google.com/sdk/docs/install

**Mac/Linux:**
```bash
curl https://sdk.cloud.google.com | bash
exec -l $SHELL
```

### Step 2: Login and Setup

```bash
# Login to Google Cloud
gcloud auth login

# Login to GitHub Student Pack and activate $300 credit
# Go to: https://console.cloud.google.com/billing
```

### Step 3: Run Deployment Script

**Windows (PowerShell):**
```powershell
.\deploy-to-cloudrun.ps1
```

**Mac/Linux:**
```bash
chmod +x deploy-to-cloudrun.sh
./deploy-to-cloudrun.sh
```

The script will:
- ✅ Create Google Cloud project
- ✅ Enable required APIs
- ✅ Setup Cloud SQL PostgreSQL
- ✅ Build and deploy your Docker image
- ✅ Configure environment variables
- ✅ Give you the live URL

**Time needed: ~15-20 minutes** (most time is waiting for Cloud SQL)

---

## 💰 Cost Breakdown

| Service | Tier | Monthly Cost | Free Tier |
|---------|------|--------------|-----------|
| **Cloud Run** | Auto-scaling | $0 | ✅ 2M requests/month |
| **Cloud SQL** | db-f1-micro | $7-10 | ❌ |
| **Container Registry** | Storage | $0.26 | ✅ First 0.5GB free |
| **Total** | | **~$7-10/month** | |

**With $300 Student Credit:**
- Covers: **30+ months** (2.5+ years!)
- Effective cost: **$0** for over 2 years! ✅

---

## 🎉 After Deployment

You'll get a URL like: `https://maville-backend-xxxxx-uc.a.run.app`

### Test Your Backend

```bash
# Health check
curl https://your-url.run.app/api/health

# View Swagger docs (in browser)
https://your-url.run.app/swagger-ui.html
```

### Update Your Frontend (Vercel)

1. Go to Vercel Dashboard → Your Project → Settings → Environment Variables
2. Update `NEXT_PUBLIC_API_URL` to your Cloud Run URL
3. Redeploy frontend

---

## 📊 Monitor Your App

### View Logs
```bash
gcloud run services logs tail maville-backend --region us-central1
```

### View Metrics
Go to: https://console.cloud.google.com/run

### Check Billing
Go to: https://console.cloud.google.com/billing

---

## 🔧 Common Commands

```bash
# Redeploy after code changes
docker build -t gcr.io/maville-prod/maville-backend:latest .
docker push gcr.io/maville-prod/maville-backend:latest
gcloud run deploy maville-backend --image gcr.io/maville-prod/maville-backend:latest --region us-central1

# Update environment variables
gcloud run services update maville-backend \
  --set-env-vars "CORS_ORIGINS=https://new-url.vercel.app" \
  --region us-central1

# Scale to zero (save costs during development)
gcloud run services update maville-backend \
  --min-instances 0 \
  --region us-central1

# Keep always warm (costs ~$5/month but no cold starts)
gcloud run services update maville-backend \
  --min-instances 1 \
  --region us-central1
```

---

## ❓ Troubleshooting

### "Docker daemon not running"
- Start Docker Desktop on Windows/Mac
- Or deploy directly from Cloud Shell (no local Docker needed!)

### "Permission denied"
```bash
gcloud auth login
gcloud auth configure-docker
```

### "Database connection failed"
- Check Cloud SQL instance is running
- Verify connection string in environment variables
- Check firewall rules allow Cloud Run → Cloud SQL

### "Cold start is slow"
- First request takes 2-3 seconds (normal)
- Set `--min-instances 1` to keep warm (costs ~$5/month)

---

## 🎓 What This Shows Recruiters

✅ **Docker** - Multi-stage builds, optimization  
✅ **Cloud Native** - Serverless architecture  
✅ **DevOps** - CI/CD, automated deployments  
✅ **Cost Optimization** - Pay-per-use, auto-scaling  
✅ **Production Ready** - Health checks, logging, monitoring  
✅ **Security** - Non-root containers, environment variables  

---

## 📚 Full Documentation

For detailed step-by-step instructions, see: **[DEPLOYMENT.md](./DEPLOYMENT.md)**

---

## 🆘 Need Help?

1. Check logs: `gcloud run services logs tail maville-backend --region us-central1`
2. View full guide: [DEPLOYMENT.md](./DEPLOYMENT.md)
3. Google Cloud docs: https://cloud.google.com/run/docs

---

**Ready to deploy? Run the script and you'll be live in 20 minutes! 🚀**

