package com.ithaorong.reggie.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithaorong.reggie.entity.Order;
import com.ithaorong.reggie.entity.OrderDetail;
import com.ithaorong.reggie.entity.ShoppingCart;
import com.ithaorong.reggie.entity.User;
import com.ithaorong.reggie.service.OrderDetailService;
import com.ithaorong.reggie.service.OrderService;
import com.ithaorong.reggie.service.ShoppingCartService;
import com.ithaorong.reggie.vo.ResultVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@CrossOrigin
@Slf4j
@Api(value = "提供订单相关接口",tags = "订单管理")
@RequestMapping("/order")
public class OrderController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private ShoppingCartService shoppingCartService;
    @Resource
    private OrderService orderService;
    @Resource
    private OrderDetailService orderDetailService;


    /**
     * 支付：payMethod（1微信 0支付宝）、amount（实际金额）
     * 备注：remark(非必填)
     * 地址的各个属性：phone、userName、address、consignee（收货人）
     */
    @PostMapping("/save")
    @Transactional
    public ResultVO save(@RequestHeader String token, @RequestBody Order order){
        synchronized (this){

            Map<String,String> map = new HashMap<>();

            Long userId;
            try {
                String s = stringRedisTemplate.boundValueOps(token).get();
                userId = objectMapper.readValue(s, User.class).getId();
            } catch (JsonProcessingException e) {
                return ResultVO.error("出现异常！");
            }
            //2.保存订单
            order.setId(0L);
            order.setUserId(userId);
            order.setCheckoutTime(null);
            order.setUpdateTime(LocalDateTime.now());
            order.setCreateTime(LocalDateTime.now());
            order.setStatus(1);

            //生成订单编号
            String orderId = UUID.randomUUID().toString().replace("-", "");
            order.setOrderId(orderId);

            //2.生成地址快照 -- 前端给
            orderService.save(order);

            //3.生成商品快照
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId,userId);
            List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

            for (ShoppingCart sc: list) {

                OrderDetail orderDetail = new OrderDetail();
                BeanUtils.copyProperties(sc,orderDetail);

                orderDetail.setOrderId(orderId);
                orderDetail.setId(0L);

                orderDetailService.save(orderDetail);
            }

            //5.删除购物车：当购物车中的记录购买成功之后，购物车中对应做删除操作
            LambdaQueryWrapper<ShoppingCart> scQueryWrapper = new LambdaQueryWrapper<>();
            scQueryWrapper.eq(ShoppingCart::getUserId,userId);
            boolean is_remove = shoppingCartService.remove(scQueryWrapper);

            //发送支付请求
            if (is_remove){
//                String orderId = orderInfo.get("orderId");
//                //设置当前订单信息
//                HashMap<String,String> data = new HashMap<>();
//                data.put("body",orderInfo.get("productNames"));  //商品描述
//                data.put("out_trade_no",orderId);               //使用当前用户订单的编号作为当前支付交易的交易号
//                data.put("fee_type","CNY");                     //支付币种
//                //data.put("total_fee",order.getActualAmount()*100+"");          //支付金额
//                data.put("total_fee","1");
//                data.put("trade_type","NATIVE");                //交易类型
//                data.put("notify_url","http://47.118.45.73:8080/pay/callback");           //设置支付完成时的回调方法接口
//
//                //发送请求，获取响应
//                //微信支付：申请支付连接
//                WXPay wxPay = new WXPay(new MyPayConfig());
//                Map<String, String> resp = wxPay.unifiedOrder(data);
//                orderInfo.put("payUrl",resp.get("code_url"));
                return ResultVO.success("保存订单成功！");
            }
            return ResultVO.error("保存订单失败！");
        }
    }
    /**
     *     @Autowired
     *     private OrderService orderService;
     *
     *     @PostMapping("/add")
     *     public ResultVO add(String cids, @RequestBody Orders order){
     *         ResultVO resultVO = null;
     *         try {
     *             Map<String, String> orderInfo = orderService.addOrder(cids, order);
     *             if(orderInfo!=null){
     *                 String orderId = orderInfo.get("orderId");
     *                 //设置当前订单信息
     *                 HashMap<String,String> data = new HashMap<>();
     *                 data.put("body",orderInfo.get("productNames"));  //商品描述
     *                 data.put("out_trade_no",orderId);               //使用当前用户订单的编号作为当前支付交易的交易号
     *                 data.put("fee_type","CNY");                     //支付币种
     *                 //data.put("total_fee",order.getActualAmount()*100+"");          //支付金额
     *                 data.put("total_fee","1");
     *                 data.put("trade_type","NATIVE");                //交易类型
     *                 data.put("notify_url","http://47.118.45.73:8080/pay/callback");           //设置支付完成时的回调方法接口
     *
     *                 //发送请求，获取响应
     *                 //微信支付：申请支付连接
     *                 WXPay wxPay = new WXPay(new MyPayConfig());
     *                 Map<String, String> resp = wxPay.unifiedOrder(data);
     *                 orderInfo.put("payUrl",resp.get("code_url"));
     *                 //orderInfo中包含：订单编号，购买的商品名称，支付链接
     *                 resultVO = new ResultVO(ResStatus.OK,"提交订单成功！",orderInfo);
     *             }else{
     *                 resultVO = new ResultVO(ResStatus.NO,"提交订单失败！",null);
     *             }
     *         } catch (SQLException e) {
     *             resultVO = new ResultVO(ResStatus.NO,"提交订单失败！",null);
     *         } catch (Exception e) {
     *             e.printStackTrace();
     *         }
     *         return resultVO;
     *     }
     *
     *     @GetMapping("/status/{oid}")
     *     public ResultVO getOrderStatus(@PathVariable("oid") String orderId,@RequestHeader("token")String token){
     *         ResultVO resultVO = orderService.getOrderById(orderId);
     *         return resultVO;
     *     }
     *
     *     @GetMapping("/list")
     *     @ApiOperation("订单查询接口")
     *     @ApiImplicitParams({
     *             @ApiImplicitParam(dataType = "string",name = "userId", value = "用户ID",required = true),
     *             @ApiImplicitParam(dataType = "string",name = "status", value = "订单状态",required = false),
     *             @ApiImplicitParam(dataType = "int",name = "pageNum", value = "页码",required = true),
     *             @ApiImplicitParam(dataType = "int",name = "limit", value = "每页条数",required = true)
     *     })
     *     public ResultVO list(@RequestHeader("token")String token,
     *                          String userId,String status,int pageNum,int limit){
     *         ResultVO resultVO = orderService.listOrders(userId, status, pageNum, limit);
     *         return resultVO;
     *     }
     */


    /**
     *    @Transactional
     *     public Map<String,String> addOrder(String cids,Orders order) throws SQLException {
     *         logger.info("add order begin...");
     *         Map<String,String> map = new HashMap<>();
     *
     *         //1.校验库存：根据cids查询当前订单中关联的购物车记录详情（包括库存）
     *         String[] arr = cids.split(",");
     *         List<Integer> cidsList = new ArrayList<>();
     *         for (int i = 0; i <arr.length ; i++) {
     *             cidsList.add(Integer.parseInt(arr[i]));
     *         }
     *         List<ShoppingCartVO> list = shoppingCartMapper.selectShopcartByCids(cidsList);
     *
     *         boolean f = true;
     *         String untitled = "";
     *         for (ShoppingCartVO sc: list) {
     *             if(Integer.parseInt(sc.getCartNum()) > sc.getSkuStock()){
     *                 f = false;
     *             }
     *             //获取所有商品名称，以,分割拼接成字符串
     *             untitled = untitled+sc.getProductName()+",";
     *         }
     *
     *         if(f){
     * //            System.out.println("-----库存校验完成");
     *             logger.info("product stock is OK...");
     *             //2.保存订单
     *             order.setUntitled(untitled);
     *             order.setCreateTime(new Date());
     *             order.setStatus("1");
     *             //生成订单编号
     *             String orderId = UUID.randomUUID().toString().replace("-", "");
     *             order.setOrderId(orderId);
     *             int i = ordersMapper.insert(order);
     *
     *             //3.生成商品快照
     *             for (ShoppingCartVO sc: list) {
     *                 int cnum = Integer.parseInt(sc.getCartNum());
     *                 String itemId = System.currentTimeMillis()+""+ (new Random().nextInt(89999)+10000);
     *                 OrderItem orderItem = new OrderItem(itemId, orderId, sc.getProductId(), sc.getProductName(), sc.getProductImg(), sc.getSkuId(), sc.getSkuName(), new BigDecimal(sc.getSellPrice()), cnum, new BigDecimal(sc.getSellPrice() * cnum), new Date(), new Date(), 0);
     *                 orderItemMapper.insert(orderItem);
     *                 //增加商品销量
     *             }
     *
     *             //4.扣减库存：根据套餐ID修改套餐库存量
     *             for (ShoppingCartVO sc: list) {
     *                 String skuId = sc.getSkuId();
     *                 int newStock = sc.getSkuStock()- Integer.parseInt(sc.getCartNum());
     *
     *                 ProductSku productSku = new ProductSku();
     *                 productSku.setSkuId(skuId);
     *                 productSku.setStock(newStock);
     *                 productSkuMapper.updateByPrimaryKeySelective(productSku);
     *             }
     *
     *             //5.删除购物车：当购物车中的记录购买成功之后，购物车中对应做删除操作
     *             for (int cid: cidsList) {
     *                 shoppingCartMapper.deleteByPrimaryKey(cid);
     *             }
     *             logger.info("add order finished...");
     *             map.put("orderId",orderId);
     *             map.put("productNames",untitled);
     *             return map;
     *         }else{
     *             //表示库存不足
     *             return null;
     *         }
     *     }
     *
     *     @Override
     *     public int updateOrderStatus(String orderId, String status) {
     *         Orders orders = new Orders();
     *         orders.setOrderId(orderId);
     *         orders.setStatus(status);
     *         int i = ordersMapper.updateByPrimaryKeySelective(orders);
     *         return i;
     *     }
     *
     *     @Override
     *     public ResultVO getOrderById(String orderId) {
     *         Orders order = ordersMapper.selectByPrimaryKey(orderId);
     *         return new ResultVO(ResStatus.OK,"sucesss",order.getStatus());
     *     }
     *
     *     @Override
     *     @Transactional(isolation = Isolation.SERIALIZABLE)
     *     public void closeOrder(String orderId) {
     *         synchronized (this) {
     *             //  1.修改当前订单：status=6 已关闭  close_type=1 超时未支付
     *             Orders cancleOrder = new Orders();
     *             cancleOrder.setOrderId(orderId);
     *             cancleOrder.setStatus("6");  //已关闭
     *             cancleOrder.setCloseType(1); //关闭类型：超时未支付
     *             ordersMapper.updateByPrimaryKeySelective(cancleOrder);
     *
     *             //  2.还原库存：先根据当前订单编号查询商品快照（skuid  buy_count）--->修改product_sku
     *             Example example1 = new Example(OrderItem.class);
     *             Example.Criteria criteria1 = example1.createCriteria();
     *             criteria1.andEqualTo("orderId", orderId);
     *             List<OrderItem> orderItems = orderItemMapper.selectByExample(example1);
     *             //还原库存
     *             for (int j = 0; j < orderItems.size(); j++) {
     *                 OrderItem orderItem = orderItems.get(j);
     *                 //修改
     *                 ProductSku productSku = productSkuMapper.selectByPrimaryKey(orderItem.getSkuId());
     *                 productSku.setStock(productSku.getStock() + orderItem.getBuyCounts());
     *                 productSkuMapper.updateByPrimaryKey(productSku);
     *             }
     *         }
     *     }
     *
     *     @Override
     *     public ResultVO listOrders(String userId, String status, int pageNum, int limit) {
     *         //1.分页查询
     *         int start = (pageNum-1)*limit;
     *         List<OrdersVO> ordersVOS = ordersMapper.selectOrders(userId, status, start, limit);
     *
     *         //2.查询总记录数
     *         Example example = new Example(Orders.class);
     *         Example.Criteria criteria = example.createCriteria();
     *         criteria.andLike("userId",userId);
     *         if(status != null && !"".equals(status)){
     *             criteria.andLike("status",status);
     *         }
     *         int count = ordersMapper.selectCountByExample(example);
     *
     *         //3.计算总页数
     *         int pageCount = count%limit==0?count/limit:count/limit+1;
     *
     *         //4.封装数据
     *         PageHelper<OrdersVO> pageHelper = new PageHelper<>(count, pageCount, ordersVOS);;
     *         return new ResultVO(ResStatus.OK,"SUCCESS",pageHelper);
     *     }
     */
}
