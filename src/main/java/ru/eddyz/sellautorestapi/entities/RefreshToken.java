package ru.eddyz.sellautorestapi.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Table(name = "refresh_token")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 4096)
    private String token;

    private boolean blocked;

    private Date expiredDate;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "account_id")
    private Account account;


}
