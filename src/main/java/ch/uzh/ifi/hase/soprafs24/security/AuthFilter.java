package ch.uzh.ifi.hase.soprafs24.security;


import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthFilter extends OncePerRequestFilter {
    private static final String AUTH_HEADER = "Authorization";
    private final UserRepository userRepository;

    // whitelist
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/login", // login
            "/h2-console",
            "/favicon.ico"
    );

    public AuthFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        if ("POST".equals(method) && "/users".equals(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        for (String excludedPath : EXCLUDED_PATHS) {
            if (requestURI.startsWith(excludedPath)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String token = request.getHeader(AUTH_HEADER);
        if (token == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
            return;
        }

        User user = userRepository.findByToken(token);
        if (user == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid token");
            return;
        }

        // continue the request
        filterChain.doFilter(request, response);
    }
}
