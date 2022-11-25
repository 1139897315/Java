package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.entity.Desk;
import com.ithaorong.reggie.entity.Employee;
import com.ithaorong.reggie.entity.UserDesk;
import com.ithaorong.reggie.service.DeskService;
import com.ithaorong.reggie.service.UserDeskService;
import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
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
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private UserDeskService userDeskService;

    /**
     * 添加桌子
     * @param desk
     * @return
     */
    @PostMapping("/save")
    public ResultVO save(@RequestHeader String token,@RequestBody Desk desk){
        Long storeId;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            storeId = objectMapper.readValue(s, Employee.class).getStoreId();
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        synchronized (this){
            desk.setId(0L);
            desk.setStatus(0);
            desk.setStoreId(storeId);
            deskService.save(desk);
            return ResultVO.success("添加成功！");
        }
    }

    @GetMapping("/page")
    public ResultVO page(@RequestHeader String token,int page, int pageSize,String name){
        //若不存在，则添加用户                （添加用户信息和设置为新用户）
        Long storeId;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            storeId = objectMapper.readValue(s, Employee.class).getStoreId();
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Desk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Desk::getStoreId,storeId);
        //执行查询，当name不为空
        if(name!=null && name.length() > 0){
            for (int i = 0; i < name.length(); i++) {
                if (!Character.isWhitespace(name.charAt(i))){
                    queryWrapper.like(Desk::getName,name);
                }

            }
        }
        //添加排序条件
        queryWrapper.orderByDesc(Desk::getId);

        deskService.page(pageInfo,queryWrapper);
        return ResultVO.success("查询成功！", pageInfo);
    }
    /**
     * 修改桌子
     * @param desk
     * @return
     */
    @PutMapping("/update")
    @Transactional
    public ResultVO update(@RequestHeader String token,@RequestBody Desk desk){
        //若不存在，则添加用户                （添加用户信息和设置为新用户）
        Long userId;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            userId = objectMapper.readValue(s, Employee.class).getId();
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        synchronized (this){
            deskService.updateById(desk);

            UserDesk userDesk = new UserDesk();
            userDesk.setId(0L);
            userDesk.setUserId(userId);
            userDesk.setDeskId(desk.getId());
            userDeskService.save(userDesk);

            return ResultVO.success("修改成功！");
        }
    }



    /**
     *
     * @param id
     * @return
     */
    @DeleteMapping("/delete")
    public ResultVO delete(@RequestHeader String token,Long id){
        synchronized (this){
            deskService.removeById(id);
            return ResultVO.success("删除成功！");
        }
    }

    @GetMapping("/getDeskById")
    public ResultVO getDeskById(@RequestHeader String token,Long id){
        Long storeId;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            storeId = objectMapper.readValue(s, Employee.class).getStoreId();
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        LambdaQueryWrapper<Desk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Desk::getId,id);
        queryWrapper.eq(Desk::getStoreId,storeId);

        Desk desk = deskService.getOne(queryWrapper);
        return ResultVO.success("查询成功！",desk);
    }

    @GetMapping("/list")
    public ResultVO listByStore(@RequestHeader String token,Long storeId){
        LambdaQueryWrapper<Desk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Desk::getStoreId,storeId);
        List<Desk> list = deskService.list(queryWrapper);
        return ResultVO.success("查询成功！",list);
    }
}
