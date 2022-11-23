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
import java.util.HashMap;
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
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId,cart.getUserId())
                        .eq(ShoppingCart::getIsDeleted,0);
            List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
            if (list != null){
                boolean change = false;
                for (ShoppingCart s : list) {
                    if (cart.getName().equals(s.getName()) && cart.getDishFlavor().equals(s.getDishFlavor())){
                        LambdaUpdateWrapper<ShoppingCart> updateWrapper = new LambdaUpdateWrapper<>();
                        updateWrapper.eq(ShoppingCart::getId,s.getId());
                        updateWrapper.set(ShoppingCart::getNumber,s.getNumber()+1);
                        shoppingCartService.update(updateWrapper);
                        change = true;
                    }
                }
                if (!change){
                    cart.setId(0L);
                    cart.setIsDeleted(0);
                    cart.setCreateTime(LocalDateTime.now());
                    cart.setUserId(cart.getUserId());
                    shoppingCartService.save(cart);
                }
            }
            return ResultVO.success("添加购物车成功！");
        }
    }

    @GetMapping("/listByUserId")
    @Transactional(propagation = Propagation.SUPPORTS)
    public ResultVO listShoppingCartsByUserId(Long userId) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId)
                    .eq(ShoppingCart::getIsDeleted,0)
                    .gt(ShoppingCart::getNumber,0);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        int allNum = 0;
        for (ShoppingCart shoppingCart: list){
            allNum += shoppingCart.getNumber();
        }
        HashMap<String ,Object> map = new HashMap<>();
        map.put("list",list);
        map.put("allNum",allNum);
        return ResultVO.success("查询成功！",map);
    }

    /**
     * 修改购物车数量接口
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
     * @return
     */
    @DeleteMapping("/delete")
    public ResultVO delete(@RequestParam("ids") List<Long> ids){
        synchronized (this){
            //查看套餐状态，确定是否可以删除
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(ShoppingCart::getId,ids);
            queryWrapper.le(ShoppingCart::getNumber,0);

            int count = shoppingCartService.count(queryWrapper);
            if (count > 0){
                //如果不能删除，返回失败
                return ResultVO.error("存在商品数量小于或等于0");
            }
            //如果可以删除，先删除套餐表中数据
            shoppingCartService.removeByIds(ids);

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
