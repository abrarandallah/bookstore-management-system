package com.abrar.BOOKSTORE.Login.auth;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public JwtAuthenticationEntryPoint() {
    }



//    @Override
//    public void commence(HttpServletRequest httpServletRequest,
//                         HttpServletResponse httpServletResponse,
//                         AuthenticationException e) throws IOException, ServletException {
//        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
//
//    }

    @Override
    public void commence(jakarta.servlet.http.HttpServletRequest request,
                         jakarta.servlet.http.HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, jakarta.servlet.ServletException {
        response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
