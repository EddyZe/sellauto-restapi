package ru.eddyz.sellautorestapi.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "car_brand")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Integer brandId;

    @Column(unique = true)
    private String title;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.REMOVE)
    private List<Model> model;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.REMOVE)
    private List<Car> cars;

}
