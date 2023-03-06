package kalix.demo.order.views;

import kalix.demo.customer.CustomerEntity;
import kalix.demo.order.OrderEntity;
import kalix.javasdk.annotations.Query;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.annotations.Table;
import kalix.javasdk.annotations.ViewId;
import kalix.javasdk.view.View;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;

@ViewId("customers-orders")
public class CustomerOrderView  {

  public record CustomerOrder(
    String customerId,
    String name,
    String email,
    String city,
    String orderId,
    List<OrderEntity.Item> items,
    boolean checkedOut
  ) {}

  @GetMapping("/customer-orders/{customerId}")
  @Query(
    """
    SELECT *
    FROM customers
    JOIN orders ON customers.customerId = orders.customerId
    WHERE customers.customerId = :customerId and orders.checkedOut = true
    """)
  public Flux<CustomerOrder> get(String customerId) {
    return null;
  }


  @Table("customers")
  @Subscribe.ValueEntity(CustomerEntity.class)
  public static class Customers extends View<CustomerEntity.Customer> {}

  @Table("orders")
  @Subscribe.EventSourcedEntity(OrderEntity.class)
  public static class Orders extends View<OrderEntity.Order> {

    public UpdateEffect<OrderEntity.Order> onEvent(OrderEntity.Event.OrderCreated evt) {
      return effects()
        .updateState(new OrderEntity.Order(evt.orderId(), evt.customerId(), Collections.emptyList(), false));
    }

    public UpdateEffect<OrderEntity.Order> onEvent(OrderEntity.Event.ItemAdded evt) {
      return effects().updateState(viewState().addItem(evt.item()));
    }

    public UpdateEffect<OrderEntity.Order> onEvent(OrderEntity.Event.ItemRemoved evt) {
      return effects().updateState(viewState().removeItem(evt.itemId()));
    }

    public UpdateEffect<OrderEntity.Order> onEvent(OrderEntity.Event.CheckedOut evt) {
      return effects().updateState(viewState().onCheckedOut());
    }
  }
}
