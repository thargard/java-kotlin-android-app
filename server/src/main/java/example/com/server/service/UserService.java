package example.com.server.service;

import example.com.server.model.User;
import example.com.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
