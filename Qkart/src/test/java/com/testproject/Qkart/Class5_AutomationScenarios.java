package com.testproject.Qkart;

import java.util.UUID;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Class5_AutomationScenarios extends BaseTest
{

	@Test
	public void verifyQkartAutomationScenarios() throws Exception
	{
		
		String username = "admin" + UUID.randomUUID().toString().substring(0, 5);
		String password = "temp123";
		
		WebElement registerButton = driver.findElement(By.xpath("//button[normalize-space()='Register']"));
		registerButton.click();
		
		// Step 1: Register with new username and password
//		WebElement userName = driver.findElement(By.id("username"));
//		WebElement passWord = driver.findElement(By.id("password"));
//		WebElement cfmPassWord = driver.findElement(By.id("confirmPassword"));
		WebElement registerNowButton = driver.findElement(By.xpath("//button[normalize-space()='Register Now']"));
		
		driver.findElement(By.id("username")).sendKeys(username);
		Thread.sleep(1500);
		driver.findElement(By.id("password")).sendKeys(password);
		Thread.sleep(1500);
		driver.findElement(By.id("confirmPassword")).sendKeys(password);
		Thread.sleep(1500);
		registerNowButton.click();
		WebElement registeredSuccess = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[text()='Registered Successfully']")));		
		Assert.assertTrue(registeredSuccess.isDisplayed(), "Registration is not Successful");
		
		// Step 2: Login using the username and password
		driver.findElement(By.id("username")).sendKeys(username);
		Thread.sleep(1500);
		driver.findElement(By.id("password")).sendKeys(password);
		Thread.sleep(1500);
		WebElement loginToQart = driver.findElement(By.xpath("//button[contains(text(),'Login to QKart')]"));
		loginToQart.click();
		Thread.sleep(1500);
		WebElement logout = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(),'Logout')]")));
		Assert.assertTrue(logout.isDisplayed(), "Logout button is not displayed");
		
		// Step 3: Add yonex  rackquet in cart by clicking on Add to cart button
		WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Search for items/categories']")));
		searchBox.clear();
		searchBox.sendKeys("YONEX Smash Badminton");
		searchBox.sendKeys(Keys.ENTER);
		Thread.sleep(1500);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(text(),'YONEX Smash Badminton Racquet')]")));
		
		WebElement yonexProduct = driver.findElement(By.xpath("//p[contains(text(),'YONEX Smash Badminton Racquet')]"));
		Assert.assertTrue(yonexProduct.isDisplayed(), "Yonex Smash badminton Racquet is not displayed");
		
		WebElement yonexAddToCart = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//p[contains(text(),'YONEX Smash Badminton Racquet')]/ancestor::div[contains(@class,'MuiCard-root')]//button[contains(.,'Add to cart')]")));
		
		yonexAddToCart.click();
		Thread.sleep(1500);
		
//		WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'Product has been successfully added to cart')]")));
//		
//		Assert.assertEquals(successMessage.getText().trim(), "Product has been successfully added to cart", 
//				"Success message is incorrect");
		
		yonexAddToCart.click();
		Thread.sleep(1500);
		
		WebElement duplicateMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'Item already in cart')]")));
		
		System.out.println(duplicateMessage.getText().trim());
		
		String expectedDuplicateMessage =
                "Item already in cart. Use the cart sidebar to update quantity or remove item.";
		
		Assert.assertEquals(duplicateMessage.getText().trim(), expectedDuplicateMessage, "Duplicate cart validation failed");
		Thread.sleep(1500);
		
		// Step 4: Add Roadster Mens Running Shoes and pick size from the dropdown menu
		//WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Search for items/categories']")));
		searchBox.clear();
		searchBox.sendKeys("Roadster Mens");
		searchBox.sendKeys(Keys.ENTER);
		Thread.sleep(1500);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(text(),'Roadster Mens Running Shoes')]")));
		
		WebElement roadsterProduct = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(text(),'Roadster Mens Running Shoes')]")));
		
		Assert.assertTrue(roadsterProduct.isDisplayed(), "Roadster product is not found");
		
		WebElement sizeDropDown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("uncontrolled-native")));
		
		Select select = new Select(sizeDropDown);
		select.selectByVisibleText("9");
		
		WebElement roadsterAddToCart = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//p[contains(text(),'Roadster Mens Running Shoes')]/ancestor::div[contains(@class,'MuiCard-root')]//button[contains(.,'Add to cart')]")));
		
		roadsterAddToCart.click();
		Thread.sleep(1500);
		
//        WebElement roadsterSuccess =
//                wait.until(ExpectedConditions.visibilityOfElementLocated(
//                        By.xpath("//*[contains(text(),'Product has been successfully added to cart')]")));
//        Assert.assertEquals(
//                roadsterSuccess.getText().trim(),
//                "Product has been successfully added to cart");
		
		// Step 5: Check the cart if Roadster and Yonex are present
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart")));
		
		WebElement yonexCartItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'YONEX Smash Badminton Racquet')]")));
		Assert.assertTrue(yonexCartItem.isDisplayed(), "yonex racquet is not available in the cart");
		
		WebElement roadsterCartItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'Roadster Mens Running Shoes')]")));
		Assert.assertTrue(roadsterCartItem.isDisplayed(), "Roadster Shoes are not available in the cart");
		
		// Step 6: Checkout the items
		WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class,'checkout-btn')]")));
		checkoutButton.click();
		
		wait.until(ExpectedConditions.urlContains("checkout"));
		
		Assert.assertTrue(driver.getCurrentUrl().contains("checkout"), "Checkout page is not opened");
				
		// Step 6: Add New Address

		By addNewAddressLocator =
		        By.xpath("//button[normalize-space()='Add new address']");

		By addressTextAreaLocator =
		        By.xpath("//textarea[@placeholder='Enter your complete address']");

		By addAddressButtonLocator =
		        By.xpath("//button[normalize-space()='Add']");

		String address1 =
		        "No, 41/170, West main road, Kerala, India, Earth, Milky way galaxy, near by andromeda galaxy";

		String address2 =
		        "No. 9/99, DM C/O TK, California, United States, near by beach";


		// ---------- Address 1: California ----------

		wait.until(ExpectedConditions.elementToBeClickable(addNewAddressLocator))
		        .click();

		WebElement addressTextArea = wait.until(
		        ExpectedConditions.visibilityOfElementLocated(addressTextAreaLocator)
		);

		addressTextArea.clear();
		addressTextArea.sendKeys(address2);

		wait.until(ExpectedConditions.elementToBeClickable(addAddressButtonLocator))
		        .click();


		// Wait until the address form disappears
		wait.until(ExpectedConditions.invisibilityOfElementLocated(addressTextAreaLocator));


		// ---------- Address 2: Kerala ----------

		wait.until(ExpectedConditions.elementToBeClickable(addNewAddressLocator))
		        .click();

		addressTextArea = wait.until(
		        ExpectedConditions.visibilityOfElementLocated(addressTextAreaLocator)
		);

		addressTextArea.clear();
		addressTextArea.sendKeys(address1);

		wait.until(ExpectedConditions.elementToBeClickable(addAddressButtonLocator))
		        .click();


		// Wait until the address form disappears again
		wait.until(ExpectedConditions.invisibilityOfElementLocated(addressTextAreaLocator));


		// Step 7: Verify addresses

		WebElement addedAddress1 = wait.until(
		        ExpectedConditions.visibilityOfElementLocated(
		                By.xpath("//*[contains(normalize-space(.),'Kerala')]")
		        )
		);

		WebElement addedAddress2 = wait.until(
		        ExpectedConditions.visibilityOfElementLocated(
		                By.xpath("//*[contains(normalize-space(.),'California')]")
		        )
		);

		Assert.assertTrue(
		        addedAddress1.isDisplayed(),
		        "Kerala address is not displayed"
		);

		Assert.assertTrue(
		        addedAddress2.isDisplayed(),
		        "California address is not displayed"
		);

		
		//Step 8: delete kerala address
		By keralaDeleteLocator = By.xpath("(//*[contains(., 'West main road, Kerala, India')]//descendant::*[normalize-space()='Delete'])[last()]");
		
		WebElement keralaAddressCard = wait.until(
		        ExpectedConditions.visibilityOfElementLocated(keralaDeleteLocator)
		);

		wait.until(ExpectedConditions.elementToBeClickable(keralaAddressCard)).click();
		
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//*[contains(.,'West main road, Kerala, India')]")));

        Assert.assertTrue(
                driver.findElements(
                        By.xpath("//*[contains(.,'West main road, Kerala, India')]"))
                        .size() == 0,
                "Address was not deleted.");

		System.out.println("Kerala address deleted successfully");

		
	}
}
