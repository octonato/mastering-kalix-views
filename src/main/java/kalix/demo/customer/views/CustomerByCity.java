package kalix.demo.customer.views;

import kalix.demo.customer.CustomerEntity;
import kalix.javasdk.view.View;
import kalix.springsdk.annotations.Query;
import kalix.springsdk.annotations.Subscribe;
import kalix.springsdk.annotations.Table;
import kalix.springsdk.annotations.ViewId;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;

@Subscribe.ValueEntity(CustomerEntity.class)
@Table("customer")
@ViewId("customer-by-city")
public class CustomerByCity extends View<CustomerEntity.Customer> {

  @GetMapping("/customer/by-city/{city}")
  @Query("SELECT * FROM customer WHERE city = :city")
  public Flux<CustomerEntity.Customer> byCity(String city) { return null; }
}
