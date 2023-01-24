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
@Table("customer_projected")
@ViewId("customer-projected-by-city")
public class CustomerByCityRelationalProjection extends View<CustomerEntity.Customer> {

  public record CustomerView(String name, String city) {}

  @GetMapping("/customer-projection/by-city/{city}")
  @Query("SELECT name, city FROM customer_projected WHERE city = :city")
  public Flux<CustomerView> byCity(String city) { return null; }
}
