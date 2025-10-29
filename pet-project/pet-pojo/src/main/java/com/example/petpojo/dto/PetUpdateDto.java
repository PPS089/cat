package com.example.petpojo.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PetUpdateDto {
    @NotBlank(message = "宠物ID不能为空")
    private Long pid;
    @NotBlank(message = "名字不能为空")
    private String name;
    @NotBlank(message = "物种不能为空")
    private String species;
    @NotBlank(message = "品种不能为空")
    private String breed;
    @NotBlank(message = "年龄不能为空")
    private Integer age;
    @NotBlank(message = "性别不能为空")
    private String gender;

}
