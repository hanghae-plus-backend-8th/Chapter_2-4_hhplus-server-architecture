package kr.hhplus.be.server.infrastructure.order.persistence;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderProduct;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.infrastructure.member.persistence.MemberEntity;
import kr.hhplus.be.server.infrastructure.member.persistence.MemberJpaRepository;
import kr.hhplus.be.server.shared.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final MemberJpaRepository memberJpaRepository;
    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        MemberEntity memberEntity = memberJpaRepository.findById(order.getMemberId())
                .orElseThrow(() -> new NotFoundResourceException("유효하지 않은 사용자 식별자입니다."));
        OrderEntity orderEntity = OrderEntity.builder()
                .member(memberEntity)
                .orderProducts(new ArrayList<>())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .build();

        for (OrderProduct orderProduct : order.getOrderProducts()) {
            OrderProductEntity orderProductEntity = OrderProductEntity.builder()
                    .order(orderEntity)
                    .productId(orderProduct.getProductId())
                    .quantity(orderProduct.getQuantity())
                    .price(orderProduct.getPrice())
                    .build();

            orderEntity.addOrderProduct(orderProductEntity);
        }
        return orderJpaRepository.save(orderEntity)
                .toDomain();
    }

    @Override
    public List<Order> findAllBy(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return orderJpaRepository.findAllBy(startDateTime, endDateTime)
                .stream()
                .map(OrderEntity::toDomain)
                .toList();
    }
}
