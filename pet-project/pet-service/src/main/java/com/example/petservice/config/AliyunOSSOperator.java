package com.example.petservice.config;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.example.petcommon.error.ErrorCode;
import com.example.petcommon.exception.BizException;
import com.example.petcommon.properties.AliyunOSSProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 33185
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AliyunOSSOperator {

    private final AliyunOSSProperties aliyunOSSProperties;

    public String upload(byte[] content, String originalFilename) {
        // 修复：使用正确的配置前缀 pet.alioss
        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();
        String accessKeyId = aliyunOSSProperties.getAccessKeyId();
        String accessKeySecret = aliyunOSSProperties.getAccessKeySecret();
        
        // 检查必要配置是否设置
        if (accessKeyId == null || accessKeySecret == null || accessKeyId.isEmpty() || accessKeySecret.isEmpty()) {
            throw new BizException(ErrorCode.OSS_CONFIG_ERROR, "OSS访问密钥未正确配置");
        }
        
        if (endpoint == null || endpoint.isEmpty()) {
            throw new BizException(ErrorCode.OSS_CONFIG_ERROR, "OSS endpoint未正确配置");
        }
        
        if (bucketName == null || bucketName.isEmpty()) {
            throw new BizException(ErrorCode.OSS_CONFIG_ERROR, "OSS bucketName未正确配置");
        }
        
        // 使用配置文件中的访问凭证
        DefaultCredentialProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);

        // 填写Object完整路径，例如202406/1.png。Object完整路径中不能包含Bucket名称。
        //获取当前系统日期的字符串,格式为 yyyy/MM
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String safeFileName = originalFilename;
        if (safeFileName == null || safeFileName.isEmpty()) {
            safeFileName = UUID.randomUUID().toString();
        }
        safeFileName = safeFileName.replace("\\", "/");
        if (safeFileName.contains("/")) {
            safeFileName = safeFileName.substring(safeFileName.lastIndexOf('/') + 1);
        }
        safeFileName = safeFileName.trim();
        if (safeFileName.isEmpty()) {
            safeFileName = UUID.randomUUID().toString();
        }
        String objectName = dir + "/" + safeFileName;

        // 创建OSSClient实例。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        // 设置连接超时时间
        clientBuilderConfiguration.setConnectionTimeout(300000); 
        // 设置读取超时时间
        clientBuilderConfiguration.setSocketTimeout(300000); 
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
                throw new BizException(ErrorCode.OSS_OPERATION_FAILED, "OSS bucket不存在: " + bucketName);
            }
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));
        } catch (Exception e) {
            log.error("OSS上传失败: {}", e.getMessage(), e);
            throw new BizException(ErrorCode.FILE_UPLOAD_FAILED, "OSS上传失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }

        // 合并getFileUrl方法的逻辑
        return "https://" + bucketName + "." + endpoint + "/" + objectName;
    }

    public boolean isManagedUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }
        String prefix = aliyunOSSProperties.getUrlPrefix();
        if (prefix != null && !prefix.isEmpty()) {
            return fileUrl.startsWith(prefix);
        }
        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        if (endpoint == null || endpoint.isEmpty() || bucketName == null || bucketName.isEmpty()) {
            return false;
        }
        String normalizedEndpoint = endpoint.replaceFirst("^https?://", "");
        String httpsPrefix = "https://" + bucketName + "." + normalizedEndpoint;
        String httpPrefix = "https://" + bucketName + "." + normalizedEndpoint;
        return fileUrl.startsWith(httpsPrefix) || fileUrl.startsWith(httpPrefix);
    }
    
    /**
     * 删除OSS上的文件
     * @param fileUrl OSS文件的完整URL
     */
    public void delete(String fileUrl) throws Exception {
        if (fileUrl == null || !fileUrl.startsWith("https://")) {
            throw new BizException(ErrorCode.BAD_REQUEST, "无效的OSS文件URL: " + fileUrl);
        }
        // 从URL中提取objectName，跳过协议和域名部分
        String objectName = fileUrl.substring(fileUrl.indexOf("/", 8) + 1);
        
        // 修复：使用正确的配置前缀 pet.alioss
        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();
        DefaultCredentialProvider credentialsProvider = getDefaultCredentialProvider();

        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        // 设置连接超时时间
        clientBuilderConfiguration.setConnectionTimeout(60000);
        // 设置读取超时时间
        clientBuilderConfiguration.setSocketTimeout(60000);

        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            // 检查bucket是否存在
            if (!ossClient.doesBucketExist(bucketName)) {
                throw new BizException(ErrorCode.MEDIA_FILE_DELETE_FAILED, "OSS bucket不存在: " + bucketName);
            }
            ossClient.deleteObject(bucketName, objectName);
        } catch (Exception e) {
            log.error("OSS删除失败: {}", e.getMessage(), e);
            throw new BizException(ErrorCode.MEDIA_FILE_DELETE_FAILED, "OSS删除失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }

    private DefaultCredentialProvider getDefaultCredentialProvider() {
        String accessKeyId = aliyunOSSProperties.getAccessKeyId();
        String accessKeySecret = aliyunOSSProperties.getAccessKeySecret();

        // 检查必要配置是否设置
        if (accessKeyId == null || accessKeySecret == null || accessKeyId.isEmpty() || accessKeySecret.isEmpty()) {
            throw new BizException(ErrorCode.OSS_CONFIG_ERROR, "OSS访问密钥未正确配置");
        }

        // 使用配置文件中的访问凭证
        return new DefaultCredentialProvider(accessKeyId, accessKeySecret);
    }
}
