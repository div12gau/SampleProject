package com.example.sambackend.config;

import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.*;
import java.io.IOException;

import java.util.logging.Logger;

public class CsrfTokenLogger implements Filter {

    private Logger logger =
            Logger.getLogger(CsrfTokenLogger.class.getName());

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain filterChain)
            throws IOException, ServletException {

        Object o = request.getAttribute("_csrf");
        CsrfToken token = (CsrfToken) o;

        logger.info("CSRF token " + token.getToken());
        System.out.print("The token is "+token.getToken());
        filterChain.doFilter(request, response);
    }
}
