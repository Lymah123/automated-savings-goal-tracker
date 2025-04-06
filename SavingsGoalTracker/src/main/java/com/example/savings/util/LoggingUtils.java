package com.example.savings.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class LoggingUtils {
    private static final Logger logger = LoggerFactory.getLogger(LoggingUtils.class);

    /**
     * Log detailed information about an HTTP request
     */
    public void logRequest(HttpServletRequest request, Object body) {
        StringBuilder requestLog = new StringBuilder();
        Map<String, String> parameters = buildParametersMap(request);

        requestLog.append("REQUEST ");
        requestLog.append("method=[").append(request.getMethod()).append("] ");
        requestLog.append("path=[").append(request.getRequestURI()).append("] ");

        if (!parameters.isEmpty()) {
            requestLog.append("parameters=[").append(parameters).append("] ");
        }

        if (body != null) {
            requestLog.append("body=[").append(body).append("]");
        }

        logger.info(requestLog.toString());
    }

    /**
     * Log detailed information about an API response
     */
    public void logResponse(HttpServletRequest request, Object body, long executionTime) {
        StringBuilder responseLog = new StringBuilder();

        responseLog.append("RESPONSE ");
        responseLog.append("method=[").append(request.getMethod()).append("] ");
        responseLog.append("path=[").append(request.getRequestURI()).append("] ");
        responseLog.append("executionTime=[").append(executionTime).append("ms] ");

        if (body != null) {
            responseLog.append("body=[").append(body).append("]");
        }

        logger.info(responseLog.toString());
    }

    /**
     * Log detailed information about an exception
     */
    public void logException(HttpServletRequest request, Exception e, long executionTime) {
        StringBuilder exceptionLog = new StringBuilder();

        exceptionLog.append("EXCEPTION ");
        exceptionLog.append("method=[").append(request.getMethod()).append("] ");
        exceptionLog.append("path=[").append(request.getRequestURI()).append("] ");
        exceptionLog.append("executionTime=[").append(executionTime).append("ms] ");
        exceptionLog.append("exception=[").append(e.getClass().getName()).append("] ");
        exceptionLog.append("message=[").append(e.getMessage()).append("]");

        logger.error(exceptionLog.toString(), e);
    }

    /**
     * Log application events with structured data
     */
    public void logEvent(String eventType, String userId, Map<String, Object> details) {
        StringBuilder eventLog = new StringBuilder();

        eventLog.append("EVENT ");
        eventLog.append("type=[").append(eventType).append("] ");
        eventLog.append("userId=[").append(userId).append("] ");

        if (details != null && !details.isEmpty()) {
            eventLog.append("details=[").append(details).append("]");
        }

        logger.info(eventLog.toString());
    }

    /**
     * Log performance metrics
     */
    public void logPerformance(String operation, long executionTime, Map<String, Object> metadata) {
        StringBuilder perfLog = new StringBuilder();

        perfLog.append("PERFORMANCE ");
        perfLog.append("operation=[").append(operation).append("] ");
        perfLog.append("executionTime=[").append(executionTime).append("ms] ");

        if (metadata != null && !metadata.isEmpty()) {
            perfLog.append("metadata=[").append(metadata).append("]");
        }

        logger.info(perfLog.toString());
    }

    /**
     * Extract parameters from request
     */
    private Map<String, String> buildParametersMap(HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            String value = request.getParameter(key);
            resultMap.put(key, value);
        }

        return resultMap;
    }
}
