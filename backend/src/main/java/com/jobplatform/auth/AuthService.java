package com.jobplatform.auth;

import com.jobplatform.auth.dto.LoginRequest;
import com.jobplatform.auth.dto.LoginResponse;
import com.jobplatform.auth.dto.RegisterRequest;
import com.jobplatform.auth.dto.RegisterResponse;
import com.jobplatform.candidate.CandidateProfileService;
import com.jobplatform.exception.BadRequestException;
import com.jobplatform.recruiter.RecruiterProfileService;
import com.jobplatform.security.JwtService;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRepository;
import com.jobplatform.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CandidateProfileService candidateProfileService;
    private final RecruiterProfileService recruiterProfileService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService,
                       CandidateProfileService candidateProfileService,
                       RecruiterProfileService recruiterProfileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.candidateProfileService = candidateProfileService;
        this.recruiterProfileService = recruiterProfileService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(userDetails);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!user.getEnabled()) {
            throw new BadRequestException("Account is disabled");
        }

        log.info("User logged in successfully: {}", normalizedEmail);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs() / 1000)
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        validatePasswordMatch(request.getPassword(), request.getConfirmPassword());
        validateRole(request.getRole());

        String normalizedEmail = normalizeEmail(request.getEmail());
        validateEmailFormat(normalizedEmail);
        checkDuplicateEmail(normalizedEmail);

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(normalizedEmail)
                .password(hashedPassword)
                .role(request.getRole())
                .enabled(true)
                .build();

        try {
            User savedUser = userRepository.save(user);
            createProfile(savedUser);
            log.info("User registered successfully: {}", normalizedEmail);
            return mapToResponse(savedUser);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Duplicate email detected during registration: {}", normalizedEmail);
            throw new BadRequestException("An account with this email already exists");
        }
    }

    private void createProfile(User user) {
        switch (user.getRole()) {
            case CANDIDATE -> candidateProfileService.createProfile(user);
            case RECRUITER -> recruiterProfileService.createProfile(user);
            default -> { }
        }
    }

    private void validatePasswordMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new BadRequestException("Passwords do not match");
        }
    }

    private void validateRole(UserRole role) {
        if (role == UserRole.ADMIN) {
            throw new BadRequestException("Admin registration is not allowed through public registration");
        }
    }

    private void validateEmailFormat(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BadRequestException("Email must be a valid email address");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private void checkDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists");
        }
    }

    private RegisterResponse mapToResponse(User user) {
        return RegisterResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
