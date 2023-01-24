package kalix.demo.customer.views;

import kalix.demo.customer.CustomerEntity;
import kalix.javasdk.view.View;
import kalix.springsdk.annotations.Query;
import kalix.springsdk.annotations.Subscribe;
import kalix.springsdk.annotations.Table;
import kalix.springsdk.annotations.ViewId;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Subscribe.ValueEntity(CustomerEntity.class)
@Table("customer_projected_list")
@ViewId("customer-list-by-city")
public class CustomerByCityWithList extends View<CustomerEntity.Customer> {

  public record CustomerList(List<CustomerEntity.Customer> customers) {}

  @GetMapping("/customer-list/by-city/{city}")
  @Query("SELECT * as customers FROM customer_projected_list WHERE city = :city")
  public CustomerList byCity(String city) { return null; }
}
