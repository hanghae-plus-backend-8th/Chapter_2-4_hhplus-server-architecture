package kr.hhplus.be.server.application.point;

import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.application.point.PointResult.PointChargeResult;
import kr.hhplus.be.server.application.point.PointResult.PointInfoResult;
import kr.hhplus.be.server.domain.point.*;
import kr.hhplus.be.server.shared.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Retryable(retryFor = OptimisticLockException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional
    public PointChargeResult charge(long memberId, long amount) {
        MemberPoint memberPoint = pointRepository.getByIdLocking(memberId);

        memberPoint.charge(amount);

        pointRepository.updatePoint(memberPoint.getMemberId(), memberPoint.getPoint());
        pointHistoryRepository.save(PointHistory.create(memberId, TransactionType.CHARGE, amount));

        return new PointChargeResult(memberPoint);
    }

    public PointInfoResult info(long memberId) {
        MemberPoint memberPoint = pointRepository.getById(memberId);

        return new PointInfoResult(memberPoint);
    }
}
