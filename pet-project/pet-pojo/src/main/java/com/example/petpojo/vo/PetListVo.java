package com.example.petpojo.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PetListVo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer pid;
    private String name;
    private String species;
    private String breed;
    private Integer age;
    private String gender;
    private String imgUrl;
    private String status;
    private String shelterName;
    private String shelterAddress;
}