package kr.hhplus.be.server.domain.order;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository {

    Order save(Order order);
    List<Order> findAllBy(LocalDateTime startDateTime, LocalDateTime endDateTime);
}
