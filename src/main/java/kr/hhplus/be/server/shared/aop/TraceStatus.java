package kr.hhplus.be.server.shared.aop;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TraceStatus {

    private TraceId traceId;
    private Long startTimeMs;
    private String message;
}
