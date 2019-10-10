package com.zcf.mahjong.service;


import com.zcf.mahjong.bean.RoomBean;
import com.zcf.mahjong.bean.UserBean;
import com.zcf.mahjong.mahjong.Public_State;

public class Exit_Game extends Thread {
	private RoomBean roomBean;
	private int time;
	public Exit_Game(RoomBean roomBean){
		this.roomBean=roomBean;
		this.time= Public_State.exit_time;
	}
	@Override
	public void run() {
		while(true){
			if(this.time==0){
				//解散房间
				for(UserBean userBean:roomBean.getGame_userlist()){
					Mahjong_Socket socket = (Mahjong_Socket) Public_State.clients.get(userBean.getOpenid());
					if(socket!=null){
						socket.Exit_All();
						break;
					}
				}
			}
			//已经解散或房间取消解散
			if(roomBean==null||roomBean.getExit_game()==0){
				break;
			}
			try {
				Thread.sleep(1000);
				this.time--;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}