package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.application.coupon.CouponCommand.CouponListCommand;
import kr.hhplus.be.server.application.coupon.CouponResult.CouponDownloadResult;
import kr.hhplus.be.server.application.coupon.CouponResult.CouponListResult;
import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.MemberCouponRepository;
import kr.hhplus.be.server.domain.member.Member;
import kr.hhplus.be.server.domain.member.MemberRepository;
import kr.hhplus.be.server.shared.dto.ListDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static kr.hhplus.be.server.domain.coupon.CouponType.PERCENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class CouponServiceTest {

    @MockitoSpyBean
    private CouponService couponService;

    @MockitoSpyBean
    private CouponRepository couponRepository;

    @MockitoSpyBean
    private MemberCouponRepository memberCouponRepository;

    @MockitoSpyBean
    private MemberRepository memberRepository;

    @Transactional
    @DisplayName("사용자는 쿠폰을 발급할 수 있다.")
    @Test
    void downloadCoupon() {
        // given
        LocalDateTime startDateTime = LocalDateTime.now().minusDays(5);
        LocalDateTime endDateTime = LocalDateTime.now().plusDays(5);
        Coupon coupon = Coupon.create("스타벅스 커피 30% 할인쿠폰", PERCENT, 30, 50, startDateTime, endDateTime);
        Member member = Member.create("hanghae", "password1234", "김항해", "M", "01012341234");
        member = memberRepository.save(member);
        coupon = couponRepository.save(coupon);

        // when
        CouponDownloadResult couponDownloadResult = couponService.download(coupon.getId(), member.getMemberId());

        // then
        InOrder inOrder = inOrder(memberRepository, couponRepository, memberCouponRepository);

        assertThat(couponDownloadResult.getName()).isEqualTo(coupon.getName());
        inOrder.verify(memberRepository, times(1)).getById(member.getMemberId());
        inOrder.verify(couponRepository, times(1)).getByIdLocking(coupon.getId());
        inOrder.verify(memberCouponRepository, times(1)).save(any());
        inOrder.verify(couponRepository, times(1)).updateQuantity(coupon.getId(), coupon.getRemainingQuantity() - 1);
    }

    @Transactional
    @DisplayName("사용자는 자신이 보유한 쿠폰 목록을 조회할 수 있다.")
    @Test
    void findMyCouponList() {
        // given
        LocalDateTime startDateTime = LocalDateTime.now().minusDays(5);
        LocalDateTime endDateTime = LocalDateTime.now().plusDays(5);
        Coupon coupon = Coupon.create("스타벅스 커피 30% 할인쿠폰", PERCENT, 30, 50, startDateTime, endDateTime);
        Member member = Member.create("hanghae", "password1234", "김항해", "M", "01012341234");
        member = memberRepository.save(member);
        coupon = couponRepository.save(coupon);
        for (int i=0; i<5; i++) {
            couponService.download(coupon.getId(), member.getMemberId());
        }
        CouponListCommand couponListCommand = new CouponListCommand(member.getMemberId(), 0, 5);

        // when
        ListDto<CouponListResult> myCouponList = couponService.list(couponListCommand);

        // then
        assertThat(myCouponList.getTotalCount()).isEqualTo(5);
        verify(couponService, times(1)).list(couponListCommand);
    }

    @DisplayName("동시에 여러 명의 사용자가 쿠폰을 발급할 수 있다.")
    @Test
    void downloadCouponV2() throws InterruptedException {
        // given
        int repeatCount = 20;
        CountDownLatch countDownLatch = new CountDownLatch(repeatCount);

        LocalDateTime startDateTime = LocalDateTime.now().minusDays(5);
        LocalDateTime endDateTime = LocalDateTime.now().plusDays(5);
        Coupon coupon = Coupon.create("스타벅스 커피 30% 할인쿠폰", PERCENT, 30, 50, startDateTime, endDateTime);
        coupon = couponRepository.save(coupon);

        List<Member> memberList = new ArrayList<>();
        for (int i = 0; i < repeatCount; i++) {
            Member member = Member.create("hanghae" + i, "password1234", "김항해", "M", "010123412" + i);
            memberList.add(memberRepository.save(member));
        }

        // when
        for (int i = 0; i < repeatCount; i++) {
            long couponId = coupon.getId();
            Long memberId = memberList.get(i).getMemberId();

            new Thread(() -> {
                couponService.download(couponId, memberId);
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
        int remainingQuantity = couponRepository.getById(coupon.getId())
                .getRemainingQuantity();

        // then
        assertThat(remainingQuantity).isEqualTo(30);
    }
}