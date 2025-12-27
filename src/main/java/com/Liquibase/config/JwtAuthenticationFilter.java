package com.Liquibase.config;

import com.Liquibase.utils.JwtUtil;
import com.Liquibase.utils.TokenBlacklist;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends GenericFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklist blacklist;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, TokenBlacklist blacklist) {
        this.jwtUtil = jwtUtil;
        this.blacklist = blacklist;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            if (!blacklist.isBlacklisted(token) && jwtUtil.isTokenValid(token)) {

                String username = jwtUtil.extractUsername(token);
                List<String> roles = jwtUtil.extractRoles(token);

                List<SimpleGrantedAuthority> authorities =
                        roles.stream()
                                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                                .toList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                authorities
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }
}
