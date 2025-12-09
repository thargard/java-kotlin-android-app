package example.com.server.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import example.com.server.model.User;
import example.com.server.service.AuthService;
import example.com.server.service.GoogleTokenVerifier;
import example.com.server.service.JwtService;
import example.com.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final GoogleTokenVerifier verifier;
    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    public AuthController(AuthService authService, GoogleTokenVerifier verifier, UserService userService, JwtService jwtService) {
        this.authService = authService;
        this.verifier = verifier;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User created = authService.register(user);
            // объект который вернется клиенту, но типа здесь не должно быть пароля даже зашифрованного
            created.setPassword(null); 
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String login = credentials.get("login");
        String password = credentials.get("password");

        return authService.login(login, password)
                .map(user -> {
                    user.setPassword(null);
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @GetMapping
    public Collection<User> getAll() {
        return authService.getAllUsers();
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody Map<String, String> credentials) throws Exception {
        String token = credentials.get("token");

        try {
            GoogleIdToken.Payload payload = verifier.verify(token);
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String googleId = payload.getSubject();

            User user = userService.createOrGetUser(email, name, googleId);

            String jwt = jwtService.generateToken(user);

            return ResponseEntity.ok(Map.of("token", jwt));
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}


