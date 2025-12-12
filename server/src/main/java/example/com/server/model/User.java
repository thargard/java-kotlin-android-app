package example.com.server.model;

import jakarta.persistence.*;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduser")
    private Long id;
    @Column(name = "full_name", nullable = true)
    private String fullName;
    @Column(name = "login", nullable = true)
    private String login;
    @Column(name = "email", nullable = true)
    private String email;
    @Column(name = "password", nullable = true)
    private String password;

    public enum Role {
        PRODUCER, CUSTOMER, ADMIN
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    public User() {
    }

    public User(Long id, String fullName, String login, String email, String password, Role role) {
        this.id = id;
        this.fullName = fullName;
        this.login = login;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
