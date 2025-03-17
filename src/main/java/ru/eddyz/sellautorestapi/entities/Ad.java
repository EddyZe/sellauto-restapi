package ru.eddyz.sellautorestapi.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ad")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ad_id")
    private Long adId;

    private String title;

    @Column(length = 1024)
    private String description;

    @OneToMany(mappedBy = "ad", cascade = CascadeType.REMOVE)
    private List<Price> prices;

    private Boolean isActive;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "car_id", referencedColumnName = "car_id")
    private Car car;

    @OneToMany(mappedBy = "ad", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<Chat> chats;
}
