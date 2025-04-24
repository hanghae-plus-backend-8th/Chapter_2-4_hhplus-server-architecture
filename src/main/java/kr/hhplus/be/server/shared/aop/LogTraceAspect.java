package kr.hhplus.be.server.shared.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogTraceAspect {

    private final LogTrace logTrace;

    @Around("execution(* kr.hhplus.be.server.interfaces..*(..)) || " +
            "execution(* kr.hhplus.be.server.application..*(..)) || " +
            "execution(* kr.hhplus.be.server.infrastructure..*(..))")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {

        TraceStatus status = null;
        Class declaringType = joinPoint.getSignature().getDeclaringType();

        // Interface 타입인 경우, 별도의 로그를 남기지 않음
        if (declaringType.isInterface()) {
            return joinPoint.proceed();
        }

        try {
            // Message
            StringBuilder message = new StringBuilder();

            // Method Signature
            message.append(joinPoint.getSignature().toShortString())
                   .append(" | Params : ")
                   .append(Arrays.stream(joinPoint.getArgs()).toList());

            // begin, proceed, end
            status = logTrace.begin(message.toString());
            Object result = joinPoint.proceed();
            logTrace.end(status);

            return result;

        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}
