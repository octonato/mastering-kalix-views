package kalix.demo.customer;

import kalix.javasdk.valueentity.ValueEntity;
import kalix.springsdk.annotations.EntityKey;
import kalix.springsdk.annotations.EntityType;
import org.springframework.web.bind.annotation.*;

import static io.grpc.Status.Code.NOT_FOUND;

@EntityKey("id")
@EntityType("customer")
@RequestMapping("/customer/{id}")
public class CustomerEntity extends ValueEntity<CustomerEntity.Customer> {

  public record Customer(String id, String name, String email, String city) {
    public Customer withCity(String newCityName) {
      return new Customer(id, name, email, newCityName);
    }
  }

  record CreateCustomer(String name, String email, String city) {
  }

  @PostMapping
  public Effect<Done> create(@PathVariable String id, @RequestBody CreateCustomer cmd) {
    if (currentState() == null)
      return effects()
        .updateState(new Customer(id, cmd.name, cmd.email, cmd.city))
        .thenReply(Done.instance);
    else
      return effects().error("Customer '" + id + "' already created.");
  }


  record ChangeCity(String newCityName) {}

  @PatchMapping("/change/city")
  public Effect<Done> onCommand(@RequestBody ChangeCity cmd) {
    if (currentState() != null)
      return effects()
        .updateState(currentState().withCity(cmd.newCityName))
        .thenReply(Done.instance);
    else
      return effects().error("Customer '" + commandContext().entityId() + "' not created yet.", NOT_FOUND);
  }


  @GetMapping()
  public Effect<Customer> getState() {
    if (currentState() != null)
      return effects().reply(currentState());
    else
      return effects()
        .error("Customer '" + commandContext().entityId() + "' not found", NOT_FOUND);
  }
}
