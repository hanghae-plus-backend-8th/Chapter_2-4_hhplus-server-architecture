package kr.hhplus.be.server.domain.point;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PointHistory {

    private Long id;
    private long memberId;
    private TransactionType type;
    private long amount;
    private LocalDateTime createdAt;

    public static PointHistory create(
            long memberId,
            @NonNull TransactionType type,
            long amount
    ) {
        if (memberId <= 0) {
            throw new IllegalArgumentException("사용자 식별자가 유효하지 않습니다.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("금액이 유효하지 않습니다.");
        }
        return new PointHistory(null, memberId, type, amount, null);
    }

    public static PointHistory of(
            long id,
            long memberId,
            @NonNull TransactionType type,
            long amount,
            @NonNull LocalDateTime createdAt
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException("포인트 히스토리 식별자가 유효하지 않습니다.");
        }
        if (memberId <= 0) {
            throw new IllegalArgumentException("사용자 식별자가 유효하지 않습니다.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("금액이 유효하지 않습니다.");
        }
        return new PointHistory(id, memberId, type, amount, createdAt);
    }
}