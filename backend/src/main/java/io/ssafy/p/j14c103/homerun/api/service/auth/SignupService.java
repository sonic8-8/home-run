package io.ssafy.p.j14c103.homerun.api.service.auth;

import io.ssafy.p.j14c103.homerun.api.service.auth.request.SignupServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.auth.response.SignupResponse;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SignupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupResponse signup(SignupServiceRequest request) {
        Email email = Email.of(request.getEmail());

        validateDuplicateEmail(email);

        User user = request.toEntity(
                email,
                passwordEncoder.encode(request.getPassword())
        );

        return SignupResponse.of(userRepository.save(user));
    }

    private void validateDuplicateEmail(Email email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            return;
        }

        throw new HomerunException(ErrorCode.USER_EMAIL_DUPLICATE);
    }
}
