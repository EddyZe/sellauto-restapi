package ru.eddyz.sellautorestapi.entities;


import jakarta.persistence.*;
import lombok.*;
import ru.eddyz.sellautorestapi.enums.Role;

import java.util.List;

@Entity
@Table(name = "account")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    private String email;

    private String phoneNumber;

    private String password;

    private boolean blocked;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "account")
    private List<RefreshToken> refreshToken;


    @ToString.Include(name = "password")
    private String maskPassword() {
        return "********";
    }
}
