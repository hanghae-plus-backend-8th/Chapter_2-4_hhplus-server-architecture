package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.application.coupon.CouponCommand.CouponListCommand;
import kr.hhplus.be.server.application.coupon.CouponResult.CouponDownloadResult;
import kr.hhplus.be.server.application.coupon.CouponResult.CouponListResult;
import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.MemberCoupon;
import kr.hhplus.be.server.domain.coupon.MemberCouponRepository;
import kr.hhplus.be.server.domain.member.Member;
import kr.hhplus.be.server.domain.member.MemberRepository;
import kr.hhplus.be.server.shared.dto.ListDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final MemberRepository memberRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public CouponDownloadResult download(long couponId, long memberId) {
        Member member = memberRepository.getById(memberId);
        Coupon coupon = couponRepository.getByIdLocking(couponId);

        MemberCoupon memberCoupon = coupon.giveCoupon(member.getMemberId(), LocalDateTime.now());
        memberCouponRepository.save(memberCoupon);
        couponRepository.updateQuantity(coupon.getId(), coupon.getRemainingQuantity());

        return new CouponDownloadResult(memberCoupon);
    }

    public ListDto<CouponListResult> list(CouponListCommand command) {
        return memberCouponRepository.list(command.getMemberId(), command.getStart(), command.getLimit())
                .map(CouponListResult::new);
    }
}
