package com.luo.lease.web.admin.service.impl;

import com.luo.lease.common.minio.MinioProperties;
import com.luo.lease.web.admin.service.FileService;
import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    // 使用Spring的依赖注入自动装配MinIO配置属性
    @Autowired
    private MinioProperties properties;

    // 自动注入MinIO客户端实例（需提前配置）
    @Autowired
    private MinioClient client;

    /**
     * 文件上传方法
     *
     * @param file Spring MVC接收的MultipartFile文件对象
     * @return 返回上传后文件的完整访问URL（当上传失败时返回null）
     */
    @Override
    public String upload(MultipartFile file) throws Exception {
            // 检查目标存储桶是否存在
            boolean bucketExists = client.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(properties.getBucketName())
                            .build()
            );

            // 如果存储桶不存在则创建并设置访问策略
            if (!bucketExists) {
                // 创建新存储桶
                client.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(properties.getBucketName())
                                .build()
                );

                // 设置存储桶策略（允许公开读取）
                client.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(properties.getBucketName())
                                .config(createBucketPolicyConfig(properties.getBucketName()))
                                .build()
                );
            }

            // 生成唯一文件名：日期目录 + UUID + 原始文件名（防止重复）
            String filename = new SimpleDateFormat("yyyyMMdd").format(new Date())
                    + "/" + UUID.randomUUID()
                    + "-" + file.getOriginalFilename();

            // 执行文件上传操作
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucketName())   // 存储桶名称
                            .object(filename)                    // 目标文件名
                            .stream(file.getInputStream(), file.getSize(), -1) // 文件流和大小
                            .contentType(file.getContentType())  // 自动识别文件类型
                            .build()
            );

            // 拼接完整的文件访问URL（格式：endpoint/bucketName/filename）
            return String.join("/",
                    properties.getEndpoint(),
                    properties.getBucketName(),
                    filename);
    }

    /**
     * 创建存储桶访问策略配置（JSON格式）
     *
     * @param bucketName 存储桶名称
     * @return 策略配置JSON字符串
     */
    private String createBucketPolicyConfig(String bucketName) {
    /*
    策略说明：
    - Version: 策略语法版本
    - Statement: 权限声明列表
      - Action: 允许的操作（s3:GetObject表示下载权限）
      - Effect: 允许（Allow）或拒绝（Deny）
      - Principal: "*" 表示对所有用户生效
      - Resource: 策略应用的资源路径（arn格式）
     */
        return """
                {
                  "Statement" : [ {
                    "Action" : "s3:GetObject",
                    "Effect" : "Allow",
                    "Principal" : "*",
                    "Resource" : "arn:aws:s3:::%s/*"
                  } ],
                  "Version" : "2012-10-17"
                }
                """.formatted(bucketName);
    }

}
