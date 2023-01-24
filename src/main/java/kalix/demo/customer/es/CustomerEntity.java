package kalix.demo.customer.es;

public class CustomerEntity  {

  record Customer(String id, String name, String email) {}

  sealed interface Command {}
  record CreateCustomer(String name, String email) implements Command {}
  record ChangeName(String name) implements Command{}


}
