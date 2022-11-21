package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ithaorong.reggie.entity.Desk;
import com.ithaorong.reggie.service.DeskService;
import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@CrossOrigin
@RestController
@Slf4j
@Api(value = "提供桌号相关接口",tags = "桌号管理")
@RequestMapping("/desk")
public class DeskController {

    @Resource
    private DeskService deskService;

    /**
     * 添加桌子
     * @param desk
     * @return
     */
    @PostMapping("/save")
    public ResultVO save(@RequestBody Desk desk){
        synchronized (this){
            desk.setId(0L);
            deskService.save(desk);
            return ResultVO.success("添加成功！");
        }
    }

    /**
     * 修改桌子
     * @param desk
     * @return
     */
    @PutMapping("/update")
    public ResultVO update(@RequestBody Desk desk){
        synchronized (this){
            deskService.updateById(desk);
            return ResultVO.success("修改成功！");
        }
    }

    /**
     *
     * @param id
     * @return
     */
    @DeleteMapping("/delete")
    public ResultVO delete(Long id){
        synchronized (this){
            deskService.removeById(id);
            return ResultVO.success("删除成功！");
        }
    }

    @GetMapping("/getDeskById")
    public ResultVO getDeskById(Long id){
        LambdaQueryWrapper<Desk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Desk::getId,id);
        Desk desk = deskService.getOne(queryWrapper);
        return ResultVO.success("查询成功！",desk);
    }

    @GetMapping("/list")
    public ResultVO listByStore(Long storeId){
        LambdaQueryWrapper<Desk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Desk::getStoreId,storeId);
        List<Desk> list = deskService.list(queryWrapper);
        return ResultVO.success("查询成功！",list);
    }
}
