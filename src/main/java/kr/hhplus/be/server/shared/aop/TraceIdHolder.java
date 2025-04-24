package kr.hhplus.be.server.shared.aop;

public abstract class TraceIdHolder {

    private static final ThreadLocal<TraceId> traceIdHolder = new ThreadLocal<>();
    public static void set(TraceId traceId) {
        traceIdHolder.set(traceId);
    }
    public static TraceId get() {
        return traceIdHolder.get();
    }
    public static void clear() {
        traceIdHolder.remove();
    }
}
