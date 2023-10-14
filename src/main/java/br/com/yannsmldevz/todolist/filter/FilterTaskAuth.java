package br.com.yannsmldevz.todolist.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.yannsmldevz.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Result;

@Component
public class FilterTaskAuth extends OncePerRequestFilter{

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
                
        var servletPath = request.getServletPath();
        if(servletPath.startsWith("/tasks/")){
            // Pegar a auth
            String auth = request.getHeader("Authorization");
            String authEncoded = auth.substring("Basic".length()).trim();

            byte[] authDecoded = Base64.getDecoder().decode(authEncoded);

            String authString = new String(authDecoded);

            String[] credentials = authString.split(":");

            String username = credentials[0];

            String password = credentials[1];

            // Validar usuario

            var user = this.userRepository.findByUsername(username);
            
            if (user == null){
                response.sendError(401);
            }
            else{
                Result result = BCrypt.verifyer().verify(password.toCharArray(),user.getPassword().toCharArray());

                if(!result.verified){
                    response.sendError(401);
                }
                else{
                    request.setAttribute("idUser",user.getId());
                    filterChain.doFilter(request, response);
                }
            }
        }

        else{
            filterChain.doFilter(request, response);
        }
    }   
}
