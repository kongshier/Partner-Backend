package com.shier.partner.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Shier
 */
public interface OssService {
    //上传头像到OSS
    String uploadFileAvatar(MultipartFile file);

}
