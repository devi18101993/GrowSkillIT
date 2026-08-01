package com.testproject.Qkart;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Class1_UIValidationTest extends BaseTest
{
	@Test
	public void verifyLoginButton()
	{
		WebElement loginButton = driver.findElement(By.xpath("//button[text()='Login']"));
		Assert.assertTrue(loginButton.isDisplayed(), "Login button is not displayed");
		Assert.assertTrue(loginButton.isEnabled(), "Login button is enabled and clickable");
		
		System.out.println("Login button validation test is completed");
		System.out.println("Maven Check");
	}
	
	@Test
	public void verifyRegisterButton()
	{
		WebElement registerButton = driver.findElement(By.xpath("//button[text()='Register']"));
		Assert.assertTrue(registerButton.isDisplayed(), "Register button is not displayed");
		Assert.assertTrue(registerButton.isEnabled(), "Register button is enabled and clickable");
		
		System.out.println("Register button validation test is completed");
		
	}
	
	@Test
	public void verifySearchTextBox()
	{
		WebElement searchButton = driver.findElement(By.xpath("//input[@placeholder='Search for items/categories']"));
		Assert.assertTrue(searchButton.isDisplayed(), "search button is not displayed");
		Assert.assertTrue(searchButton.isEnabled(), "search button is enabled and clickable");
		
		System.out.println("search placeholder validation test is completed");
	}
}
