package com.app.prod.user.service;

import com.app.prod.config.security.jwt.JwtService;
import com.app.prod.exceptions.exceptions.EntityNotPresentException;
import com.app.prod.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.sources.tables.AppUser;
import org.jooq.sources.tables.records.AppUserRecord;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AppUserRecord findByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException(String.format("User with username: %s does not exist.", username))
        );
    }

    public AppUserRecord findById(UUID userId){
        return userRepository.findById(userId).orElseThrow(
                () -> new EntityNotPresentException(
                        String.format("User with id: %s does not exist.", userId),
                        AppUser.class.getSimpleName()
                )
        );
    }

    public String generateToken(String username, String password){
        var auth = new UsernamePasswordAuthenticationToken(username, password);
        Authentication result = authenticationManager.authenticate(auth);
        UserDetails ud = (UserDetails) result.getPrincipal();
        return jwtService.generateToken((org.springframework.security.core.userdetails.User) ud);
    }

    public void updatePassword(UUID userId, String password){
        userRepository.updatePassword(userId, password);
    }

}
