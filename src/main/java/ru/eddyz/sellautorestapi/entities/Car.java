package ru.eddyz.sellautorestapi.entities;


import jakarta.persistence.*;
import lombok.*;
import ru.eddyz.sellautorestapi.enums.BodyType;
import ru.eddyz.sellautorestapi.enums.DriveMode;
import ru.eddyz.sellautorestapi.enums.EngineType;
import ru.eddyz.sellautorestapi.enums.TransmissionType;

import java.util.List;

@Entity
@Table(name = "car")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private Long carId;

    private Integer year;

    private String vin;

    private Integer mileage;

    @Enumerated(EnumType.STRING)
    private EngineType engineType;

    @Enumerated(EnumType.STRING)
    private TransmissionType transmissionType;

    @Enumerated(EnumType.STRING)
    private BodyType bodyType;

    @Enumerated(EnumType.STRING)
    private DriveMode drive;

    @ManyToOne
    @JoinColumn(name = "brand_id", referencedColumnName = "brand_id")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "model_id", referencedColumnName = "model_id")
    private Model model;

    @OneToMany(mappedBy = "car", cascade = CascadeType.REMOVE)
    private List<Photo> photos;

    @ManyToOne
    @JoinColumn(name = "color_id", referencedColumnName = "color_id")
    private Color color;

    @OneToOne
    @JoinColumn(name = "ad_id", referencedColumnName = "ad_id")
    private Ad ad;

}
