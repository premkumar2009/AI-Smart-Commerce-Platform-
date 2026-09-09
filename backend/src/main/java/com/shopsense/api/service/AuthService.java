package com.shopsense.api.service;

import com.shopsense.api.dto.AuthDtos.*;
import com.shopsense.api.entity.User;
import com.shopsense.api.repository.UserRepository;
import com.shopsense.api.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) { this.users = users; this.encoder = encoder; this.jwt = jwt; }
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        User user = new User(); user.setEmail(email); user.setFirstName(request.firstName().trim()); user.setLastName(request.lastName().trim()); user.setPasswordHash(encoder.encode(request.password())); users.save(user);
        return response(user);
    }
    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email().trim()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!user.isEnabled() || !encoder.matches(request.password(), user.getPasswordHash())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        return response(user);
    }
    private AuthResponse response(User user) { return new AuthResponse(jwt.createToken(user.getEmail(), user.getRole().name()), "Bearer", user.getId().toString(), user.getEmail(), user.getFirstName(), user.getRole().name()); }
}
