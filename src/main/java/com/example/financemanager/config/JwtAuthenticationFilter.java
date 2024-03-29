package com.example.financemanager.config;

import com.example.financemanager.auth.controller.AuthRepository;
import com.example.financemanager.auth.model.Customer;
import com.example.financemanager.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final int BEARER_PREFIX = 7;

    private final JwtUtil jwtUtil;
    private final AuthRepository authRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, AuthRepository authRepository) {
        this.jwtUtil = jwtUtil;
        this.authRepository = authRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        LOGGER.info("Incoming request {} {} {}", request.getServerName(), request.getRequestURI(), request.getMethod());

        try {
            String token = extractToken(request);
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                Customer customer = authRepository.findByUsernameOrEmail(username);

                if (customer != null) {
                    Authentication auth = new UsernamePasswordAuthenticationToken(customer.getUserName(), customer, null);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    LOGGER.error("User not found with username or email: {}", username);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
        } catch (AuthenticationServiceException ex) {
            SecurityContextHolder.clearContext();
            LOGGER.error("Authentication request for failed: {}", ex.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken)) {
            return bearerToken.substring(BEARER_PREFIX);
        }
        return null;
    }
}