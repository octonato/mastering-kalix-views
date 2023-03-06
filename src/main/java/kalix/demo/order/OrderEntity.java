package kalix.demo.order;

import kalix.demo.Done;
import kalix.javasdk.StatusCode;
import kalix.javasdk.annotations.EntityKey;
import kalix.javasdk.annotations.EntityType;
import kalix.javasdk.annotations.EventHandler;
import kalix.javasdk.annotations.TypeName;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@EntityKey("orderId")
@EntityType("order")
@RequestMapping("/order/{orderId}")
public class OrderEntity extends EventSourcedEntity<OrderEntity.Order, OrderEntity.Event> {


  @GetMapping
  public Effect<Order> get() {
    if (currentState() == null)
      return effects().error("Order not found", StatusCode.ErrorCode.NOT_FOUND);
    else
      return effects().reply(currentState());
  }

  @PostMapping("/create/{customerId}")
  public Effect<Done> create(@PathVariable String customerId) {

    var entityId = commandContext().entityId();

    if (currentState() != null)
      return effects().error("Order " + entityId + " already created");

    return effects()
      .emitEvent(new Event.OrderCreated(entityId, customerId))
      .thenReply(__ -> Done.instance);
  }

  @PostMapping("/items/add")
  public Effect<Done> addItem(@RequestBody Item item) {

    if (currentState() == null)
      return effects().error("Order not found", StatusCode.ErrorCode.NOT_FOUND);

    if (currentState().checkedOut)
      return effects().error("Order is already checked out");

    if (item.quantity <= 0)
      return effects().error("Quantity for item " + item.itemId + " must be greater than zero");

    return effects()
      .emitEvent(new Event.ItemAdded(item))
      .thenReply(__ -> Done.instance);

  }


  @PostMapping("/items/{itemId}/remove")
  public Effect<Done> removeItem(@PathVariable String itemId) {

    if (currentState() == null)
      return effects().error("Order not found", StatusCode.ErrorCode.NOT_FOUND);

    if (currentState().checkedOut)
      return effects().error("Order is already checked out");

    if (currentState().hasItem(itemId)) {
      return effects()
        .emitEvent(new Event.ItemRemoved(itemId))
        .thenReply(__ -> Done.instance);
    } else {
      return effects().reply(Done.instance);
    }
  }

  @PostMapping("/checkout")
  public Effect<Done> checkout() {
    if (currentState() == null)
      return effects().error("Order not found", StatusCode.ErrorCode.NOT_FOUND);

    if (currentState().checkedOut)
      return effects().reply(Done.instance);
    else
      return effects().emitEvent(new Event.CheckedOut()).thenReply(__ -> Done.instance);
  }

  @EventHandler
  public Order onEvent(Event.OrderCreated evt) {
    return new Order(evt.orderId, evt.customerId, Collections.emptyList(), false);
  }

  @EventHandler
  public Order onEvent(Event.ItemAdded evt) {
    return currentState().addItem(evt.item);
  }

  @EventHandler
  public Order onEvent(Event.ItemRemoved evt) {
    return currentState().removeItem(evt.itemId);
  }

  @EventHandler
  public Order onEvent(Event.CheckedOut evt) {
    return currentState().onCheckedOut();
  }

  // Domain
  public record Item(String itemId, String name, int quantity) {
    public Item increaseQuantity(int by) {
      return new Item(itemId, name, quantity + by);
    }
  }

  public record Order(String orderId, String customerId, List<Item> items, boolean checkedOut) {


    public boolean hasItem(String itemId) {
      return items.stream().anyMatch(it -> it.itemId.equals(itemId));
    }

    private Item updatedItem(Item item) {
      return
        items
          .stream()
          // select the item (if exists)
          .filter(it -> it.itemId.equals(item.itemId)).findFirst()
          // update its quantity
          .map(li -> li.increaseQuantity(item.quantity()))
          // or just insert a new one
          .orElse(item);
    }

    private List<Item> dropItem(String itemId) {
      return
        items.stream()
          .filter(lineItem -> !lineItem.itemId().equals(itemId))
          .collect(Collectors.toList());
    }

    public Order addItem(Item item) {
      var itemToAdd = updatedItem(item);
      var items = dropItem(item.itemId);
      items.add(itemToAdd);
      return new Order(orderId, customerId, items, checkedOut);
    }

    public Order removeItem(String itemId) {
      return new Order(orderId, customerId, dropItem(itemId), checkedOut);
    }

    public Order onCheckedOut() {
      return new Order(orderId, customerId, items, true);
    }

  }

  public sealed interface Event {

    @TypeName("order-created")
    record OrderCreated(String orderId, String customerId) implements Event {
    }

    @TypeName("item-added")
    record ItemAdded(Item item) implements Event {
    }

    @TypeName("item-removed")
    record ItemRemoved(String itemId) implements Event {
    }

    @TypeName("checked-out")
    record CheckedOut() implements Event {
    }
  }

}
