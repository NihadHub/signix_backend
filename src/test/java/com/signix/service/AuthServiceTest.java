package com.signix.service;

import com.signix.dto.AuthResponse;
import com.signix.dto.LoginRequest;
import com.signix.dto.RegisterRequest;
import com.signix.exception.EmailAlreadyExistsException;
import com.signix.model.User;
import com.signix.repository.UserRepository;
import com.signix.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldSucceed_whenEmailNotUsed() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@signix.ma");
        request.setPassword("password123");
        request.setFullName("New User");

        when(userRepository.existsByEmail("new@signix.ma")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("fake-jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@signix.ma");
        request.setPassword("password123");
        request.setFullName("Existing User");

        when(userRepository.existsByEmail("existing@signix.ma")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldSucceed_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("owner@signix.ma");
        request.setPassword("password123");

        User user = User.builder()
                .id(1L)
                .email("owner@signix.ma")
                .fullName("Owner")
                .build();

        when(userRepository.findUserByEmail("owner@signix.ma")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
