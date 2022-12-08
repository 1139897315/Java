package com.ithaorong.reggie.api;

import com.ithaorong.reggie.entity.Dish;
import com.ithaorong.reggie.entity.Setmeal;
import com.ithaorong.reggie.service.DishService;
import com.ithaorong.reggie.service.SetmealService;
import com.ithaorong.reggie.vo.ResultVO;
import com.sun.webkit.network.URLs;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传和下载接口
 */
@RestController
@CrossOrigin
@Slf4j
@Api(value = "提供文件相关接口",tags = "文件管理")
@RequestMapping("/common")
public class FileController {
    @Value("${reggie.path}")
    private String basePath;
    @Resource
    private DishService dishService;
    @Resource
    private SetmealService setmealService;

    /**
     * 前端似乎带不了token，后期看是否可以维护，否则可能导致网络攻击服务器被图片内存占满
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public ResultVO upload(MultipartFile file) {
        //获取原始文件名后缀名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用UUID随机生成文件名
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        //需要判断是否存在该目录结构，否则创建目录
        File dir = new File(basePath);
        //当前目录不存在
        if (!dir.exists()){
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(basePath+'/'+fileName));
        } catch (IOException e) {
            return ResultVO.error("出现异常，文件上传失败");
        }

        return ResultVO.success("上传文件成功！",fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try {
            //输入流，通过输入流读取文件内容
            FileInputStream inputStream = new FileInputStream(new File(basePath + name));

            //输出流，通过输出流将文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();

            //设置返回类型
            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[2048];
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
