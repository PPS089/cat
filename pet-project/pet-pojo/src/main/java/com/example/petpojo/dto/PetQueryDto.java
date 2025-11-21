package com.example.petpojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PetQueryDto {
    private Integer pageSize=12;
    private Integer currentPage=1;
    private String breed;
    private String gender;
    private Integer minAge;
    private Integer maxAge;
}
