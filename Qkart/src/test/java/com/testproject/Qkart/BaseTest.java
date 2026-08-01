package com.testproject.Qkart;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseTest 
{
	
	protected WebDriver driver;
	protected WebDriverWait wait;
	
	protected static final String EDGE_DRIVER_PATH =
			"C:\\Users\\user\\Downloads\\Softwares\\edgedriver_win64\\msedgedriver.exe";
	
	protected static final String BASE_URL =
			"https://crio-qkart-frontend-qa.vercel.app/";
	
	
	@BeforeMethod		
	public void setup()
	{
		System.setProperty("webdriver.edge.driver", EDGE_DRIVER_PATH);
		
		EdgeOptions options = new EdgeOptions();
		options.addArguments("--headless=new");
		options.addArguments("--window-size=1920,1080");
		options.addArguments("--disable-gpu");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		driver = new EdgeDriver(options);
		
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		driver.get(BASE_URL);
	}
	
	@AfterMethod
	public void teardown()
	{
		if (driver != null)
		{
			driver.quit();
		}
	}
	
	

}
