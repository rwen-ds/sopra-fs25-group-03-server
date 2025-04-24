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


@Component
public class AuthFilter extends OncePerRequestFilter {
    private static final String AUTH_HEADER = "Authorization";
    private final UserRepository userRepository;

    public AuthFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        if (requestURI.equals("/") ||
                (method.equals("POST") && requestURI.equals("/users")) ||
                requestURI.startsWith("/users/login") ||
                requestURI.startsWith("/users/logout") ||
                requestURI.startsWith("/translate") ||
                requestURI.startsWith("/h2-console") ||
                requestURI.startsWith("/favicon.ico") ||
                requestURI.startsWith("/requests") ||
                requestURI.startsWith("/messages")) {
            filterChain.doFilter(request, response);
            return;
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
