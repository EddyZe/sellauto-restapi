package ru.eddyz.sellautorestapi.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "model_car")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id")
    private Integer modelId;

    private String title;

    @ManyToOne
    @JoinColumn(name = "brand_id", referencedColumnName = "brand_id")
    private Brand brand;

    @OneToMany(mappedBy = "model", cascade = CascadeType.REMOVE)
    private List<Car> cars;

}
