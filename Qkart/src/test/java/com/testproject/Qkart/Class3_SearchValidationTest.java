package com.testproject.Qkart;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Class3_SearchValidationTest extends BaseTest
{
	@Test
	public void verifySearchPlaceHolder()
	{
		WebElement searchBox = driver.findElement(By.xpath("//input[@name='search']"));
		
		Assert.assertEquals(searchBox.getAccessibleName(), "Search for items/categories");
		
		System.out.println("Search place holder = "+ searchBox.getAccessibleName());
		
	}
	
	@Test
	public void verifyPageTitle()
	{
		
		String actualTitle = driver.getTitle();
		
		Assert.assertEquals(actualTitle, "QKart", "Page Title is not QKart");
		System.out.println("Page Title = "+ actualTitle);
		
	}
	
	@Test
	public void verifyURL()
	{
		String actualURL = driver.getCurrentUrl();
		
		Assert.assertTrue(actualURL.contains("https"), "url contains not contains https");
	
		System.out.println("Page URL = "+ actualURL);
	}
	
	
}
