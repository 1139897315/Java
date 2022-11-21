package com.ithaorong.reggie.api;

import com.ithaorong.reggie.entity.Store;
import com.ithaorong.reggie.service.StoreService;
import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.xml.transform.Result;
import java.time.LocalDateTime;

@CrossOrigin
@RestController
@Slf4j
@Api(value = "提供门店相关接口",tags = "门店管理")
@RequestMapping("/store")
public class StoreController {
    @Resource
    private StoreService storeService;

    @PostMapping("/save")
    public ResultVO save(@RequestBody Store store){
        synchronized (this){
            store.setId(0L);
            store.setStatus(0);
            store.setUpdateTime(LocalDateTime.now());
            store.setIsDelete(0);
            store.setRanking(0);
            storeService.save(store);
            return ResultVO.success("保存成功！");
        }
    }
    @PutMapping("/update")
    public ResultVO update(@RequestBody Store store){

        return ResultVO.success("修改成功！");
    }
    @DeleteMapping("/delete")
    public ResultVO delete(Long id){

        return ResultVO.success("删除成功！");
    }
    @GetMapping("/getStoreById")
    public ResultVO getStoreById(Long id){

        return ResultVO.success("查询成功！");
    }
    @GetMapping("/list")
    public ResultVO list(){

        return ResultVO.success("查询成功！");
    }

}
