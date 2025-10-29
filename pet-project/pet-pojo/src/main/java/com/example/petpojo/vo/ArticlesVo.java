package com.example.petpojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticlesVo {


    private Integer id;

    private String title;

    private String content;

    private String summary;

    private String author;

    private String coverImage;

    private Integer viewCount;

    private LocalDateTime createdAt;

}
