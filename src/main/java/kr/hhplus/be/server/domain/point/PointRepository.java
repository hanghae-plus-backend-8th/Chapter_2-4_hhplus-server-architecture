package kr.hhplus.be.server.domain.point;

public interface PointRepository {

    MemberPoint getById(long memberId);
    MemberPoint getByIdLocking(long memberId);
    void updatePoint(long memberId, long point);
}
