package kr.hhplus.be.server.infrastructure.coupon.persistence;

import kr.hhplus.be.server.domain.coupon.MemberCoupon;
import kr.hhplus.be.server.domain.coupon.MemberCouponRepository;
import kr.hhplus.be.server.domain.coupon.MemberCouponStatus;
import kr.hhplus.be.server.infrastructure.member.persistence.MemberEntity;
import kr.hhplus.be.server.infrastructure.member.persistence.MemberJpaRepository;
import kr.hhplus.be.server.shared.dto.ListDto;
import kr.hhplus.be.server.shared.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberCouponRepositoryImpl implements MemberCouponRepository {

    private final MemberJpaRepository memberJpaRepository;
    private final CouponJpaRepository couponJpaRepository;
    private final MemberCouponJpaRepository memberCouponJpaRepository;

    @Override
    public MemberCoupon save(MemberCoupon memberCoupon) {
        CouponEntity couponEntity = couponJpaRepository.findById(memberCoupon.getCoupon().getId())
                .orElseThrow(() -> new NotFoundResourceException("유효하지 않은 쿠폰 식별자입니다."));
        MemberEntity memberEntity = memberJpaRepository.findById(memberCoupon.getMemberId())
                .orElseThrow(() -> new NotFoundResourceException("유효하지 않은 사용자 식별자입니다."));
        MemberCouponEntity memberCouponEntity = MemberCouponEntity.builder()
                .coupon(couponEntity)
                .member(memberEntity)
                .couponNumber(memberCoupon.getCouponNumber())
                .status(memberCoupon.getStatus())
                .build();

        return memberCouponJpaRepository.save(memberCouponEntity).toDomain();
    }

    @Override
    public MemberCoupon getByCouponNumberLocking(String couponNumber) {
        return memberCouponJpaRepository.findByCouponNumber(couponNumber)
                .orElseThrow(() -> new NotFoundResourceException("유효하지 않은 쿠폰번호입니다."))
                .toDomain();
    }

    @Override
    public void updateStatus(long memberCouponId, MemberCouponStatus status) {
        MemberCouponEntity memberCouponEntity = memberCouponJpaRepository.findById(memberCouponId)
                .orElseThrow(() -> new NotFoundResourceException("유효하지 않은 쿠폰번호 식별자입니다."));

        memberCouponEntity.updateStatus(status);
    }

    @Override
    public ListDto<MemberCoupon> list(long memberId, int start, int limit) {
        return memberCouponJpaRepository.list(memberId, start, limit)
                .map(MemberCouponEntity::toDomain);
    }
}
