package com.sky.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.sky.properties.AliOssProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 阿里云OSS工具类（完整优化版）
 * 功能：文件上传、下载、删除、生成访问URL等
 */

@Data
@AllArgsConstructor
@Component
@Slf4j
public class AliOssUtil {

    @Autowired
    private AliOssProperties aliOssProperties;

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    // OSS客户端实例（线程安全）
    private OSS ossClient;

    // 连接池配置
    private int maxConnections = 100;
    private int connectionTimeout = 5000; // ms
    private int socketTimeout = 50000;    // ms

    /**
     * 默认构造函数（用于Spring依赖注入）
     */
    public AliOssUtil() {
        // 属性将通过setter注入
    }

    /**
     * 初始化OSS客户端
     */
    @PostConstruct
    public void init() {
        // 创建OSS客户端实例
            this.endpoint = aliOssProperties.getEndpoint();
            this.accessKeyId = aliOssProperties.getAccessKeyId();
            this.accessKeySecret = aliOssProperties.getAccessKeySecret();
            this.bucketName = aliOssProperties.getBucketName();

        if (ossClient != null) {
            log.warn("OSS客户端已初始化，跳过重复初始化");
            return;
        }

        // 验证必要配置
        validateConfig();

        try {
            // 标准化endpoint（移除协议前缀）
            String normalizedEndpoint = normalizeEndpoint(endpoint);

            // 创建凭证提供器
            CredentialsProvider credentialsProvider = new DefaultCredentialProvider(
                    accessKeyId, accessKeySecret);

            // 配置连接池
            ClientBuilderConfiguration config = new ClientBuilderConfiguration();
            config.setMaxConnections(maxConnections);
            config.setConnectionTimeout(connectionTimeout);
            config.setSocketTimeout(socketTimeout);

            // 创建OSS客户端
            this.ossClient = new OSSClientBuilder().build(
                    normalizedEndpoint,
                    credentialsProvider,
                    config
            );

            // 验证Bucket是否存在
            if (!ossClient.doesBucketExist(bucketName)) {
                log.error("指定的Bucket不存在: {}", bucketName);
                throw new IllegalStateException("Bucket不存在: " + bucketName);
            }

            log.info("OSS客户端初始化成功 | Endpoint: {} | Bucket: {}", normalizedEndpoint, bucketName);
        } catch (Exception e) {
            log.error("OSS客户端初始化失败", e);
            throw new RuntimeException("OSS初始化失败", e);
        }
    }

    /**
     * 销毁OSS客户端
     */
    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            try {
                ossClient.shutdown();
                log.info("OSS客户端已关闭");
            } catch (Exception e) {
                log.error("关闭OSS客户端时出错", e);
            }
        }
    }

    /**
     * 文件上传
     *
     * @param bytes      文件内容字节数组
     * @param objectName 对象路径（包含目录）
     * @return 文件访问URL
     */
    public String upload(byte[] bytes, String objectName) {
        return upload(bytes, objectName, null);
    }

    /**
     * 文件上传（带元数据）
     *
     * @param bytes      文件内容字节数组
     * @param objectName 对象路径
     * @param contentType 文件MIME类型
     * @return 文件访问URL
     */
    public String upload(byte[] bytes, String objectName, String contentType) {
        // 参数校验
        validateUploadParams(bytes, objectName);

        // 标准化对象名称
        String normalizedObjectName = normalizeObjectName(objectName);

        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            // 创建对象元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);

            if (StringUtils.hasText(contentType)) {
                metadata.setContentType(contentType);
            } else {
                // 尝试根据扩展名推断类型
                String detectedType = detectContentType(objectName);
                if (detectedType != null) {
                    metadata.setContentType(detectedType);
                }
            }

            // 执行上传
            ossClient.putObject(bucketName, normalizedObjectName, inputStream, metadata);

            // 生成访问URL
            String fileUrl = generateFileUrl(normalizedObjectName);
            log.info("文件上传成功 | Bucket: {} | Object: {} | Size: {} bytes",
                    bucketName, normalizedObjectName, bytes.length);
            return fileUrl;
        } catch (OSSException | ClientException e) {
            log.error("OSS文件上传失败 | Bucket: {} | Object: {}", bucketName, normalizedObjectName, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("处理文件流失败", e);
        }
    }

    /**
     * 下载文件
     *
     * @param objectName 对象路径
     * @return 文件内容字节数组
     */
    public byte[] download(String objectName) {
        validateObjectName(objectName);
        String normalizedObjectName = normalizeObjectName(objectName);

        try (OSSObject ossObject = ossClient.getObject(bucketName, normalizedObjectName);
             InputStream inputStream = ossObject.getObjectContent()) {

            byte[] content = IOUtils.toByteArray(inputStream);
            log.info("文件下载成功 | Bucket: {} | Object: {} | Size: {} bytes",
                    bucketName, normalizedObjectName, content.length);
            return content;
        } catch (OSSException | ClientException e) {
            log.error("OSS文件下载失败 | Bucket: {} | Object: {}", bucketName, normalizedObjectName, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("读取文件流失败", e);
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 对象路径
     */
    public void delete(String objectName) {
        validateObjectName(objectName);
        String normalizedObjectName = normalizeObjectName(objectName);

        try {
            ossClient.deleteObject(bucketName, normalizedObjectName);
            log.info("文件删除成功 | Bucket: {} | Object: {}", bucketName, normalizedObjectName);
        } catch (OSSException | ClientException e) {
            log.error("OSS文件删除失败 | Bucket: {} | Object: {}", bucketName, normalizedObjectName, e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成文件访问URL（默认有效期1小时）
     *
     * @param objectName 对象路径
     * @return 带签名的访问URL
     */
    public String generatePresignedUrl(String objectName) {
        return generatePresignedUrl(objectName, 1, TimeUnit.HOURS);
    }

    /**
     * 生成文件访问URL（自定义有效期）
     *
     * @param objectName 对象路径
     * @param duration   有效期时长
     * @param unit       时间单位
     * @return 带签名的访问URL
     */
    public String generatePresignedUrl(String objectName, long duration, TimeUnit unit) {
        validateObjectName(objectName);
        String normalizedObjectName = normalizeObjectName(objectName);

        try {
            // 计算过期时间
            Date expiration = new Date(System.currentTimeMillis() + unit.toMillis(duration));

            // 生成签名URL
            URL url = ossClient.generatePresignedUrl(bucketName, normalizedObjectName, expiration);
            return url.toString();
        } catch (OSSException | ClientException e) {
            log.error("生成签名URL失败 | Bucket: {} | Object: {}", bucketName, normalizedObjectName, e);
            throw new RuntimeException("生成访问URL失败", e);
        }
    }

    // ======================== 私有工具方法 ========================

    /**
     * 验证配置参数
     */
    private void validateConfig() {
        if (!StringUtils.hasText(endpoint)) {
            throw new IllegalStateException("OSS endpoint未配置");
        }
        if (!StringUtils.hasText(accessKeyId)) {
            throw new IllegalStateException("OSS accessKeyId未配置");
        }
        if (!StringUtils.hasText(accessKeySecret)) {
            throw new IllegalStateException("OSS accessKeySecret未配置");
        }
        if (!StringUtils.hasText(bucketName)) {
            throw new IllegalStateException("OSS bucketName未配置");
        }
    }

    /**
     * 验证上传参数
     */
    private void validateUploadParams(byte[] bytes, String objectName) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("文件内容不能为空");
        }
        validateObjectName(objectName);
    }

    /**
     * 验证对象名称
     */
    private void validateObjectName(String objectName) {
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("对象名称不能为空");
        }
        if (objectName.contains("..") || objectName.contains("//")) {
            throw new IllegalArgumentException("对象名称包含非法路径: " + objectName);
        }
        if (objectName.length() > 1024) {
            throw new IllegalArgumentException("对象名称过长");
        }
    }

    /**
     * 标准化endpoint
     */
    private String normalizeEndpoint(String endpoint) {
        // 移除协议前缀和尾部斜杠
        return endpoint.replaceFirst("^(http://|https://)", "")
                .replaceAll("/+$", "");
    }

    /**
     * 标准化对象名称
     */
    private String normalizeObjectName(String objectName) {
        // 移除开头斜杠和多余斜杠
        String normalized = objectName.replaceAll("^/+", "")
                .replaceAll("/{2,}", "/");

        // 检查是否为空
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("对象名称无效: " + objectName);
        }

        return normalized;
    }

    /**
     * 生成基础文件访问URL
     */
    private String generateFileUrl(String objectName) {
        return "https://" + bucketName + "." + endpoint + "/" + objectName;
    }

    /**
     * 根据文件扩展名检测内容类型
     */
    private String detectContentType(String objectName) {
        String lowerName = objectName.toLowerCase();
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerName.endsWith(".png")) {
            return "image/png";
        } else if (lowerName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerName.endsWith(".txt")) {
            return "text/plain";
        } else if (lowerName.endsWith(".html")) {
            return "text/html";
        }
        return null;
    }
}