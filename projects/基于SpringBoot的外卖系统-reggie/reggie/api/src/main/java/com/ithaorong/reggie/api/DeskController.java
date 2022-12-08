package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.dto.DeskDto;
import com.ithaorong.reggie.dto.UserDeskDto;
import com.ithaorong.reggie.entity.*;
import com.ithaorong.reggie.service.DeskService;
import com.ithaorong.reggie.service.UserDeskDetailService;
import com.ithaorong.reggie.service.UserDeskService;
import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@Slf4j
@Api(value = "提供桌号相关接口", tags = "桌号管理")
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
    @Resource
    private UserDeskDetailService userDeskDetailService;

    /**
     * 添加桌子
     *
     * @param desk
     * @return
     */
    @PostMapping("/save")
    public ResultVO save(@RequestHeader String token, @RequestBody Desk desk) {
        Long storeId;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            storeId = objectMapper.readValue(s, Employee.class).getStoreId();
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        synchronized (this) {
            desk.setId(0L);
            desk.setStatus(0);
            desk.setStoreId(storeId);
            deskService.save(desk);
            return ResultVO.success("添加成功！");
        }
    }

    @GetMapping("/page")
    public ResultVO page(@RequestHeader String token, int page, int pageSize, String name) {
        //若不存在，则添加用户                （添加用户信息和设置为新用户）
        Employee employee;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            employee = objectMapper.readValue(s, Employee.class);
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        Long storeId = employee.getStoreId();
        int ranking = employee.getRanking();
        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Desk> queryWrapper = new LambdaQueryWrapper<>();
        if (ranking == 1 || ranking == 2)
            queryWrapper.eq(Desk::getStoreId, storeId);

        //执行查询，当name不为空
        if (name != null && name.length() > 0) {
            for (int i = 0; i < name.length(); i++) {
                if (!Character.isWhitespace(name.charAt(i))) {
                    queryWrapper.like(Desk::getName, name);
                }

            }
        }
        //添加排序条件
        queryWrapper.orderByDesc(Desk::getId);

        deskService.page(pageInfo, queryWrapper);
        return ResultVO.success("查询成功！", pageInfo);
    }

    /**
     * 修改桌子
     *
     * @param
     * @return
     */
    @PutMapping("/update")
    @Transactional
    public ResultVO update(@RequestHeader String token, @RequestBody HashMap<String ,List<DeskDto>> list) {
        //确定预约：
            //
            //改变desk -> status、userDesk -> userId。。
        List<DeskDto> deskDtos = list.get("list");
        if (deskDtos == null || deskDtos.size() == 0)
            return ResultVO.error("请选择桌号！");
        Long userId;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            userId = objectMapper.readValue(s, User.class).getId();
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        synchronized (this) {

            boolean save = false;
            boolean remove = false;
            Long userDeskId = new Long("0");
            for (int i = 0; i < deskDtos.size(); i++) {
                //将桌子状态修改
                boolean b = deskService.updateById(deskDtos.get(i));



                if (b) {
                    //传desk的status为修改后的status
                    if (deskDtos.get(i).getStatus() == 1) {
                        UserDesk userDesk = new UserDesk();
                        userDesk.setId(0L);
                        userDesk.setUserId(userId);

                        if (deskDtos.get(i).getUpdateTime() == null){
                            DateFormat dateformat = new SimpleDateFormat("hh:mm");
                            String strdate = dateformat.format(new Date());
                            userDesk.setUpdateTime(strdate);
                        }else
                            userDesk.setUpdateTime(deskDtos.get(i).getUpdateTime());

                        //只保存一次
                        if (i == 0) {
                            save = userDeskService.save(userDesk);
                            userDeskId = userDesk.getId();
                        }
                        //保存deskId、userDeskId到userDeskDetail表
                        UserDeskDetail userDeskDetail = new UserDeskDetail();
                        userDeskDetail.setId(0L);
                        userDeskDetail.setDeskId(deskDtos.get(i).getId());
                        if (userDeskId == 0L)
                            return ResultVO.error("出现异常！");
                        userDeskDetail.setUserDeskId(userDeskId);
                        save = userDeskDetailService.save(userDeskDetail) & save;

                        if (i == deskDtos.size() - 1){
                            if (save)
                                return ResultVO.success("落座成功");
                            return ResultVO.error("落座失败");
                        }
                    }
                    else if (deskDtos.get(i).getStatus() == 0) {
                        //status修改为0
                        //删除关系表：userDesk、userDeskDetail



                        if (i == 0){
                            LambdaQueryWrapper<UserDeskDetail> queryWrapper = new LambdaQueryWrapper<>();
                            queryWrapper.eq(UserDeskDetail::getDeskId, deskDtos.get(i).getId())
                                        .last("limit 1");
                            UserDeskDetail one = userDeskDetailService.getOne(queryWrapper);

                            LambdaQueryWrapper<UserDesk> userDeskLambdaQueryWrapper = new LambdaQueryWrapper<>();
                            userDeskLambdaQueryWrapper.eq(UserDesk::getId, one.getUserDeskId());
                            remove = userDeskService.remove(userDeskLambdaQueryWrapper);
                        }

                        LambdaQueryWrapper<UserDeskDetail> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(UserDeskDetail::getDeskId, deskDtos.get(i).getId());
                        remove = userDeskDetailService.remove(queryWrapper) & remove;

                        if (i == deskDtos.size() - 1){
                            if (remove)
                                return ResultVO.success("修改成功");
                            return ResultVO.error("修改失败");
                        }
                    }
                }else {
                    if (i != 0)
                        for (int j = i;j >= 0;j--){
                            if (deskDtos.get(j).getStatus() == 1)
                                deskDtos.get(j).setStatus(0);
                            else
                                deskDtos.get(j).setStatus(1);
                            deskService.updateById(deskDtos.get(j));
                        }
                }
            }
        }
        return ResultVO.error("修改失败");
    }

    /**
     * @param id
     * @return
     */
    @DeleteMapping("/delete")
    public ResultVO delete(@RequestHeader String token, Long id) {
        synchronized (this) {
            boolean b = deskService.removeById(id);
            if (b)
                return ResultVO.success("删除成功！");
            return ResultVO.error("删除失败！");
        }
    }

    @GetMapping("/getDeskById")
    public ResultVO getDeskById(@RequestHeader String token, Long id) {
        Long storeId;
        try {
            String s = stringRedisTemplate.opsForValue().get(token);
            storeId = objectMapper.readValue(s, Employee.class).getStoreId();
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
        LambdaQueryWrapper<Desk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Desk::getId, id);
        queryWrapper.eq(Desk::getStoreId, storeId);

        Desk desk = deskService.getOne(queryWrapper);
        return ResultVO.success("查询成功！", desk);
    }

    @GetMapping("/list")
    public ResultVO listByStore(@RequestHeader String token, Long storeId) {
        //根据storeId查询桌号
        LambdaQueryWrapper<Desk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Desk::getStoreId, storeId);
        List<Desk> list = deskService.list(queryWrapper);
        return ResultVO.success("查询成功！", list);
    }

    @GetMapping("/listByUserId")
    public ResultVO listByUserId(@RequestHeader String token, Long userId) {
        //根据userId查询桌号
        LambdaQueryWrapper<UserDesk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDesk::getUserId, userId);
        List<UserDesk> list = userDeskService.list(queryWrapper);

        List<UserDeskDto> listDto = new ArrayList<>();

        for (UserDesk userDesk : list) {
            UserDeskDto deskDto = new UserDeskDto();
            BeanUtils.copyProperties(userDesk, deskDto);

            //根据userDeskId查询所有user_desk_detail
            LambdaQueryWrapper<UserDeskDetail> deskDtoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            deskDtoLambdaQueryWrapper.eq(UserDeskDetail::getUserDeskId, deskDto.getId());
            List<UserDeskDetail> userDeskDetails = userDeskDetailService.list(deskDtoLambdaQueryWrapper);

            //根据user_desk_detail依次查询desks
            for (UserDeskDetail userDeskDetail : userDeskDetails) {
                Desk desk = deskService.getById(userDeskDetail.getDeskId());
                deskDto.getDesks().add(desk);
            }
            listDto.add(deskDto);
        }

        return ResultVO.success("查询成功！", listDto);
    }

}
