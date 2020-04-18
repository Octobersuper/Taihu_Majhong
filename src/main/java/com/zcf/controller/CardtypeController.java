package com.zcf.controller;


import com.alipay.api.domain.Car;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.zcf.mahjong.json.Body;
import com.zcf.mahjong.mapper.CardtypeMapper;
import com.zcf.mahjong.util.LayuiJson;
import com.zcf.pojo.Cardtype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ZhaoQi
 * @since 2020-04-15
 */
@RestController
@CrossOrigin
@RequestMapping("/cardtype")
public class CardtypeController {

    @Autowired
    CardtypeMapper cm;


    @GetMapping("get")
    public LayuiJson get(@RequestParam(value = "pageNum") int pageNum,
                           @RequestParam(value = "pageSize") int pageSize){
        EntityWrapper<Cardtype> ew = new EntityWrapper<>();
        ew.orderBy("id",false);
        List<Cardtype> list = cm.selectPage(new Page<Cardtype>(pageNum, pageSize), ew);
        LayuiJson ls = new LayuiJson();
        if (list == null) {
            ls.setCode(1);
            ls.setCount(0);
            ls.setMsg("无数据");
            ls.setData(null);
        }else{
            ls.setCode(0);
            ls.setCount(cm.selectList(null).size());
            ls.setMsg("OK");
            ls.setData(list);
        }
        return ls;
    }

    @PostMapping("update")
    public Body update(Cardtype  cardtype){
        boolean b = cardtype.updateById();
        if(b){
            return Body.BODY_200;
        }
        return Body.BODY_451;
    }

    @PostMapping("delete")
    public Body delete(Cardtype  cardtype){
        boolean b = cardtype.deleteById();
        if(b){
            return Body.BODY_200;
        }
        return Body.BODY_451;
    }

    @PostMapping("insert")
    public Body insert(Cardtype  cardtype){
        boolean b = cardtype.insert();
        if(b){
            return Body.BODY_200;
        }
        return Body.BODY_451;
    }
}

