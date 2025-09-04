package com.wegotoo.sse.infra.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class ServletUtils {

    public static String findAuthorizationHeaderToRequest() {
        HttpServletRequest request = findServletRequest();
        return request.getHeader("Authorization");
    }

    private static HttpServletRequest findServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        return (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
    }

}
