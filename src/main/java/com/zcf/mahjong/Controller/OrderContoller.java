package com.zcf.mahjong.Controller;

import com.zcf.mahjong.bean.Order;
import com.zcf.mahjong.json.Body;
import com.zcf.mahjong.service.OrderServiceImpl;
import com.zcf.mahjong.util.LayuiJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@CrossOrigin
@Controller
@ResponseBody
@RequestMapping("/order/")
public class OrderContoller {

	@Autowired
	private OrderServiceImpl os;
	
	
	/**
	 * 查询订单
	 * @param o
	 * @return
	 */
	@PostMapping("getOrder")
	public Body getOrder(Order o){
		return os.getOrder(o);
	}
	
	@PostMapping("/selectAll")
    @ResponseBody
    public LayuiJson selectAll(@RequestParam(value = "pageNum") int pageNum,
							   @RequestParam(value = "pageSize") int pageSize){
        return os.selectAll(pageNum,pageSize);
    }
	
	@PostMapping("/delete")
	@ResponseBody
	public Body delete(Long id){
		return os.delete(id);
	}
	
	
}
