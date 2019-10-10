package com.zcf.mahjong.mahjong;

import com.zcf.mahjong.bean.RoomBean;

public class Establish_PK_Money {
	/**
	 * 创建房间
	 * 
	 */
	public static synchronized RoomBean Establish(int roomtype, int clubid) {
		String roomNo = "";
		while(true){
			for(int i=0;i<6;i++){
				roomNo+=(int)(Math.random() * 10);
			}
			if(Public_State.PKMap.get(roomNo) == null){
				RoomBean roomBean = new RoomBean();
				// 放入房间号
				roomBean.setRoomno(roomNo);
				//茶馆创建
				if(clubid!=0){
					roomBean.setClubid(clubid);
				}
				// 将房间实例放入房间map
				Public_State.PKMap.put(roomNo,roomBean);
				return roomBean;
			}
		}
	}
	
}
