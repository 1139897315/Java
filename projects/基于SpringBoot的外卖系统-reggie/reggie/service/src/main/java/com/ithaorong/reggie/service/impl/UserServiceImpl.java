package com.ithaorong.reggie.service.impl;

import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.config.WXAuth;
import com.ithaorong.reggie.dao.UserMapper;
import com.ithaorong.reggie.entity.User;
import com.ithaorong.reggie.entity.UserInfo;
import com.ithaorong.reggie.service.UserService;
import com.ithaorong.reggie.service.WXService;
import com.ithaorong.reggie.utils.WXPayConstants;
import com.ithaorong.reggie.vo.ResultVO;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private WXService wxService;
    @Value("${jwt.secret}")
    private String secrete;

    public ResultVO getSessionId(String code) {
        //校验接口，获取返回结果（通过code去获取该用户唯一凭证信息）
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid={0}&secret={1}&js_code={2}&grant_type=authorization_code";
        url = url.replace("{0}",WXPayConstants.APP_ID).replace("{1}", WXPayConstants.SECRET).replace("{2}",code);

        String res = HttpUtil.get(url);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(uuid,res,30, TimeUnit.MINUTES);

        HashMap<String, String> map = new HashMap<>();
        map.put("sessionId",uuid);
        return ResultVO.success("获取SessionId成功！",map);
    }

    //登录
    public ResultVO authLogin(WXAuth wxAuth) {
        //通过wxAuth的值，进行解密
        try {
            UserInfo userInfo = wxService.wxDecrypt(wxAuth.getEncryptedData(), wxAuth.getSessionId(), wxAuth.getIv());
            if (userInfo != null) {
                String openId = userInfo.getOpenId();
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                User userExist = userMapper.selectOne(queryWrapper.eq(User::getOpenId, openId).last("limit 1"));

                //解析用户信息并拷贝到user对象
                User user = new User();
                user.from(userInfo);
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());

                if (userExist == null){
                    //注册
                    synchronized (this){
                        //如何数据存储在数据库和redis保持事务性
                        userMapper.insert(user);
                        return this.generateToken(user);
                    }
                }else {
                    //登录：从数据库获取user数据
                    LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    userLambdaQueryWrapper.eq(User::getOpenId,user.getOpenId());
                    user = this.getOne(userLambdaQueryWrapper);

                    return this.generateToken(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResultVO.error("验证失败！");
    }


    private ResultVO generateToken(User user){
        //生成Token
        String token = Jwts.builder()                                //主题，就是token中携带的数据
                .setIssuedAt(new Date())                            //设置token的生成时间
                .setExpiration(new Date(System.currentTimeMillis() + 7*24*60*60*1000)) //设置token过期时间
                .signWith(SignatureAlgorithm.HS256, secrete)     //设置加密方式和加密密码
                .compact();

        //当用户登录成功之后，将token存入redis
        try {
            String userInfo = objectMapper.writeValueAsString(user);
            stringRedisTemplate.boundValueOps(token).set(userInfo, 6, TimeUnit.HOURS);

            //这两个值不能给前端展示
            user.setOpenId(null);
            user.setUnionId(null);

            HashMap<String,Object> map = new HashMap<>();
            map.put("user",user);
            map.put("token",token);

            return ResultVO.success("登录成功！",map);
        } catch (JsonProcessingException e) {
            return ResultVO.error("出现异常！");
        }
    }
}
