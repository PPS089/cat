package com.example.petpojo.vo;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author 33185
 */
@Data
@Schema(description = "管理员视角宠物信息")
public class AdminPetVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer pid;
    private String name;
    private Integer speciesId;
    private String species;
    private Integer breedId;
    private String breed;
    private Integer age;
    private String gender;
    private String status;
    private String image;
    private Integer shelterId;
    private String shelterName;
    private String shelterLocation;
    private Integer adopterId;
    private String adopterName;
    private String adopterPhone;
    private String adopterEmail;
}
