package ru.eddyz.sellautorestapi.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "color_car")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "color_id")
    private Integer colorId;

    @Column(unique = true)
    private String title;

    @OneToMany(mappedBy = "color", cascade = CascadeType.REMOVE)
    private List<Car> car;

}
