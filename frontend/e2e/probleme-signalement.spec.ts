import { test, expect } from '@playwright/test';

test.describe('Signalement de problème', () => {
  test.beforeEach(async ({ page }) => {
    // Login as resident
    await page.goto('/login');
    await page.fill('input[type="email"], input[name="email"]', 'resident1@example.com');
    await page.fill('input[type="password"], input[name="password"]', 'password123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/.*residents/);
  });

  test('should navigate to signalement page', async ({ page }) => {
    await page.goto('/residents/signaler');
    
    // Should show the form
    await expect(page.locator('text=/signaler un problème/i')).toBeVisible();
    await expect(page.locator('input[placeholder*="lieu"], input[placeholder*="adresse"]')).toBeVisible();
  });

  test('should submit problem report successfully', async ({ page }) => {
    await page.goto('/residents/signaler');
    
    // Fill the form
    await page.fill('input[placeholder*="lieu"], input[placeholder*="adresse"]', '123 Rue Test, Montréal');
    await page.fill('textarea[placeholder*="description"]', 'Test de signalement de problème pour les tests E2E');
    
    // Submit
    await page.click('button[type="submit"]');
    
    // Should show success message or redirect
    await expect(
      page.locator('text=/succès|problème signalé|redirection/i')
    ).toBeVisible({ timeout: 10000 });
  });

  test('should validate required fields', async ({ page }) => {
    await page.goto('/residents/signaler');
    
    // Try to submit without filling fields
    await page.click('button[type="submit"]');
    
    // Should show validation errors
    await expect(page.locator('text=/requis|obligatoire/i')).toBeVisible();
  });
});
