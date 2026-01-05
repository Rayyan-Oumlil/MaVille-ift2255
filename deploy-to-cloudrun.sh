#!/bin/bash
# Quick deployment script for Google Cloud Run
# Make sure you have gcloud CLI installed and authenticated

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 MaVille - Google Cloud Run Deployment Script${NC}\n"

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo -e "${RED}❌ gcloud CLI is not installed${NC}"
    echo "Install from: https://cloud.google.com/sdk/docs/install"
    exit 1
fi

# Variables (customize these)
PROJECT_ID="maville-prod"
REGION="us-central1"
SERVICE_NAME="maville-backend"
DB_INSTANCE="maville-db"
DB_NAME="maville"
DB_USER="maville_user"

echo -e "${GREEN}📋 Configuration:${NC}"
echo "  Project ID: $PROJECT_ID"
echo "  Region: $REGION"
echo "  Service: $SERVICE_NAME"
echo "  Database: $DB_INSTANCE"
echo ""

# Prompt for passwords
read -sp "Enter PostgreSQL root password: " DB_ROOT_PASSWORD
echo ""
read -sp "Enter PostgreSQL user password: " DB_USER_PASSWORD
echo ""
read -p "Enter your Vercel frontend URL (e.g., https://maville.vercel.app): " FRONTEND_URL
echo ""

# Step 1: Set project
echo -e "\n${BLUE}Step 1: Setting active project...${NC}"
gcloud config set project $PROJECT_ID

# Step 2: Enable APIs
echo -e "\n${BLUE}Step 2: Enabling required APIs...${NC}"
gcloud services enable run.googleapis.com
gcloud services enable sqladmin.googleapis.com
gcloud services enable containerregistry.googleapis.com

# Step 3: Create Cloud SQL instance
echo -e "\n${BLUE}Step 3: Creating Cloud SQL PostgreSQL instance...${NC}"
echo "⏳ This may take 5-10 minutes..."
gcloud sql instances create $DB_INSTANCE \
  --database-version=POSTGRES_15 \
  --tier=db-f1-micro \
  --region=$REGION \
  --root-password=$DB_ROOT_PASSWORD || echo "Instance may already exist"

# Step 4: Create database and user
echo -e "\n${BLUE}Step 4: Creating database and user...${NC}"
gcloud sql databases create $DB_NAME --instance=$DB_INSTANCE || echo "Database may already exist"
gcloud sql users create $DB_USER \
  --instance=$DB_INSTANCE \
  --password=$DB_USER_PASSWORD || echo "User may already exist"

# Step 5: Get connection name
echo -e "\n${BLUE}Step 5: Getting Cloud SQL connection name...${NC}"
CONNECTION_NAME=$(gcloud sql instances describe $DB_INSTANCE --format="value(connectionName)")
echo "  Connection: $CONNECTION_NAME"

# Step 6: Build and push Docker image
echo -e "\n${BLUE}Step 6: Building and pushing Docker image...${NC}"
gcloud auth configure-docker
docker build -t gcr.io/$PROJECT_ID/$SERVICE_NAME:latest .
docker push gcr.io/$PROJECT_ID/$SERVICE_NAME:latest

# Step 7: Deploy to Cloud Run
echo -e "\n${BLUE}Step 7: Deploying to Cloud Run...${NC}"
gcloud run deploy $SERVICE_NAME \
  --image gcr.io/$PROJECT_ID/$SERVICE_NAME:latest \
  --platform managed \
  --region $REGION \
  --allow-unauthenticated \
  --add-cloudsql-instances $CONNECTION_NAME \
  --set-env-vars "DATABASE_URL=jdbc:postgresql:///$DB_NAME?cloudSqlInstance=$CONNECTION_NAME&socketFactory=com.google.cloud.sql.postgres.SocketFactory" \
  --set-env-vars "DATABASE_USER=$DB_USER" \
  --set-env-vars "DATABASE_PASSWORD=$DB_USER_PASSWORD" \
  --set-env-vars "CORS_ORIGINS=$FRONTEND_URL,http://localhost:3000" \
  --memory 512Mi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 10 \
  --timeout 300

# Step 8: Get service URL
echo -e "\n${BLUE}Step 8: Getting service URL...${NC}"
SERVICE_URL=$(gcloud run services describe $SERVICE_NAME \
  --platform managed \
  --region $REGION \
  --format "value(status.url)")

echo -e "\n${GREEN}✅ Deployment complete!${NC}\n"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${GREEN}🎉 Your backend is live at:${NC}"
echo -e "${BLUE}$SERVICE_URL${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📝 Next steps:"
echo "  1. Test: curl $SERVICE_URL/api/health"
echo "  2. View Swagger: $SERVICE_URL/swagger-ui.html"
echo "  3. Update Vercel env: NEXT_PUBLIC_API_URL=$SERVICE_URL"
echo "  4. View logs: gcloud run services logs tail $SERVICE_NAME --region $REGION"
echo ""
echo "💰 Cost estimate: ~\$7-10/month (covered by your \$300 student credit!)"
echo ""

