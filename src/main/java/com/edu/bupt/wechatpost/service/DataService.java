package com.edu.bupt.wechatpost.service;


import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface DataService {
    String uploadImage(MultipartFile image) throws IOException;
    void downloadImage(String fileName, HttpServletRequest request, HttpServletResponse response)throws IOException;
}
