package example.com.server.service;

import example.com.server.model.User;
import example.com.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createOrGetUser(String email, String login, String googleId){
        return userRepository.findByEmail(email).orElseGet(() -> {
           User u = new User();
           u.setEmail(email);
           u.setLogin(login);
           u.setRole(User.Role.CUSTOMER); // default role is customer
           return userRepository.save(u);
        });
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Update profile fields (fullName, login, email). Password is not updated here.
     */
    public User updateProfile(Long userId, String fullName, String login, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        if (fullName != null) {
            user.setFullName(fullName);
        }
        if (login != null && !login.isBlank()) {
            Optional<User> existing = userRepository.findByLogin(login);
            if (existing.isPresent() && !existing.get().getId().equals(userId)) {
                throw new IllegalArgumentException("User with this login already exists");
            }
            user.setLogin(login);
        }
        if (email != null) {
            user.setEmail(email);
        }
        return userRepository.save(user);
    }
}
