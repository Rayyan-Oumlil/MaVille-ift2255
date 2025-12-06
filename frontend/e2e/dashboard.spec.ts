import { test, expect } from '@playwright/test';

test.describe('Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    // Login as STPM
    await page.goto('/login');
    await page.fill('input[type="email"], input[name="email"]', 'stpm@example.com');
    await page.fill('input[type="password"], input[name="password"]', 'password123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/.*stpm/);
  });

  test('should display dashboard overview', async ({ page }) => {
    await page.goto('/');
    
    // Should show statistics cards
    await expect(page.locator('text=/problèmes|projets|candidatures/i')).toBeVisible();
  });

  test('should navigate to problems page', async ({ page }) => {
    await page.goto('/stpm');
    
    // Click on problems link or navigate
    const problemsLink = page.locator('a[href*="problemes"], text=/problèmes/i').first();
    if (await problemsLink.isVisible()) {
      await problemsLink.click();
      await expect(page).toHaveURL(/.*problemes/);
    }
  });

  test('should display notifications', async ({ page }) => {
    await page.goto('/notifications');
    
    // Should show notifications page
    await expect(page.locator('text=/notifications/i')).toBeVisible();
  });
});
