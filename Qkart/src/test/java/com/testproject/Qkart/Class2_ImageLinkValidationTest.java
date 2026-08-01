package com.testproject.Qkart;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Class2_ImageLinkValidationTest extends BaseTest
{
	@Test
	public void verifyTotalImages()
	{
		
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img.MuiCardMedia-img")));
		
		List<WebElement> page1Images = driver.findElements(By.tagName("img"));
		
		driver.findElement(By.xpath("//button[@aria-label='Go to page 2']")).click();
		
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img.MuiCardMedia-img")));
		
		List<WebElement> page2Images = driver.findElements(By.tagName("img"));
		
		int totalImages = page1Images.size() + page2Images.size();
		
		Assert.assertTrue(totalImages > 0, "No images found");
		Assert.assertEquals(totalImages, 25, "Total images are not equal to 25");
		
		System.out.println("Total Images  = "+ totalImages);
		
	}
	
	@Test
	public void verifyTotalLinks()
	{
		List<WebElement> links = driver.findElements(By.tagName("a"));
		
		Assert.assertEquals(links.size(), 4, "Total Links in Qkart page is not equal to 4");
		
		System.out.println("Total Link  = "+ links.size());
		
	}
}
