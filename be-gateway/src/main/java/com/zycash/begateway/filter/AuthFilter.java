package com.zycash.begateway.filter;


import com.zycash.begateway.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    @Autowired
    private RouteValidator routeValidator;
    @Autowired
    private JwtUtils jwtUtils;

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (routeValidator.isSecure.test(exchange.getRequest())) {
                // check header not contain Authorization
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException(Constant.Message.FORBIDDEN_MESSAGE);
                }

                String authHeader = Objects.requireNonNull(exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION)).getFirst();
                if (Objects.nonNull(authHeader) && authHeader.startsWith("Bearer")) {
                    authHeader = authHeader.split("\\s")[1];

                    try {
                        boolean isValid = jwtUtils.validateJwtToken(authHeader);
                        if (!isValid) {
                            throw new RuntimeException(Constant.Message.INVALID_TOKEN_MESSAGE);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(Constant.Message.INVALID_TOKEN_MESSAGE);
                    }

                } else {
                    throw new RuntimeException(Constant.Message.INVALID_TOKEN_MESSAGE);
                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {}

}
