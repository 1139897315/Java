package com.ithaorong.reggie.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CheckTokenInterceptor implements HandlerInterceptor {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //跳过OPTIONS请求
        String method = request.getMethod();
        log.info(method);
        if("OPTIONS".equalsIgnoreCase(method)){
            return true;
        }
        //获取token
        String token = request.getHeader("token");
        //判断是否携带token
        if(token == null){
            doResponse(response,ResultVO.error("请先登录！"));
        }else{
            //判断token是否在token中过期
            String s = stringRedisTemplate.boundValueOps(token).get();
            if(s == null){
                doResponse(response,ResultVO.error("请先登录！"));
            }else{
                stringRedisTemplate.boundValueOps(token).expire(30, TimeUnit.MINUTES);
                return true;
            }
            return true;
            /**
             try {
             JwtParser parser = Jwts.parser();
             Jws<Claims> claimsJws = parser.parseClaimsJws(token);
             return true;
             }catch (ExpiredJwtException e){
             ResultVO resultVO = new ResultVO(ResStatus.LOGIN_FAIL_OVERDUE, "登录过期，请重新登录！", null);
             doResponse(response,resultVO);
             }catch (UnsupportedJwtException e){
             ResultVO resultVO = new ResultVO(ResStatus.LOGIN_FAIL_NOT, "Token不合法，请自重！", null);
             doResponse(response,resultVO);
             }catch (Exception e){
             ResultVO resultVO = new ResultVO(ResStatus.LOGIN_FAIL_NOT, "请先登录！", null);
             doResponse(response,resultVO);
             }
             **/
        }
        return false;
    }

    private void doResponse(HttpServletResponse response,ResultVO resultVO) throws IOException {
        response.reset();
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
//        ServletOutputStream out = response.getOutputStream();
        PrintWriter out = response.getWriter();
        String s = new ObjectMapper().writeValueAsString(resultVO);
        out.print(s);
        out.flush();
        out.close();
    }
}
