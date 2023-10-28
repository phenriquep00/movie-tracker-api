package com.phenriquep00.movietracker.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.phenriquep00.movietracker.model.UserModel;
import com.phenriquep00.movietracker.repository.IUserRepository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterMovieAuth extends OncePerRequestFilter
{
    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException 
        {
            var servletPath = request.getServletPath();
            if(!servletPath.startsWith("/movie"))
            {
                filterChain.doFilter(request, response);
                return;
            } else 
            {
                // get user credentials
                String[] credentials = this.getBasicAuthCredentials(request);
                if (credentials == null) 
                {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                String username = credentials[0];
                String password = credentials[1];

                // check if user exists
                UserModel user = this.userRepository.findByUsername(username);

                if (user == null)
                {
                    response.sendError(401, "Unauthorized (invalid user)");
                } else
                {
                    // validate password
                    var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

                    if (!passwordVerify.verified)
                    {
                        response.sendError(401, "Unauthorized (invalid password)");
                        return;
                    }

                    request.setAttribute("userId", user.getId());
                    filterChain.doFilter(request, response);
                }
            }

        }

    
    private String[] getBasicAuthCredentials(HttpServletRequest request)
    {
        String authorization = request.getHeader("Authorization");

        if (authorization == null)
        {
            return null;
        }

        String[] parts = authorization.split(" ");

        if (parts.length != 2)
        {
            return null;
        }

        if (!parts[0].equalsIgnoreCase("Basic"))
        {
            return null;
        }

        return new String(Base64.getDecoder().decode(parts[1])).split(":");
    }

}
