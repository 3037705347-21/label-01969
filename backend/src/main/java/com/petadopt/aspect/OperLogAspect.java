package com.petadopt.aspect;

import cn.hutool.json.JSONUtil;
import com.petadopt.entity.OperationLog;
import com.petadopt.mapper.OperationLogMapper;
import com.petadopt.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final OperationLogMapper operationLogMapper;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint point, OperLog operLog) throws Throwable {
        Object result = null;
        String resultStr = "success";
        try {
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            resultStr = "error: " + e.getMessage();
            throw e;
        } finally {
            try {
                saveLog(point, operLog, resultStr);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }
    }

    private void saveLog(ProceedingJoinPoint point, OperLog operLog, String resultStr) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        
        OperationLog log = new OperationLog();
        log.setUserId(UserContext.getUserId());
        log.setModule(operLog.module());
        log.setAction(operLog.action());
        
        Object[] args = point.getArgs();
        if (args != null && args.length > 0) {
            try {
                log.setParams(JSONUtil.toJsonStr(args));
            } catch (Exception e) {
                log.setParams("参数序列化失败");
            }
        }
        
        log.setResult(resultStr);
        log.setIp(getIpAddress());
        log.setCreateTime(LocalDateTime.now());
        
        operationLogMapper.insert(log);
    }

    private String getIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            // ignore
        }
        return "unknown";
    }
}
