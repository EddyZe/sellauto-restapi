package ru.eddyz.sellautorestapi.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "photo_car")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long photoId;

    private String filePath;

    @ManyToOne
    @JoinColumn(name = "car_id", referencedColumnName = "car_id")
    private Car car;

}
