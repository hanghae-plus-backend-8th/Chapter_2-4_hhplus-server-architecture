package kr.hhplus.be.server.infrastructure.point.persistence;

import kr.hhplus.be.server.domain.point.MemberPoint;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.infrastructure.member.persistence.MemberEntity;
import kr.hhplus.be.server.infrastructure.member.persistence.MemberJpaRepository;
import kr.hhplus.be.server.shared.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public MemberPoint getById(long memberId) {
        return memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundResourceException("유효하지 않은 사용자 식별자입니다."))
                .toMemberPointDomain();
    }

    @Override
    public MemberPoint getByIdLocking(long memberId) {
        return memberJpaRepository.findByIdLocking(memberId)
                .orElseThrow(() -> new NotFoundResourceException("유효하지 않은 사용자 식별자입니다."))
                .toMemberPointDomain();
    }

    @Override
    public void updatePoint(long memberId, long point) {
        MemberEntity memberEntity = memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundResourceException("유효하지 않은 사용자 식별자입니다."));

        memberEntity.updatePoint(point);
    }
}
