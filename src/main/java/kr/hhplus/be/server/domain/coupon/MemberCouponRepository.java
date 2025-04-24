package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.shared.dto.ListDto;

public interface MemberCouponRepository {

    MemberCoupon save(MemberCoupon memberCoupon);
    MemberCoupon getByCouponNumberLocking(String couponNumber);
    void updateStatus(long memberCouponId, MemberCouponStatus memberCouponStatus);
    ListDto<MemberCoupon> list(long memberId, int start, int limit);
}
