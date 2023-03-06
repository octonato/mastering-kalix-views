package kalix.demo.customer.views;

import kalix.demo.customer.CustomerEntity;
import kalix.javasdk.view.View;
import kalix.javasdk.annotations.Query;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.annotations.Table;
import kalix.javasdk.annotations.ViewId;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;


@Table("customer_view")
@ViewId("customer-view-by-city")
public class CustomerByCityTransformed extends View<CustomerByCityTransformed.CustomerView> {

  public record CustomerView(String name, String city) {}

  @GetMapping("/customer-view/by-city/{city}")
  @Query("SELECT * FROM customer_view WHERE city = :city")
  public Flux<CustomerView> byCity(String city) { return null; }

  @Subscribe.ValueEntity(CustomerEntity.class)
  public UpdateEffect<CustomerView> onChange(CustomerEntity.Customer customer) {
    return effects().updateState(new CustomerView(customer.name(), customer.city()));
  }
}
