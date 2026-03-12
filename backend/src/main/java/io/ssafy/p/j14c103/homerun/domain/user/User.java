package io.ssafy.p.j14c103.homerun.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = EmailConverter.class)
    @Column(nullable = false, unique = true)
    private Email email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String passwordHash;

    @Column(unique = true)
    private String ssafyUserKey;

    private User(Email email, String name, String passwordHash, String ssafyUserKey) {
        this.email = email;
        this.name = name;
        this.passwordHash = passwordHash;
        this.ssafyUserKey = ssafyUserKey;
    }

    public static User create(Email email, String name, String passwordHash, String ssafyUserKey) {
        return new User(email, name, passwordHash, ssafyUserKey);
    }
}
