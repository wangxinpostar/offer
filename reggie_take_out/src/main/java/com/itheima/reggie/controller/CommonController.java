package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 文件上传和下载
 *
 * @author wangxinpo
 * @date 2023/09/02
 */
@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basepath;

    /**
     * 上传文件
     *
     * @param file
     * @return {@code R<String>}
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        log.info("上传文件：{}", file.getOriginalFilename());

        String orginalFilename = file.getOriginalFilename();
        String suffix = orginalFilename.substring(orginalFilename.lastIndexOf("."));
        String filename = System.currentTimeMillis() + suffix;

        File dir = new File(basepath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(basepath + filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(filename);
    }

    /**
     * 下载文件
     *
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {

        try {
//            输入流，读取文件
            FileInputStream fileInputStream = new FileInputStream(new File(basepath + name));
//            输出流，写入文件
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

//            关闭流
            fileInputStream.close();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
