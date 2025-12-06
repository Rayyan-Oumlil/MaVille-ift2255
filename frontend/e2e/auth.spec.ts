import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {
  test('should redirect to login page when not authenticated', async ({ page }) => {
    await page.goto('/');
    
    // Should redirect to login
    await expect(page).toHaveURL(/.*login/);
  });

  test('should login successfully with valid credentials', async ({ page }) => {
    await page.goto('/login');
    
    // Fill login form
    await page.fill('input[type="email"], input[name="email"]', 'resident1@example.com');
    await page.fill('input[type="password"], input[name="password"]', 'password123');
    
    // Submit form
    await page.click('button[type="submit"]');
    
    // Should redirect to dashboard
    await expect(page).toHaveURL(/.*\/(residents|prestataires|stpm)/);
  });

  test('should show error with invalid credentials', async ({ page }) => {
    await page.goto('/login');
    
    // Fill login form with invalid credentials
    await page.fill('input[type="email"], input[name="email"]', 'invalid@example.com');
    await page.fill('input[type="password"], input[name="password"]', 'wrongpassword');
    
    // Submit form
    await page.click('button[type="submit"]');
    
    // Should show error message
    await expect(page.locator('text=/erreur|invalid|incorrect/i')).toBeVisible();
  });
});
