package example.com.server.service;

import example.com.server.model.User;
import example.com.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(User user) {
        if (user == null || user.getLogin() == null) {
            throw new IllegalArgumentException("Login is required");
        }

        if (userRepository.findByLogin(user.getLogin()).isPresent()) {
            throw new IllegalArgumentException("User with this login already exists");
        }

        // Hash the password before saving
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        userRepository.save(user);
        return user;
    }

    public Optional<User> findByLogin(String login) {
        if (login == null) {
            return Optional.empty();
        }
        return userRepository.findByLogin(login);
    }

    public Optional<User> login(String login, String password) {
        if (login == null || password == null) {
            return Optional.empty();
        }

        return findByLogin(login)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()));
    }

    public Collection<User> getAllUsers() {
        return userRepository.findAll();
    }
}


