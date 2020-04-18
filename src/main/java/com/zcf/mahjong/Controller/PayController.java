package com.zcf.mahjong.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.zcf.mahjong.bean.Order;
import com.zcf.mahjong.dao.M_GameDao;
import com.zcf.mahjong.json.Body;
import com.zcf.mahjong.mapper.OrderMapper;
import com.zcf.mahjong.util.BaseDao;
import com.zcf.mahjong.util.HttpUtils;
import com.zcf.mahjong.util.SignUtil;
import com.zcf.mahjong.util.md5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.zcf.mahjong.util.PayUtil.generateOrderId;


@Controller
@CrossOrigin
@RequestMapping("/pays")
public class PayController {

    @Autowired
    OrderMapper om;
    private M_GameDao gameDao = new M_GameDao(new BaseDao());

    @GetMapping("/pay")
    @ResponseBody
    public JSONObject pay(String Money, Integer id, String Num, int type, HttpServletResponse response) throws IOException, NoSuchAlgorithmException {
        String pay_orderid = generateOrderId();//20位订单号 时间戳+6位随机字符串组成
        if (!Money.contains(".")){
            Money = Money+".00";
            //Money = "0.01";
        }
        //插入订单记录
        Order order = new Order();
        order.setCreateTime(new Date());
        order.setType(type);
        order.setOrderNumber(pay_orderid);
        order.setNum(Integer.valueOf(Num));
        order.setPaymentType(0);
        order.setPrice(Money);
        order.setState(0);
        order.setUid(id);
        om.insert(order);

        String msg = "www.baidu.com";
        String callback = "http://103.193.175.59:8080/Taihu_Majhong/pays/notifyPay";//回调地址
        String uid = String.valueOf(id);
        String pay_type = type == 1 ? "wechat" : "alipay";

        HashMap<String, String> map = new HashMap<>();
        map.put("return_type", "app");
        map.put("appid", "1036840");
        map.put("pay_type", pay_type);
        map.put("amount", Money);
        map.put("callback_url", callback);
        map.put("success_url", msg);
        map.put("error_url", msg);
        map.put("out_uid", uid);
        map.put("out_trade_no", pay_orderid);
        map.put("version", "v1.1");
        String sign1 = SignUtil.getSign(map);
        map.put("sign", sign1);


        String s = HttpUtils.sendPost("http://api.cs96.cn/index/unifiedorder?format=json", map);
        JSONObject jsonObject = JSONObject.parseObject(s);
        return jsonObject;
    }

    @RequestMapping("/notifyPay")
    public void notifyPay(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("进入回调");
        String callbacks = request.getParameter("callbacks");
        String appid = request.getParameter("appid");
        String orderid = request.getParameter("out_trade_no");
        String amount = request.getParameter("amount");
        if (callbacks.equals("CODE_SUCCESS")) {
            //支付成功，写返回数据逻辑
            Order order = new Order();
            order.setOrderNumber(orderid);
            order = om.selectOne(order);
            if (order.getState() == 0) {
                Integer num = order.getNum();
                gameDao.Adddiamond(order.getUid(),Integer.valueOf(num));
                order.setState(1);
                om.updateById(order);
                PrintWriter pw = response.getWriter();
                pw.write("success");
            } else {
                PrintWriter pw = response.getWriter();
                pw.write("success");
            }
        } else {
            PrintWriter pw = response.getWriter();
            pw.write("支付失败");
        }
    }
}
