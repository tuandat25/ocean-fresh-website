package com.tuandat.oceanfresh_backend.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tuandat.oceanfresh_backend.components.JwtTokenUtils;
import com.tuandat.oceanfresh_backend.models.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor

public class JwtTokenFilter extends OncePerRequestFilter {
    @Value("${api.prefix}")
    private String apiPrefix;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtils jwtTokenUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String requestPath = request.getServletPath();
            if (requestPath.startsWith("/ws/")) {
                filterChain.doFilter(request, response);
                return;
            }
            String requestMethod = request.getMethod();

            // Xử lý đặc biệt cho endpoint /api/v1/products (không có gì phía sau)
            boolean isProductsEndpoint = requestPath.equals(String.format("%s/products", apiPrefix))
                    && "GET".equalsIgnoreCase(requestMethod);

            if (isProductsEndpoint || !isBypassToken(request)) {
                // Xử lý JWT token cho endpoint /products hoặc các endpoint không bypass
                final String authHeader = request.getHeader("Authorization");

                if (isProductsEndpoint) {
                    // Cho phép cả có token và không có token cho endpoint /products
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        final String token = authHeader.substring(7);
                        final String phoneNumber = jwtTokenUtil.getSubject(token);
                        if (phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                            try {
                                User userDetails = (User) userDetailsService.loadUserByUsername(phoneNumber);
                                if (jwtTokenUtil.validateToken(token, userDetails)) {
                                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities());
                                    authenticationToken
                                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                                }
                            } catch (Exception e) {
                                // Log error nhưng không throw exception để cho phép guest access
                                // Có thể log chi tiết hơn nếu cần debug
                            }
                        }
                    }
                    // Cho phép tiếp tục dù có hay không có valid token
                } else {
                    // Xử lý bình thường cho các endpoint khác cần authentication
                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        response.sendError(
                                HttpServletResponse.SC_UNAUTHORIZED,
                                "authHeader null or not started with Bearer");
                        return;
                    }
                    final String token = authHeader.substring(7);
                    final String phoneNumber = jwtTokenUtil.getSubject(token);
                    if (phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        User userDetails = (User) userDetailsService.loadUserByUsername(phoneNumber);
                        if (jwtTokenUtil.validateToken(token, userDetails)) {
                            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());
                            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        }
                    }
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
        }

    }

    private boolean isBypassToken(@NonNull HttpServletRequest request) {
        final List<Pair<String, String>> bypassTokens = Arrays.asList(
                // Healthcheck request, no JWT token required
                Pair.of(String.format("%s/healthcheck/health", apiPrefix), "GET"),
                Pair.of(String.format("%s/actuator/**", apiPrefix), "GET"),

                Pair.of(String.format("%s/roles**", apiPrefix), "GET"),
                Pair.of(String.format("%s/policies**", apiPrefix), "GET"),
                Pair.of(String.format("%s/comments**", apiPrefix), "GET"),
                Pair.of(String.format("%s/coupons**", apiPrefix), "GET"),

                // Products endpoints - bypass specific endpoints only
                Pair.of(String.format("%s/products/get-all-products", apiPrefix), "GET"),
                Pair.of(String.format("%s/products/images/.*", apiPrefix), "GET"),
                Pair.of(String.format("%s/products/\\d+", apiPrefix), "GET"), // GET by ID: /products/123
                Pair.of(String.format("%s/products/slug/.*", apiPrefix), "GET"), // GET by slug
                Pair.of(String.format("%s/products/\\d+/variants", apiPrefix), "GET"), // GET variants by product ID
                Pair.of(String.format("%s/products/variants/\\d+", apiPrefix), "GET"), // GET variant by ID

                // KHÔNG bypass: String.format("%s/products", apiPrefix), "GET"
                // Endpoint này cần xử lý JWT để phân biệt admin vs user

                Pair.of(String.format("%s/categories**", apiPrefix), "GET"),

                Pair.of(String.format("%s/users/register", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/login", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/profile-images/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/users/refreshToken", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/auth/social/callback", apiPrefix), "GET"),

                // Swagger
                Pair.of("/api-docs", "GET"),
                Pair.of("/api-docs/**", "GET"),
                Pair.of("/swagger-resources", "GET"),
                Pair.of("/swagger-resources/**", "GET"),
                Pair.of("/configuration/ui", "GET"),
                Pair.of("/configuration/security", "GET"),
                Pair.of("/swagger-ui/**", "GET"),
                Pair.of("/swagger-ui.html", "GET"),
                Pair.of("/swagger-ui/index.html", "GET"),

                // Đăng nhập social
                Pair.of(String.format("%s/users/auth/social-login**", apiPrefix), "GET"),
                Pair.of(String.format("%s/users/auth/social/callback**", apiPrefix), "GET"),

                Pair.of(String.format("%s/payments**", apiPrefix), "GET"),
                Pair.of(String.format("%s/payments**", apiPrefix), "POST")

        );

        String requestPath = request.getServletPath();
        String requestMethod = request.getMethod();

        for (Pair<String, String> token : bypassTokens) {
            String path = token.getFirst();
            String method = token.getSecond();
            // Check if the request path and method match any pair in the bypassTokens list
            if (requestPath.matches(path.replace("**", ".*"))
                    && requestMethod.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }
}
