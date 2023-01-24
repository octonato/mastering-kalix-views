package kalix.demo.customer.views;

import kalix.demo.customer.CustomerEntity;
import kalix.javasdk.view.View;
import kalix.springsdk.annotations.Query;
import kalix.springsdk.annotations.Subscribe;
import kalix.springsdk.annotations.Table;
import kalix.springsdk.annotations.ViewId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Subscribe.ValueEntity(CustomerEntity.class)
@Table("customer_paging")
@ViewId("customer-paging-by-city")
public class CustomerByCityWithPaging extends View<CustomerEntity.Customer> {

  public record CustomerList(List<CustomerEntity.Customer> customers, String nextPageToken) {}

  @GetMapping("/customer-paging/by-city/{city}")
  @Query("""
    SELECT * as customers, next_page_token() AS nextPageToken
    FROM customer_paging
    WHERE city = :city
    OFFSET page_token_offset(:pageToken)
    LIMIT 10
    """)
  public CustomerList byCity(String city, @RequestParam(required=false) String pageToken) { return null; }
}
