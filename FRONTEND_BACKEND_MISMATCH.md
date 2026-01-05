# Frontend Operations Without Backend Support

## Summary
This document lists all frontend operations and their backend support status.

---

## ✅ All Operations Now Have Backend Support

All missing backend endpoints have been implemented.

---

## ✅ Notification Operations - Backend Endpoints Implemented

### Residents
- ✅ `PUT /api/residents/{email}/notifications/{id}/marquer-lu` - Mark single notification as read
- ✅ `DELETE /api/residents/{email}/notifications/{id}` - Delete single notification
- ✅ `DELETE /api/residents/{email}/notifications` - Clear all resident notifications
- ✅ `PUT /api/residents/{email}/notifications/marquer-lu` - Mark ALL notifications as read (existing)

### STPM
- ✅ `PUT /api/stpm/notifications/{id}/marquer-lu` - Mark single STPM notification as read
- ✅ `DELETE /api/stpm/notifications/{id}` - Delete single STPM notification
- ✅ `DELETE /api/stpm/notifications` - Clear all STPM notifications

### Providers
- ✅ `PUT /api/prestataires/{neq}/notifications/{id}/marquer-lu` - Mark single provider notification as read
- ✅ `DELETE /api/prestataires/{neq}/notifications/{id}` - Delete single provider notification
- ✅ `DELETE /api/prestataires/{neq}/notifications` - Clear all provider notifications

---

## ✅ Other Frontend Operations WITH Backend Support

### Authentication
- ✅ `POST /api/auth/login` - Login

### Health
- ✅ `GET /api/health` - Health check

### Residents
- ✅ `GET /api/residents/travaux` - Get public works
- ✅ `POST /api/residents/problemes` - Report problem
- ✅ `GET /api/residents/{email}/notifications` - Get notifications
- ✅ `GET /api/residents/{email}/preferences` - Get preferences
- ✅ `PUT /api/residents/{email}/preferences` - Update preferences

### Providers
- ✅ `GET /api/prestataires/problemes` - Get available problems
- ✅ `POST /api/prestataires/candidatures` - Submit application
- ✅ `GET /api/prestataires/{neq}/projets` - Get projects
- ✅ `PUT /api/prestataires/projets/{id}` - Update project
- ✅ `GET /api/prestataires/{neq}/notifications` - Get notifications

### STPM
- ✅ `GET /api/stpm/candidatures` - Get applications
- ✅ `PUT /api/stpm/candidatures/{id}/valider` - Validate application
- ✅ `GET /api/stpm/problemes` - Get problems
- ✅ `PUT /api/stpm/problemes/{id}/priorite` - Modify priority
- ✅ `GET /api/stpm/notifications` - Get notifications

### Montreal
- ✅ `GET /api/montreal/travaux` - Get Montreal public works

---

## Implementation Details

### Backend Methods Added (DatabaseStorageService.java)
- `markNotificationAsRead(Long notificationId)` - Mark single notification as read
- `deleteNotification(Long notificationId)` - Delete single notification
- `deleteAllResidentNotifications(String email)` - Clear all resident notifications
- `deleteAllStpmNotifications()` - Clear all STPM notifications
- `deleteAllPrestataireNotifications(String neq)` - Clear all provider notifications

### Security
All endpoints include proper validation:
- Check if notification exists (404 if not found)
- Check if notification belongs to the requesting user/role (403 if unauthorized)
- Proper error handling and logging

---

## Next Steps

The frontend can now be updated to call these new endpoints instead of only manipulating local state. The frontend notification components should be updated to:
1. Call the backend when marking individual notifications as read
2. Call the backend when deleting individual notifications
3. Call the backend when clearing all notifications
4. Refresh the notification list after these operations
