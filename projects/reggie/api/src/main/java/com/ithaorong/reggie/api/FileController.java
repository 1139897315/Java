package com.ithaorong.reggie.api;

import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传和下载接口
 */
@RestController
@CrossOrigin
@Slf4j
@Api(value = "提供文件相关接口",tags = "文件上传和下载管理")
@RequestMapping("/common")
public class FileController {
    @PostMapping("/upload")
    public ResultVO upload(MultipartFile file){
        return ResultVO.success("上传文件成功！");
    }
}
