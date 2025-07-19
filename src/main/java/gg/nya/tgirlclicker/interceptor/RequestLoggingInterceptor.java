package gg.nya.tgirlclicker.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to log request details such as client IP and user agent.
 * This interceptor can be used to enrich logs with request-specific information.
 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    /**
     * Pre-handle method to log client IP and user agent before the request is processed.
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler for the request
     * @return Always true to continue the request processing
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        MDC.put("clientIp", getClientIpAddress(request));
        MDC.put("userAgent", getUserAgent(request));
        return true;
    }

    /**
     * Post-handle method to clean up MDCs after the request is processed.
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler for the request
     * @param ex any exception that occurred during request processing
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.clear();
    }

    /**
     * Utility method to extract the client IP address from the request.
     * @param request the HTTP request
     * @return the client IP address as a String
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Utility method to extract the user agent from the request.
     * @param request the HTTP request
     * @return the user agent as a String
     */
    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent") != null ? request.getHeader("User-Agent") : "Unknown";
    }
}