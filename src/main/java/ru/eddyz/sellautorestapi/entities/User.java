package ru.eddyz.sellautorestapi.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "usr")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    private String firstName;

    private String lastName;

    @OneToOne(cascade = CascadeType.REMOVE)
    @PrimaryKeyJoinColumn
    private Account account;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Ad> ads;

    @ManyToMany(mappedBy = "users")
    private List<Chat> chats;

    @OneToMany(mappedBy = "from")
    private List<Message> sendMessages;

}
