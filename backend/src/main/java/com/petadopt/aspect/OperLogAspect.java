package com.petadopt.aspect;

import cn.hutool.json.JSONUtil;
import com.petadopt.entity.OperationLog;
import com.petadopt.mapper.OperationLogMapper;
import com.petadopt.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class OperLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(OperLogAspect.class);
    private final OperationLogMapper operationLogMapper;

    public OperLogAspect(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

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
                logger.error("保存操作日志失败", e);
            }
        }
    }

    private void saveLog(ProceedingJoinPoint point, OperLog operLog, String resultStr) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        
        OperationLog operLogEntity = new OperationLog();
        operLogEntity.setUserId(UserContext.getUserId());
        operLogEntity.setModule(operLog.module());
        operLogEntity.setAction(operLog.action());
        
        Object[] args = point.getArgs();
        if (args != null && args.length > 0) {
            try {
                operLogEntity.setParams(JSONUtil.toJsonStr(args));
            } catch (Exception e) {
                operLogEntity.setParams("无法序列化参数");
            }
        }
        operLogEntity.setResult(resultStr);
        operLogEntity.setIp(getIp());
        operLogEntity.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(operLogEntity);
    }

    private String getIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "127.0.0.1";
        }
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
}
