Feature: Purchase flow on SauceDemo
  As a SauceDemo customer
  I want to log in, select products and complete checkout
  So that I can buy products online

  Background:
    Given the SauceDemo login page is open

  @positive @smoke
  Scenario: Standard user completes a purchase and logs out
    Given the user is logged in as "standard_user"
    When the user adds "Sauce Labs Backpack" and "Sauce Labs Bike Light" to the cart
    Then the cart badge shows "2"
    When the user opens the cart
    Then the cart contains "Sauce Labs Backpack" and "Sauce Labs Bike Light" with their prices
    When the user proceeds to checkout
    And the user enters first name "Daniel", last name "Georgiev" and postal code "1000"
    And the user continues to the overview
    Then the overview lists both products
    And the item total equals the sum of the product prices
    And the total equals the item total plus tax
    When the user finishes the order
    Then the "Thank you for your order!" confirmation is displayed
    And the cart badge is not displayed
    When the user logs out
    Then the login page is displayed

  # <published password> is the password shown on the login page under "Password for all users".
  @negative
  Scenario Outline: Login is rejected for invalid or blocked credentials
    When the user logs in with username "<username>" and password "<password>"
    Then the error message "<error>" is displayed
    And the user stays on the login page

    Examples:
      | username        | password             | error                                                                     |
      | locked_out_user | <published password> | Epic sadface: Sorry, this user has been locked out.                       |
      | standard_user   | wrong_pass           | Epic sadface: Username and password do not match any user in this service |
      |                 | <published password> | Epic sadface: Username is required                                        |
      | standard_user   |                      | Epic sadface: Password is required                                        |

  @negative
  Scenario Outline: Checkout cannot continue when required customer information is missing
    Given the user is logged in as "standard_user"
    And the cart contains "Sauce Labs Backpack"
    And the user is on the checkout information step
    When the user enters first name "<first name>", last name "<last name>" and postal code "<postal code>"
    And the user tries to continue
    Then the error message "<error>" is displayed
    And the user stays on the checkout information step

    Examples:
      | first name | last name | postal code | error                          |
      |            | Georgiev  | 1000        | Error: First Name is required  |
      | Daniel     |           | 1000        | Error: Last Name is required   |
      | Daniel     | Georgiev  |             | Error: Postal Code is required |

  @edge @known-defect @BUG-1
  Scenario: Checkout is not allowed with an empty cart
    Given the user is logged in as "standard_user"
    And the cart is empty
    When the user opens the cart
    Then checkout is not possible: the "Checkout" button is disabled or an "empty cart" message is shown
    And no order confirmation can be reached
