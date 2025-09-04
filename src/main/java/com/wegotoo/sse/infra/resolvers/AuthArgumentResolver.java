package com.wegotoo.sse.infra.resolvers;

import com.wegotoo.sse.exception.BusinessException;
import com.wegotoo.sse.exception.ErrorCode;
import com.wegotoo.sse.infra.jwt.JwtTokenProvider;
import com.wegotoo.sse.infra.utils.ServletUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider tokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Auth.class) && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String bearerToken = ServletUtils.findAuthorizationHeaderToRequest();
        if (isNull(bearerToken) || !isValid(bearerToken)) {
            throw new BusinessException(ErrorCode.INVALID_USER);
        }

        return tokenProvider.extractSubId(removeBearer(bearerToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USER));
    }

    private boolean isNull(String bearerToken) {
        return bearerToken == null;
    }

    private boolean isValid(String bearerToken) {
        return tokenProvider.isValid(bearerToken);
    }

    private String removeBearer(String bearerToken) {
        return bearerToken.replace("Bearer ", "");
    }

}
