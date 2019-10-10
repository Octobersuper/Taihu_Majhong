package com.zcf.mahjong.service;

import java.util.HashMap;
import java.util.Map;

import com.zcf.mahjong.bean.Service;
import com.zcf.mahjong.json.Body;
import com.zcf.mahjong.mapper.ServiceMapper;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service
public class ServiceService {
	
	@Autowired
	private ServiceMapper am;

	public Service selectone() {
		// TODO Auto-generated method stub
		return am.selectByPrimaryKey((long)1);
	}

	public Body update(Service a) {
		a.setId((long)1);
		// TODO Auto-generated method stub
		int i = am.updateByPrimaryKeySelective(a);
		if (i!=0) {
			return Body.BODY_200;
		}
		return Body.BODY_451;
	}

}
