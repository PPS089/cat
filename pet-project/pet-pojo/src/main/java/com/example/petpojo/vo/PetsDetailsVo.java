package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor

public class PetsDetailsVo {

     private Integer id;
    private Integer pid;
    private String name;
    private String species;
    private String breed;
    private Integer age;
    private String gender;
    private String status;
    private String description;
    private String image;
    private String healthStatus;
    private Boolean vaccinated;
    private Boolean spayed;
    private Integer adoptionFee;
    private Integer fosterFee;
    private String shelterName;
    private String shelterLocation;
    private Long shelterId;

    
}
