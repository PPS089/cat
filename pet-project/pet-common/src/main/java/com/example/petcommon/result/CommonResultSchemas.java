package com.example.petcommon.result;


import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

/**
 * 用于Swagger文档生成的通用响应结构Schema定义
 * 为不同VO类型提供具体的Schema定义
 */
public class CommonResultSchemas {
    
    /**
     * 401错误响应结构 - 未授权
     */
    @Schema(description = "401错误响应结构")
    public static class UnauthorizedResponse {
        @Schema(description = "错误码", example = "401")
        public Integer code;
        
        @Schema(description = "错误消息", example = "未授权")
        public String msg;
        
        @Schema(description = "错误数据", nullable = true)
        public Object data;
    }
    
    /**
     * 400错误响应结构 - 客户端请求参数错误
     */
    @Schema(description = "400错误响应结构")
    public static class BadRequestResponse {
        @Schema(description = "错误码", example = "400")
        public Integer code;
        
        @Schema(description = "错误消息", example = "请求参数错误")
        public String msg;
        
        @Schema(description = "错误数据", nullable = true)
        public Object data;
    }
    
    /**
     * 400错误响应结构 - 参数验证错误（Map<String, String>类型）
     */
    @Schema(description = "400参数验证错误响应结构")
    public static class ResultMapStringString {
        @Schema(description = "错误码", example = "400")
        public Integer code;
        
        @Schema(description = "错误消息", example = "请求参数错误")
        public String msg;
        
        @Schema(description = "验证错误信息", example = "{\"username\": \"用户名不能为空\", \"password\": \"密码不能为空\"}")
        public Map<String, String> data;
    }
    
    /**
     * 404错误响应结构 - 资源不存在
     */
    @Schema(description = "404错误响应结构")
    public static class NotFoundResponse {
        @Schema(description = "错误码", example = "404")
        public Integer code;
        
        @Schema(description = "错误消息", example = "资源不存在")
        public String msg;
        
        @Schema(description = "错误数据", nullable = true)
        public Object data;
    }
    
    /**
     * 500错误响应结构 - 服务器内部错误
     */
    @Schema(description = "500错误响应结构")
    public static class InternalServerErrorResponse {
        @Schema(description = "错误码", example = "500")
        public Integer code;
        
        @Schema(description = "错误消息", example = "服务器内部错误")
        public String msg;
        
        @Schema(description = "错误数据", nullable = true)
        public Object data;
    }
    
    /**
     * 分页响应结构 - 领养记录
     */
    @Schema(description = "分页响应结构 - 领养记录")
    public static class PageAdoptionsResponse {
        @Schema(description = "响应码", example = "200")
        public Integer code;
        
        @Schema(description = "响应消息", example = "success")
        public String msg;
        
        @Schema(description = "响应数据")
        public PageAdoptionsData data;
        
        @Schema(description = "分页数据")
        public static class PageAdoptionsData {
            @Schema(description = "记录列表")
            public List<com.example.petpojo.vo.AdoptionsVo> records;
            
            @Schema(description = "总记录数", example = "100")
            public Long total;
            
            @Schema(description = "每页记录数", example = "10")
            public Long size;
            
            @Schema(description = "当前页码", example = "1")
            public Long current;
            
            @Schema(description = "总页数", example = "10")
            public Long pages;
        }
    }
    
    /**
     * 分页响应结构 - 寄养记录
     */
    @Schema(description = "分页响应结构 - 寄养记录")
    public static class PageFostersResponse {
        @Schema(description = "响应码", example = "200")
        public Integer code;
        
        @Schema(description = "响应消息", example = "success")
        public String msg;
        
        @Schema(description = "响应数据")
        public PageFostersData data;
        
        @Schema(description = "分页数据")
        public static class PageFostersData {
            @Schema(description = "记录列表")
            public List<com.example.petpojo.vo.FostersVo> records;
            
            @Schema(description = "总记录数", example = "100")
            public Long total;
            
            @Schema(description = "每页记录数", example = "10")
            public Long size;
            
            @Schema(description = "当前页码", example = "1")
            public Long current;
            
            @Schema(description = "总页数", example = "10")
            public Long pages;
        }
    }
    
    /**
     * 结束宠物寄养响应结构
     */
    @Schema(description = "结束宠物寄养响应结构")
    public static class EndPetFosterResponse {
        @Schema(description = "响应码", example = "200")
        public Integer code;
        
        @Schema(description = "响应消息", example = "success")
        public String msg;
        
        @Schema(description = "响应数据")
        public EndPetFosterData data;
        
        @Schema(description = "结束寄养数据")
        public static class EndPetFosterData {
            @Schema(description = "结束日期", example = "2023-10-01T10:00:00")
            public java.time.LocalDateTime end_date;
            
            @Schema(description = "宠物ID", example = "1")
            public Long pet_id;
        }
    }
    
    /**
     * 领养宠物响应结构
     */
    @Schema(description = "领养宠物响应结构")
    public static class AdoptPetResponse {
        @Schema(description = "响应码", example = "200")
        public Integer code;
        
        @Schema(description = "响应消息", example = "success")
        public String msg;
        
        @Schema(description = "响应数据")
        public AdoptPetData data;
        
        @Schema(description = "领养数据")
        public static class AdoptPetData {
            @Schema(description = "宠物ID", example = "1")
            public Integer petId;
            
            @Schema(description = "宠物名称", example = "小白")
            public String petName;
            
            @Schema(description = "物种", example = "狗")
            public String species;
            
            @Schema(description = "品种", example = "金毛")
            public String breed;
            
            @Schema(description = "状态", example = "ADOPTED")
            public String status;
            
            @Schema(description = "领养日期", example = "2023-10-01T10:00:00")
            public java.time.LocalDateTime adoptionDate;
            
            @Schema(description = "消息", example = "领养成功")
            public String message;
        }
    }
    
    /**
     * 删除操作响应结构
     */
    @Schema(description = "删除操作响应结构")
    public static class DeleteOperationResponse {
        @Schema(description = "响应码", example = "200")
        public Integer code;
        
        @Schema(description = "响应消息", example = "删除成功")
        public String msg;
        
        @Schema(description = "响应数据", nullable = true)
        public Object data;
    }
    
    /**
     * 清除操作响应结构
     */
    @Schema(description = "清除操作响应结构")
    public static class ClearOperationResponse {
        @Schema(description = "响应码", example = "200")
        public Integer code;
        
        @Schema(description = "响应消息", example = "清除成功")
        public String msg;
        
        @Schema(description = "响应数据", nullable = true)
        public Object data;
    }
}