package com.personal.sirestproxy.components;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private static final int MAX_REQUESTS = 15; 
    private static final int TIME_WINDOW_MINUTES = 1;
    
    private final Map<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        
        String clientId = getClientId(request);
        RequestInfo requestInfo = requestCounts.computeIfAbsent(
            clientId, 
            k -> new RequestInfo()
        );
        
        if (requestInfo.isExpired()) {
            requestInfo.reset();
        }
        
        if (requestInfo.getCount() >= MAX_REQUESTS) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\": \"Límite de requests excedido. Intenta más tarde.\"}"
            );
            return false;
        }
        
        requestInfo.increment();
        
        response.addHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS));
        response.addHeader("X-RateLimit-Remaining", 
            String.valueOf(MAX_REQUESTS - requestInfo.getCount()));
        response.addHeader("X-RateLimit-Reset", 
            requestInfo.getResetTime().toString());
        
        return true;
    }
    
    private String getClientId(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        
        return  ip;
    }
    
    private class RequestInfo {
        private final AtomicInteger count;
        private LocalDateTime windowStart;
        
        public RequestInfo() {
            this.count = new AtomicInteger(0);
            this.windowStart = LocalDateTime.now();
        }
        
        public void increment() {
            count.incrementAndGet();
        }
        
        public int getCount() {
            return count.get();
        }
        
        public void reset() {
            count.set(0);
            windowStart = LocalDateTime.now();
        }
        
        public boolean isExpired() {
            long minutesElapsed = ChronoUnit.MINUTES.between(
                windowStart, 
                LocalDateTime.now()
            );
            return minutesElapsed >= TIME_WINDOW_MINUTES;
        }
        
        public LocalDateTime getResetTime() {
            return windowStart.plusMinutes(TIME_WINDOW_MINUTES);
        }
    }
}
