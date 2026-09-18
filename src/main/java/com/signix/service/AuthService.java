package com.signix.service;

import com.signix.dto.AuthResponse;
import com.signix.dto.LoginRequest;
import com.signix.dto.RegisterRequest;
import com.signix.exception.EmailAlreadyExistsException;
import com.signix.model.User;
import com.signix.model.enums.Role;
import com.signix.repository.UserRepository;
import com.signix.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (
                userRepository.existsByEmail(request.getEmail())
        ) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.USER)
                .build();
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    public AuthResponse login (LoginRequest request){
           authenticationManager.authenticate(new UsernamePasswordAuthenticationToken( request.getEmail(), request.getPassword()));

          User user =userRepository.findUserByEmail(request.getEmail())
                   .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable après authentification"));

          String token = jwtService.generateToken(user);
          return AuthResponse.builder()
                  .token(token)
                  .email(user.getEmail())
                  .fullName(user.getFullName())
                  .build();
    }



































}
