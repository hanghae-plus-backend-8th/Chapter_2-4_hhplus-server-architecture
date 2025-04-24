package kr.hhplus.be.server.application.order;


import jakarta.transaction.Transactional;
import kr.hhplus.be.server.application.coupon.CouponService;
import kr.hhplus.be.server.application.order.OrderCommand.OrderCaptureCommand;
import kr.hhplus.be.server.application.order.OrderResult.OrderCaptureResult;
import kr.hhplus.be.server.application.point.PointService;
import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.MemberCouponRepository;
import kr.hhplus.be.server.domain.member.Member;
import kr.hhplus.be.server.domain.member.MemberRepository;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.point.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductRepository;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static kr.hhplus.be.server.domain.coupon.CouponType.FIXED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;

@SpringBootTest
class OrderServiceTest {

    @MockitoSpyBean
    private ProductRepository productRepository;

    @MockitoSpyBean
    private PointRepository pointRepository;

    @MockitoSpyBean
    private CouponRepository couponRepository;

    @MockitoSpyBean
    private MemberCouponRepository memberCouponRepository;

    @MockitoSpyBean
    private OrderRepository orderRepository;

    @MockitoSpyBean
    private PaymentRepository paymentRepository;

    @MockitoSpyBean
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PointService pointService;

    @Transactional
    @DisplayName("사용자는 상품을 주문하고 결제할 수 있다.")
    @Test
    void order() {
        // given
        LocalDateTime startDateTime = LocalDateTime.now().minusDays(5);
        LocalDateTime endDateTime = LocalDateTime.now().plusDays(5);
        Coupon coupon = Coupon.create("5000원 할인 쿠폰", FIXED, 5000, 50, startDateTime, endDateTime);
        Member member = Member.create("hanghae", "password1234", "김항해", "M", "01012341234");
        Product product = Product.create("상품명", 3000L, 50);
        member = memberRepository.save(member);
        product = productRepository.save(product);
        coupon = couponRepository.save(coupon);
        String couponNumber = couponService.download(coupon.getId(), member.getMemberId())
                .getCouponNumber();
        OrderCaptureCommand orderCaptureCommand = new OrderCaptureCommand(
                member.getMemberId(),
                couponNumber,
                List.of(product.getId()),
                List.of(1)
        );

        // when
        OrderCaptureResult orderCaptureResult = orderService.capture(orderCaptureCommand);

        // then
        InOrder inOrder = BDDMockito.inOrder(
                memberCouponRepository, productRepository, pointRepository,
                couponRepository, orderRepository, paymentRepository,
                pointHistoryRepository
        );

        assertThat(orderCaptureResult).extracting("totalPrice", "discountPrice", "payPrice")
                .containsExactlyInAnyOrder(3000L, 3000L, 0L);
        inOrder.verify(memberCouponRepository, times(1)).getByCouponNumberLocking(couponNumber);
        inOrder.verify(pointRepository, times(1)).getByIdLocking(member.getMemberId());
        inOrder.verify(productRepository, times(1)).findAllByIdsLocking(List.of(product.getId()));
        inOrder.verify(orderRepository, times(1)).save(any());
        inOrder.verify(paymentRepository, times(1)).save(any());
        inOrder.verify(memberCouponRepository, times(1)).updateStatus(anyLong(), any());
    }

    @DisplayName("동시에 여러 명의 사용자가 상품을 주문하고 결제할 수 있다.")
    @Test
    void orderV2() throws InterruptedException {
        // given
        int repeatCount = 15;
        CountDownLatch countDownLatch = new CountDownLatch(repeatCount);

        Product product = Product.create("상품명", 1000L, 50);
        product = productRepository.save(product);

        List<Member> memberList = new ArrayList<>();
        for (int i = 0; i < repeatCount; i++) {
            Member member = Member.create("hanghaePlus" + i, "password1234", "김항해", "M", "010123255" + i);
            member = memberRepository.save(member);
            memberList.add(member);
            pointService.charge(member.getMemberId(), 10000L);
        }

        // when
        for (int i = 0; i < repeatCount; i++) {
            Long memberId = memberList.get(i).getMemberId();

            OrderCaptureCommand orderCaptureCommand = new OrderCaptureCommand(
                    memberId,
                    null,
                    List.of(product.getId()),
                    List.of(1)
            );

            new Thread(() -> {
                orderService.capture(orderCaptureCommand);
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();

        // then
        int remainingQuantity = productRepository.getById(product.getId())
                .getQuantity();

        assertThat(remainingQuantity).isEqualTo(35);
    }
}