package kr.hhplus.be.server.infrastructure.order.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    @Query("""
        SELECT
            O
        FROM
            OrderEntity O
        WHERE
            O.createdAt >= :startDateTime
            AND O.createdAt <= :endDateTime
    """)
    List<OrderEntity> findAllBy(LocalDateTime startDateTime, LocalDateTime endDateTime);
}
