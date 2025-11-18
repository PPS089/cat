package com.example.petservice.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.example.petcommon.properties.AliyunOSSProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AliyunOSSOperator {

    private final AliyunOSSProperties aliyunOSSProperties;

    public String upload(byte[] content, String originalFilename) throws Exception {
        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();
        String accessKeyId = aliyunOSSProperties.getAccessKeyId();
        String accessKeySecret = aliyunOSSProperties.getAccessKeySecret();
        
        // 检查必要配置是否设置
        if (accessKeyId == null || accessKeySecret == null || accessKeyId.isEmpty() || accessKeySecret.isEmpty()) {
            throw new IllegalArgumentException("OSS访问密钥未正确配置");
        }
        
        if (endpoint == null || endpoint.isEmpty()) {
            throw new IllegalArgumentException("OSS endpoint未正确配置");
        }
        
        if (bucketName == null || bucketName.isEmpty()) {
            throw new IllegalArgumentException("OSS bucketName未正确配置");
        }
        
        // 使用配置文件中的访问凭证
        DefaultCredentialProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);

        // 填写Object完整路径，例如202406/1.png。Object完整路径中不能包含Bucket名称。
        //获取当前系统日期的字符串,格式为 yyyy/MM
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        //生成一个新的不重复的文件名
        String newFileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = dir + "/" + newFileName;

        // 创建OSSClient实例。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        // 设置连接超时时间
        clientBuilderConfiguration.setConnectionTimeout(300000); // 5分钟
        // 设置读取超时时间
        clientBuilderConfiguration.setSocketTimeout(300000); // 5分钟
        // 设置最大连接数
        clientBuilderConfiguration.setMaxConnections(100);
        
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            // 检查bucket是否存在
            if (!ossClient.doesBucketExist(bucketName)) {
                throw new Exception("OSS bucket不存在: " + bucketName);
            }
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));
        } catch (Exception e) {
            log.error("OSS上传失败: {}", e.getMessage(), e);
            throw new Exception("OSS上传失败: " + e.getMessage(), e);
        } finally {
            ossClient.shutdown();
        }

        // 合并getFileUrl方法的逻辑
        return "https://" + bucketName + "." + endpoint + "/" + objectName;
    }
    
    /**
     * 删除OSS上的文件
     * @param fileUrl OSS文件的完整URL
     */
    public void delete(String fileUrl) throws Exception {
        if (fileUrl == null || (!fileUrl.startsWith("https://") && !fileUrl.startsWith("http://"))) {
            throw new IllegalArgumentException("无效的OSS文件URL: " + fileUrl);
        }
        // 从URL中提取objectName，跳过协议和域名部分
        String objectName = fileUrl.substring(fileUrl.indexOf("/", 8) + 1);
        
        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();
        String accessKeyId = aliyunOSSProperties.getAccessKeyId();
        String accessKeySecret = aliyunOSSProperties.getAccessKeySecret();
        
        // 检查必要配置是否设置
        if (accessKeyId == null || accessKeySecret == null || accessKeyId.isEmpty() || accessKeySecret.isEmpty()) {
            throw new IllegalArgumentException("OSS访问密钥未正确配置");
        }
        
        // 使用配置文件中的访问凭证
        DefaultCredentialProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);

        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        // 设置连接超时时间
        clientBuilderConfiguration.setConnectionTimeout(60000); // 1分钟
        // 设置读取超时时间
        clientBuilderConfiguration.setSocketTimeout(60000); // 1分钟
        
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            // 检查bucket是否存在
            if (!ossClient.doesBucketExist(bucketName)) {
                throw new Exception("OSS bucket不存在: " + bucketName);
            }
            ossClient.deleteObject(bucketName, objectName);
        } catch (Exception e) {
            log.error("OSS删除失败: {}", e.getMessage(), e);
            throw new Exception("OSS删除失败: " + e.getMessage(), e);
        } finally {
            ossClient.shutdown();
        }
    }
}