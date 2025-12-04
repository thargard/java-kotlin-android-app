package example.com.server.service;

import example.com.server.model.User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AuthService {

    private final Map<String, User> usersByLogin = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public User register(User user) {
        if (user == null || user.getLogin() == null) {
            throw new IllegalArgumentException("Login is required");
        }

        if (existsByLogin(user.getLogin())) {
            throw new IllegalArgumentException("User with this login already exists");
        }

        user.setId(idGenerator.getAndIncrement());
        usersByLogin.put(user.getLogin(), user);
        return user;
    }

    public Optional<User> findByLogin(String login) {
        if (login == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersByLogin.get(login));
    }

    public boolean existsByLogin(String login) {
        if (login == null) {
            return false;
        }
        return usersByLogin.containsKey(login);
    }

    public Optional<User> login(String login, String password) {
        if (login == null || password == null) {
            return Optional.empty();
        }

        return findByLogin(login)
                .filter(user -> password.equals(user.getPassword()));
    }

    public Collection<User> getAllUsers() {
        return usersByLogin.values();
    }
}


