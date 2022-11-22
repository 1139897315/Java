package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ithaorong.reggie.entity.Store;
import com.ithaorong.reggie.service.StoreService;
import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.xml.transform.Result;
import java.time.LocalDateTime;
import java.util.List;

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
            store.setCreateTime(LocalDateTime.now());
            store.setUpdateTime(LocalDateTime.now());
            store.setIsDelete(0);
            store.setRanking(0);
            store.setDayCustomers(0L);
            store.setMonthCustomers(0L);
            store.setYearCustomers(0L);
            store.setDayTurnover(0L);
            store.setMonthTurnover(0L);
            store.setYearTurnover(0L);
            boolean is_OK = storeService.save(store);
            if (is_OK)
                return ResultVO.success("保存成功！");
            return ResultVO.error("保存出错！");
        }
    }

    /**
     *
     * @param store id必传，另外需要修改哪些字段就传哪些字段
     * @return
     */
    @PutMapping("/update")
    public ResultVO update(@RequestBody Store store){
        synchronized (this){
            store.setUpdateTime(LocalDateTime.now());
            storeService.updateById(store);
            return ResultVO.success("修改成功！");
        }
    }
    @DeleteMapping("/delete")
    public ResultVO delete(Long id){
        synchronized (this){
            LambdaUpdateWrapper<Store> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Store::getId,id)
                    .set(Store::getIsDelete,1);
            storeService.update(updateWrapper);
            return ResultVO.success("删除成功！");
        }
    }
    @GetMapping("/getStoreById")
    public ResultVO getStoreById(Long id){
        LambdaQueryWrapper<Store> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Store::getId,id);
        Store store = storeService.getOne(queryWrapper);
        return ResultVO.success("查询成功！",store);
    }
    @GetMapping("/list")
    public ResultVO list(){
        List<Store> list = storeService.list();
        return ResultVO.success("查询成功！",list);
    }

}
