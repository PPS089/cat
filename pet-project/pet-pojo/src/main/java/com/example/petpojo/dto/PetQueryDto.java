package com.example.petpojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PetQueryDto {
    private Integer pageSize;
    private Integer currentPage;
    private String breed;
    private String gender;
    private Integer minAge;
    private Integer maxAge;
}
