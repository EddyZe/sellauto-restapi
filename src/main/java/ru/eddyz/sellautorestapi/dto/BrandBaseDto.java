package ru.eddyz.sellautorestapi.dto;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.eddyz.sellautorestapi.entities.Model;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BrandBaseDto {
    private Integer brandId;

    private String title;

    private List<ModelBaseDto> model;
}
