package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.entity.AddressBook;
import com.ithaorong.reggie.service.AddressBookService;
import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@RestController
@CrossOrigin
@Slf4j
@Api(value = "提供地址相关接口",tags = "地址管理")
@RequestMapping("/address")
public class AddressBookController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private AddressBookService addressBookService;

    @PostMapping("/save")
    public ResultVO save(@RequestBody AddressBook addressBook){

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,addressBook.getUserId())
                    .eq(AddressBook::getIsDeleted,0);
        List<AddressBook> list = addressBookService.list(queryWrapper);

        if (list.size() == 0){
            addressBook.setId(0L);
            addressBook.setIsDefault(1);
            addressBook.setIsDeleted(0);

            addressBook.setCreateTime(LocalDateTime.now());
            addressBook.setUpdateTime(LocalDateTime.now());

            addressBookService.save(addressBook);
            return ResultVO.success("保存成功！");
        }else {
            synchronized (this) {
                addressBook.setId(0L);
                addressBook.setIsDefault(0);
                addressBook.setIsDeleted(0);

                addressBook.setCreateTime(LocalDateTime.now());
                addressBook.setUpdateTime(LocalDateTime.now());

                addressBookService.save(addressBook);
                return ResultVO.success("保存成功！");
            }
        }
    }

    @DeleteMapping("/delete")
    public ResultVO delete(@RequestParam("id") Long id){
        synchronized (this){
            AddressBook addressBook = new AddressBook();
            addressBook.setId(id);
            addressBook.setIsDeleted(1);
            addressBook.setUpdateTime(LocalDateTime.now());
            addressBookService.updateById(addressBook);
            return ResultVO.success("删除成功！");
        }
    }

    @PutMapping("/update")
    public ResultVO update(@RequestBody AddressBook addressBook){
        synchronized (this){
            addressBook.setIsDeleted(0);
            addressBook.setUpdateTime(LocalDateTime.now());
            addressBookService.updateById(addressBook);

            return ResultVO.success("修改成功！");
        }
    }

    //修改默认(参数：id和isDefault、userId)
    @PutMapping("/updateIsDefault")
    public ResultVO updateIsDefault(@RequestBody AddressBook addressBook){
        synchronized (this){
            LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AddressBook::getIsDefault,1)
                    .eq(AddressBook::getUserId,addressBook.getUserId())
                    .set(AddressBook::getIsDefault,0);
            boolean is_OK = addressBookService.update(updateWrapper);
            if (is_OK){
                LambdaUpdateWrapper<AddressBook> updateWrapper_1 = new LambdaUpdateWrapper<>();
                updateWrapper_1.eq(AddressBook::getId,addressBook.getId());
                updateWrapper_1.set(AddressBook::getIsDefault,1);
                updateWrapper_1.set(AddressBook::getUpdateTime,LocalDateTime.now());
                addressBookService.update(updateWrapper_1);
                return ResultVO.success("修改成功！");
            }else
                return ResultVO.error("修改出错！");

        }
    }

    @GetMapping("/getAddressById")
    public ResultVO getAddressById(Long id){
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getId,id);
        queryWrapper.eq(AddressBook::getIsDeleted,0);
        AddressBook addressBook = addressBookService.getOne(queryWrapper);

        return ResultVO.success("查询成功！",addressBook);
    }

    @GetMapping("/listByUserId")
    public ResultVO getAddressByUserId(Long userId){
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,userId);
        queryWrapper.eq(AddressBook::getIsDeleted,0);
        List<AddressBook> list = addressBookService.list(queryWrapper);

        return ResultVO.success("查询成功！",list);
    }

}
