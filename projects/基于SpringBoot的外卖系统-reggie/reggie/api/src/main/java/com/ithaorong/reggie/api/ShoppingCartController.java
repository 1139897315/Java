package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.config.RedisKey;
import com.ithaorong.reggie.entity.*;
import com.ithaorong.reggie.service.ShoppingCartService;
import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
@Slf4j
@Api(value = "提供购物车相关接口",tags = "购物车管理")
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Resource
    private ShoppingCartService shoppingCartService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @PostMapping("/add")
    public ResultVO addShoppingCart(@RequestHeader String token, @RequestBody ShoppingCart cart) {
        synchronized (this){
            Long userId;
            try {
                String s = stringRedisTemplate.boundValueOps(token).get();
                userId = objectMapper.readValue(s, User.class).getId();
            } catch (JsonProcessingException e) {
                return ResultVO.error("出现异常！");
            }

            cart.setId(0L);
            cart.setIsDeleted(0);
            cart.setCreateTime(LocalDateTime.now());
            cart.setUserId(userId);

            shoppingCartService.save(cart);
            return ResultVO.success("添加购物车成功！");
        }
    }

    @GetMapping("/listByUserId")
    @Transactional(propagation = Propagation.SUPPORTS)
    public ResultVO listShoppingCartsByUserId(int userId) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getId,userId)
                    .eq(ShoppingCart::getIsDeleted,0);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return ResultVO.success("查询成功！",list);
    }

    /**
     * 修改购物车数量接口
     * @param shoppingCart.cartId
     * @param shoppingCart.cartNumber
     * @return
     */
    @PutMapping("/updateNum")
    public ResultVO updateCartNum(@RequestBody ShoppingCart shoppingCart) {
        synchronized (this){
            //条件
            LambdaUpdateWrapper<ShoppingCart> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ShoppingCart::getId,shoppingCart.getId());
            updateWrapper.set(ShoppingCart::getNumber,shoppingCart.getNumber());

            shoppingCartService.update(updateWrapper);
            return ResultVO.success("修改成功！");
        }
    }

    /**
     * 如果数量小于或等于0，则删除该购物车商品
     * @param cartId
     * @return
     */
    @DeleteMapping("/delete")
    public ResultVO delete(Long cartId){
        synchronized (this){
            LambdaUpdateWrapper<ShoppingCart> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ShoppingCart::getId,cartId);
            updateWrapper.set(ShoppingCart::getIsDeleted,1);
            return ResultVO.success("删除成功！");
        }
    }
//    @GetMapping("/listByIds/{cids}")
//    public ResultVO listShoppingCartsByCids(String cids) {
//        //使用tkmapper只能查询到某张表中拥有的字段，因此没法查询到商品名称、图片、单价等信息
//        //获取String类型的cids
//        String[] arr = cids.split(",");
//        List<Integer> cartIds = new ArrayList<>();
//        for (int i=0; i<arr.length; i++){
//            cartIds.add(Integer.parseInt(arr[i]));
//        }
//        List<ShoppingCart> list = shoppingCartService.
//                selectShopcartByCids(cartIds);
//
//        return ResultVO.success("success", list);
//    }
}
