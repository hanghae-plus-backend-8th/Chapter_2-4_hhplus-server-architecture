package kr.hhplus.be.server.application.order;

import jakarta.persistence.EntityManagerFactory;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.order.OrderStatistics;
import kr.hhplus.be.server.domain.order.OrderStatisticsRepository;
import kr.hhplus.be.server.infrastructure.order.persistence.OrderStatisticsEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderBatchSchedular {

    private final OrderRepository orderRepository;
    private final EntityManagerFactory entityManagerFactory;

    // 매일 새벽 1시에 배치 실행!
    @Scheduled(cron = "0 0 1 * * *")
    public void orderStatistics() {
        LocalDate yesterday = LocalDate.now()
                .minusDays(1L);

        // 추후 페이징 처리 필요!
        List<Order> orderList = orderRepository.findAllBy(yesterday.atStartOfDay(), yesterday.atTime(23, 59, 59));

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        StatelessSession statelessSession = sessionFactory.openStatelessSession();
        Transaction tx = statelessSession.beginTransaction();

        try {
            for (Order order : orderList) {
                List<OrderStatistics> orderStatisticsList = OrderStatistics.create(order);

                for (OrderStatistics orderStatistics : orderStatisticsList) {
                    OrderStatisticsEntity orderStatisticsEntity = OrderStatisticsEntity.builder()
                            .orderId(orderStatistics.getOrderId())
                            .productId(orderStatistics.getProductId())
                            .quantity(orderStatistics.getQuantity())
                            .build();

                    statelessSession.insert(orderStatisticsEntity);
                }
            }
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            statelessSession.close();
        }
    }
}
