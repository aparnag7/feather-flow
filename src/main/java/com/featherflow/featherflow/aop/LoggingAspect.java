package com.featherflow.featherflow.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(com.featherflow.featherflow.aop.LogExecution) || within(@com.featherflow.featherflow.aop.LogExecution *)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTimeMillis = System.currentTimeMillis();
        String methodSignature = joinPoint.getSignature().toShortString();
        try {
            logger.info("Entering {}", methodSignature);
            Object result = joinPoint.proceed();
            long durationMillis = System.currentTimeMillis() - startTimeMillis;
            logger.info("Exiting {} ({} ms)", methodSignature, durationMillis);
            return result;
        } catch (Throwable throwable) {
            long durationMillis = System.currentTimeMillis() - startTimeMillis;
            logger.error("Exception in {} after {} ms: {}", methodSignature, durationMillis, throwable.toString());
            throw throwable;
        }
    }
}


