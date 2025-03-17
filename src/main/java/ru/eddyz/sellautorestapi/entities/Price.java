package ru.eddyz.sellautorestapi.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "price")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Integer priceId;

    private Double price;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "ad_id", referencedColumnName = "ad_id")
    private Ad ad;

}
