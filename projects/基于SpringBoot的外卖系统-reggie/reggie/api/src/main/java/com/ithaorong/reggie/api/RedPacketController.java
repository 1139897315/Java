package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.dto.RedPacketDto;
import com.ithaorong.reggie.entity.Employee;
import com.ithaorong.reggie.entity.Order;
import com.ithaorong.reggie.entity.RedPacket;
import com.ithaorong.reggie.entity.UserRedPacket;
import com.ithaorong.reggie.service.RedPacketService;
import com.ithaorong.reggie.service.UserRedPacketService;
import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@Slf4j
@Api(value = "提供红包相关接口",tags = "红包管理")
@RequestMapping("/redPacket")
public class RedPacketController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private RedPacketService redPacketService;
    @Resource
    private UserRedPacketService userRedPacketService;


    @PostMapping("/save")
    public ResultVO save(@RequestHeader String token, @RequestBody RedPacketDto redPacketDto){

        if (redPacketDto.getFullMoney() == null)
            return ResultVO.error("没有设置满额减免");
        if (redPacketDto.getReduceMoney() == null)
            return ResultVO.error("没有设置减免金额");
        if (redPacketDto.getDurationTime() == null)
            return ResultVO.error("没有设置存在时间");

        Employee employee;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            employee = objectMapper.readValue(s, Employee.class);
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        int ranking = employee.getRanking();
        if (ranking == 3 && employee.getStatus() == 1){
            redPacketDto.setId(0L);
            redPacketDto.setIsDeleted(0);
            //默认未发布(未抢)
            redPacketDto.setIsPub(0);
            redPacketDto.setNumber(0);
            redPacketDto.setCreateTime(LocalDateTime.now());
            redPacketDto.setUpdateTime(LocalDateTime.now());
            boolean save = redPacketService.save(redPacketDto);
            if (save)
                return ResultVO.success("添加红包成功！");
        }
        return ResultVO.error("添加红包失败！");
    }

    @PutMapping("/update")
    public ResultVO update(@RequestHeader String token, @RequestBody RedPacketDto redPacketDto){
        if (redPacketDto.getId() == null || redPacketDto.getId() == 0)
            return ResultVO.error("没有设置id");
        RedPacket redPacket = redPacketService.getById(redPacketDto.getId());

        if (redPacketDto.getIsPub() != null && redPacketDto.getIsPub() == 1)
            if (redPacket.getIsDeleted() == 1)
                return ResultVO.error("该红包已删除，请先恢复");
            if (redPacketDto.getNumber() != null && redPacketDto.getNumber() <= 0)
                return ResultVO.error("请填写数量");

        if (redPacketDto.getIsDeleted() != null && redPacketDto.getIsDeleted() == 1)
            if (redPacket.getIsPub() == 1)
                return ResultVO.error("请关闭发布，再删除");

        Employee employee;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            employee = objectMapper.readValue(s, Employee.class);
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        int ranking = employee.getRanking();
        if (ranking == 3 && employee.getStatus() == 1){
            redPacketDto.setUpdateTime(LocalDateTime.now());
            boolean save = redPacketService.updateById(redPacketDto);
            if (save)
                return ResultVO.success("修改红包成功！");
        }
        return ResultVO.error("修改红包失败！");
    }

//    @DeleteMapping("/delete")
//    public ResultVO delete(@RequestHeader String token, Long id){
//        if (id == null || id == 0)
//            return ResultVO.error("没有设置id");
//        Employee employee;
//        try {
//            String s = stringRedisTemplate.opsForValue().get(token);
//            employee = objectMapper.readValue(s, Employee.class);
//        } catch (JsonProcessingException e) {
//            return ResultVO.error("出现异常！");
//        }
//        int ranking = employee.getRanking();
//        if (ranking == 3 && employee.getStatus() == 1){
//            //查询是否还有人存有该红包
////            LambdaQueryWrapper<UserRedPacket> queryWrapper = new LambdaQueryWrapper<>();
////            queryWrapper.eq(UserRedPacket::getRedPacketId,id)
////                    .apply("acquired_time >= date_sub(now(), interval expire_time day)")
////                    .last("limit 1");
////            UserRedPacket one = userRedPacketService.getOne(queryWrapper);
//            //存在
////            if (one != null){
////                return ResultVO.error("存在用户拥有该红包，删除红包失败！");
////            }
//
//            //判断是否发布
//            //未发布，再删除
//            RedPacket one = redPacketService.getById(id);
//            if (one.getIsPub() == 0)
//                return ResultVO.error("请先取消发布，再删除");
//            RedPacket redPacket = new RedPacket();
//            redPacket.setId(id);
//            redPacket.setIsPub(0);
//            redPacket.setIsDeleted(1);
//            redPacket.setUpdateTime(LocalDateTime.now());
//            boolean save = redPacketService.updateById(redPacket);
//            if (save)
//                return ResultVO.success("删除红包成功！");
//        }
//        return ResultVO.error("删除红包失败！");
//    }

    @GetMapping("/page")
    public ResultVO page(@RequestHeader String token,int page, int pageSize,String reduceMoney){
        Employee employee;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            employee = objectMapper.readValue(s, Employee.class);
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        int ranking = employee.getRanking();
        if (ranking == 3){
            //构造分页构造器
            Page pageInfo = new Page(page,pageSize);

            //构造条件构造器
            LambdaQueryWrapper<RedPacket> queryWrapper = new LambdaQueryWrapper<>();

            //执行查询，当name不为空
            if(reduceMoney!=null && reduceMoney.length() > 0){
                for (int i = 0; i < reduceMoney.length(); i++) {
                    if (!Character.isWhitespace(reduceMoney.charAt(i))){
                        //判断是否全为数字
                        for (int j = reduceMoney.length();--j>=0;){
                            if (!Character.isDigit(reduceMoney.charAt(i))){
                                return ResultVO.error("非数字，查询失败！");
                            }
                        }
                        //转换为数字
                        Integer intReduceMoney = Integer.valueOf(reduceMoney);
                        queryWrapper.like(RedPacket::getReduceMoney,intReduceMoney);
                    }
                }
            }
            //添加排序条件
            queryWrapper.orderByDesc(RedPacket::getReduceMoney)
                    .orderByDesc(RedPacket::getUpdateTime);

            redPacketService.page(pageInfo,queryWrapper);
            return ResultVO.success("查询成功！", pageInfo);
        }
        return ResultVO.error("查询失败");
    }

    @GetMapping("/listAll")
    public ResultVO list(@RequestHeader String token){
        Employee employee;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            employee = objectMapper.readValue(s, Employee.class);
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        int ranking = employee.getRanking();
        if (ranking == 3){
            LambdaQueryWrapper<RedPacket> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RedPacket::getIsDeleted,0)
                        .orderByDesc(RedPacket::getReduceMoney);
            List<RedPacket> list = redPacketService.list(queryWrapper);
            if (list == null)
                return ResultVO.error("查询失败");

            return ResultVO.success("查询成功！", list);
        }
        return ResultVO.error("查询失败");
    }

    @GetMapping("/getById")
    public ResultVO getById(@RequestHeader String token, Long id){
        Employee employee;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            employee = objectMapper.readValue(s, Employee.class);
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        int ranking = employee.getRanking();
        if (ranking == 3){
            RedPacket one = redPacketService.getById(id);
            if (one == null)
                return ResultVO.error("查询失败");
            if (one.getIsDeleted() == 1)
                return ResultVO.error("该红包已删除");
            return ResultVO.success("查询成功！", one);
        }
        return ResultVO.error("查询失败");
    }
}
