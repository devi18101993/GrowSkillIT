package com.testproject.Qkart;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Class4_LoginTest extends BaseTest
{
	@Test
	public void verifyLoginAndLogOut() throws Exception
	{
		WebElement loginButton = driver.findElement(By.xpath("//button[text()='Login']"));
		loginButton.click();
		WebElement userName = driver.findElement(By.id("username"));
		WebElement passWord = driver.findElement(By.id("password"));
		userName.sendKeys("admin123");
		Thread.sleep(1500);
		passWord.sendKeys("admin123");
		Thread.sleep(1500);
		WebElement loginToQart = driver.findElement(By.xpath("//button[contains(text(),'Login to QKart')]"));
		loginToQart.click();
		Thread.sleep(1500);
		WebElement logout = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(),'Logout')]")));
		
		Assert.assertTrue(logout.isDisplayed(), "Logout button is not displayed");
	}
}
