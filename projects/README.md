## 商城管理系统：

### 基于SpringBoot的商城管理系统：

- 项目描述：作为毕业设计，从零到一实现支持商品上下架和交易的B2C商城管理系统。 

- 技术描述：该项目是前后端分离项目，前端使用VUE、ElementUI、LayUI、Cookie、 JQuery，后端使用SpringBoot、MyBatis、tkMapper、Swagger，数据库使用MySql 

- 收获：通过这个项目，对前后端分离开发、JWT权限校验、内网穿透、微信支付业务、订单超时、用Linux项目部署上线等有了进一步的理解



### 基于SpringCloud的商城管理系统：

- 项目描述：由前一个SpringBoot版本升级为分布式-微服务版本。

- 技术描述：该项目是微服务架构，前端使用VUE、ElementUI、LayUI、Cookie、 JQuery，后端使用SpringBoot、SpringCloud、MyBatis、tkMapper、Swagger、 SpringSecurity，数据库使用Redis、MySql 

- 收获：通过这个项目，对项目的微服务框架各个组件的使用、JWT权限校验、消息中间件、Docker上线部 署、分布式缓存、分布式锁、分布式事务、内网穿透、微信支付业务、订单超时等有了进一步的理解 

- 特点：
  - 使用JWT生成Token和用户信息存入Redis实现共享Session，后端用户只需拿Token即可 获取获取用户信息 
  - 使用RabbitMQ解决流量削峰问题和订单超时问题。 
  - 使用Redisson框架（原使用redis+lua脚本）解决分布式订单超卖问题