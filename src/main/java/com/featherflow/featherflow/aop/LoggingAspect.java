package com.featherflow.featherflow.aop;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    private final MeterRegistry meterRegistry;

    public LoggingAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("@annotation(com.featherflow.featherflow.aop.LogExecution) || within(@com.featherflow.featherflow.aop.LogExecution *)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTimeMillis = System.currentTimeMillis();
        String methodSignature = joinPoint.getSignature().toShortString();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        List<Tag> tags = Arrays.asList(
                Tag.of("class", className),
                Tag.of("method", methodName)
        );
        try {
            logger.info("Entering {}", methodSignature);
            Object result = joinPoint.proceed();
            long durationMillis = System.currentTimeMillis() - startTimeMillis;
            Timer.builder("featherflow.method.execution")
                    .description("Method execution time")
                    .tags(tags)
                    .register(meterRegistry)
                    .record(durationMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
            logger.info("Exiting {} ({} ms)", methodSignature, durationMillis);
            return result;
        } catch (Throwable throwable) {
            long durationMillis = System.currentTimeMillis() - startTimeMillis;
            Timer.builder("featherflow.method.exceptions")
                    .description("Method exceptions count and latency")
                    .tags(tags)
                    .tag("exception", throwable.getClass().getSimpleName())
                    .register(meterRegistry)
                    .record(durationMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
            logger.error("Exception in {} after {} ms: {}", methodSignature, durationMillis, throwable.toString());
            throw throwable;
        }
    }
}


