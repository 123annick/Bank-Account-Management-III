package models;

public abstract class Customer {
  private final String customerId;
  private String name;
  private int age;
  private String email;
  private String phone;
  private String address;

  protected Customer(String customerId, CustomerDetails details) {
    this.customerId = customerId;
    this.name = details.name();
    this.age = details.age();
    this.email = details.email();
    this.phone = details.phone();
    this.address = details.address();
  }

  /**
   * Returns the unique customer identifier, for example CUS001.
   *
   * @return the customer id
   */
  public String getCustomerId() {
    return customerId;
  }

  /**
   * Returns the customer's full name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Changes the customer's full name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the customer's age.
   *
   * @return the age in years
   */
  public int getAge() {
    return age;
  }

  /**
   * Changes the customer's age.
   *
   * @param age the new age in years
   */
  public void setAge(int age) {
    this.age = age;
  }

  /**
   * Returns the customer's email address.
   *
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * Changes the customer's email address.
   *
   * @param email the new email
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Returns the customer's phone number.
   *
   * @return the phone number
   */
  public String getPhone() {
    return phone;
  }

  /**
   * Changes the customer's phone number.
   *
   * @param phone the new phone number
   */
  public void setPhone(String phone) {
    this.phone = phone;
  }

  /**
   * Returns the customer's address.
   *
   * @return the address
   */
  public String getAddress() {
    return address;
  }

  /**
   * Changes the customer's address.
   *
   * @param address the new address
   */
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * Returns the personal details of this customer as one record.
   *
   * @return the customer details
   */
  public CustomerDetails getDetails() {
    return new CustomerDetails(name, age, email, phone, address);
  }

  /**
   * Returns the kind of customer.
   *
   * @return the customer type
   */
  public abstract CustomerType getCustomerType();

  /**
   * Tells whether monthly account fees are waived for this customer.
   *
   * @return true when fees are waived
   */
  public abstract boolean hasWaivedFees();
}
