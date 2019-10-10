package com.zcf.mahjong.Controller;

import com.zcf.mahjong.bean.Service;
import com.zcf.mahjong.json.Body;
import com.zcf.mahjong.mapper.ServiceMapper;
import com.zcf.mahjong.service.FileService;
import com.zcf.mahjong.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * Created with IDEA
 * author:ZhaoQ
 * className:
 * Date:2019/8/17
 * Time:16:17
 */
@CrossOrigin
@Controller
@RequestMapping("service")
@ResponseBody
public class ServiceController {

    @Autowired
    ServiceService as;
    @Autowired
    FileService iFileService;
    /**
     * 修改轮播图信息
     * @param a
     * @param file
     * @param request
     * @return
     */
    @RequestMapping(value="update",method= RequestMethod.POST)
    public Body update(Service a, @RequestParam(value = "uploadFile",required = false) MultipartFile file, HttpServletRequest request){

        if(file!=null){
            String path = request.getSession().getServletContext().getRealPath("serviceimg");
            String targetFileName = iFileService.upload(file,path);

            a.setImg("/serviceimg/"+targetFileName);
        }

        return as.update(a);
    }

    @GetMapping("getone")
    public Body getone(){
        Service service = as.selectone();
        return Body.newInstance(service);
    }
}
