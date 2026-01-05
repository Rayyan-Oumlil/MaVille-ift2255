# Quick deployment script for Google Cloud Run (PowerShell version)
# Make sure you have gcloud CLI installed and authenticated

$ErrorActionPreference = "Stop"

Write-Host "`n🚀 MaVille - Google Cloud Run Deployment Script`n" -ForegroundColor Blue

# Check if gcloud is installed
if (!(Get-Command gcloud -ErrorAction SilentlyContinue)) {
    Write-Host "❌ gcloud CLI is not installed" -ForegroundColor Red
    Write-Host "Install from: https://cloud.google.com/sdk/docs/install"
    exit 1
}

# Variables (customize these)
$PROJECT_ID = "maville-prod"
$REGION = "us-central1"
$SERVICE_NAME = "maville-backend"
$DB_INSTANCE = "maville-db"
$DB_NAME = "maville"
$DB_USER = "maville_user"

Write-Host "📋 Configuration:" -ForegroundColor Green
Write-Host "  Project ID: $PROJECT_ID"
Write-Host "  Region: $REGION"
Write-Host "  Service: $SERVICE_NAME"
Write-Host "  Database: $DB_INSTANCE"
Write-Host ""

# Prompt for passwords
$DB_ROOT_PASSWORD = Read-Host "Enter PostgreSQL root password" -AsSecureString
$DB_ROOT_PASSWORD_TEXT = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($DB_ROOT_PASSWORD))

$DB_USER_PASSWORD = Read-Host "Enter PostgreSQL user password" -AsSecureString
$DB_USER_PASSWORD_TEXT = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($DB_USER_PASSWORD))

$FRONTEND_URL = Read-Host "Enter your Vercel frontend URL (e.g., https://maville.vercel.app)"

# Step 1: Set project
Write-Host "`nStep 1: Setting active project..." -ForegroundColor Blue
gcloud config set project $PROJECT_ID

# Step 2: Enable APIs
Write-Host "`nStep 2: Enabling required APIs..." -ForegroundColor Blue
gcloud services enable run.googleapis.com
gcloud services enable sqladmin.googleapis.com
gcloud services enable containerregistry.googleapis.com

# Step 3: Create Cloud SQL instance
Write-Host "`nStep 3: Creating Cloud SQL PostgreSQL instance..." -ForegroundColor Blue
Write-Host "⏳ This may take 5-10 minutes..."
try {
    gcloud sql instances create $DB_INSTANCE `
      --database-version=POSTGRES_15 `
      --tier=db-f1-micro `
      --region=$REGION `
      --root-password=$DB_ROOT_PASSWORD_TEXT
} catch {
    Write-Host "Instance may already exist" -ForegroundColor Yellow
}

# Step 4: Create database and user
Write-Host "`nStep 4: Creating database and user..." -ForegroundColor Blue
try {
    gcloud sql databases create $DB_NAME --instance=$DB_INSTANCE
} catch {
    Write-Host "Database may already exist" -ForegroundColor Yellow
}

try {
    gcloud sql users create $DB_USER `
      --instance=$DB_INSTANCE `
      --password=$DB_USER_PASSWORD_TEXT
} catch {
    Write-Host "User may already exist" -ForegroundColor Yellow
}

# Step 5: Get connection name
Write-Host "`nStep 5: Getting Cloud SQL connection name..." -ForegroundColor Blue
$CONNECTION_NAME = gcloud sql instances describe $DB_INSTANCE --format="value(connectionName)"
Write-Host "  Connection: $CONNECTION_NAME"

# Step 6: Build and push Docker image
Write-Host "`nStep 6: Building and pushing Docker image..." -ForegroundColor Blue
gcloud auth configure-docker
docker build -t gcr.io/$PROJECT_ID/$SERVICE_NAME`:latest .
docker push gcr.io/$PROJECT_ID/$SERVICE_NAME`:latest

# Step 7: Deploy to Cloud Run
Write-Host "`nStep 7: Deploying to Cloud Run..." -ForegroundColor Blue
gcloud run deploy $SERVICE_NAME `
  --image gcr.io/$PROJECT_ID/$SERVICE_NAME`:latest `
  --platform managed `
  --region $REGION `
  --allow-unauthenticated `
  --add-cloudsql-instances $CONNECTION_NAME `
  --set-env-vars "DATABASE_URL=jdbc:postgresql:///$DB_NAME?cloudSqlInstance=$CONNECTION_NAME&socketFactory=com.google.cloud.sql.postgres.SocketFactory" `
  --set-env-vars "DATABASE_USER=$DB_USER" `
  --set-env-vars "DATABASE_PASSWORD=$DB_USER_PASSWORD_TEXT" `
  --set-env-vars "CORS_ORIGINS=$FRONTEND_URL,http://localhost:3000" `
  --memory 512Mi `
  --cpu 1 `
  --min-instances 0 `
  --max-instances 10 `
  --timeout 300

# Step 8: Get service URL
Write-Host "`nStep 8: Getting service URL..." -ForegroundColor Blue
$SERVICE_URL = gcloud run services describe $SERVICE_NAME `
  --platform managed `
  --region $REGION `
  --format "value(status.url)"

Write-Host "`n✅ Deployment complete!`n" -ForegroundColor Green
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Host "🎉 Your backend is live at:" -ForegroundColor Green
Write-Host "$SERVICE_URL" -ForegroundColor Blue
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
Write-Host ""
Write-Host "📝 Next steps:"
Write-Host "  1. Test: curl $SERVICE_URL/api/health"
Write-Host "  2. View Swagger: $SERVICE_URL/swagger-ui.html"
Write-Host "  3. Update Vercel env: NEXT_PUBLIC_API_URL=$SERVICE_URL"
Write-Host "  4. View logs: gcloud run services logs tail $SERVICE_NAME --region $REGION"
Write-Host ""
Write-Host "💰 Cost estimate: ~`$7-10/month (covered by your `$300 student credit!)"
Write-Host ""

