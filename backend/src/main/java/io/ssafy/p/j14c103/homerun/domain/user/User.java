package io.ssafy.p.j14c103.homerun.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Convert(converter = EmailConverter.class)
    @Column(name = "email", nullable = false, unique = true)
    private Email email;

    @Column(name = "user_name", nullable = false)
    private String name;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider_type", nullable = false, length = 20)
    private AuthProvider authProvider;

    @Column(name = "ssafy_user_key", unique = true)
    private String ssafyUserKey;

    @Column(name = "ssafy_connected_at")
    private LocalDateTime ssafyConnectedAt;

    @Column(name = "account_auth_verified_at")
    private LocalDateTime accountAuthVerifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private User(Email email, String name, String passwordHash, AuthProvider authProvider, LocalDateTime createdAt) {
        this.email = email;
        this.name = name;
        this.passwordHash = passwordHash;
        this.authProvider = authProvider;
        this.createdAt = createdAt;
    }

    public static User register(Email email, String name, String passwordHash) {
        return new User(email, name, passwordHash, AuthProvider.EMAIL, LocalDateTime.now());
    }

    public boolean hasSsafyLink() {
        return ssafyUserKey != null && ssafyConnectedAt != null;
    }

    public void linkSsafy(String ssafyUserKey, LocalDateTime connectedAt) {
        this.ssafyUserKey = ssafyUserKey;
        this.ssafyConnectedAt = connectedAt;
    }

    public void markAccountVerified(LocalDateTime verifiedAt) {
        this.accountAuthVerifiedAt = verifiedAt;
    }
}
