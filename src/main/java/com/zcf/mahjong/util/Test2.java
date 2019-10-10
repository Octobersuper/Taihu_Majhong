package com.zcf.mahjong.util;

public class Test2 extends Thread {
	private BaseDao baseDao = null;
	public Test2(BaseDao baseDao){
		this.baseDao=baseDao;
	}
	@Override
	public void run() {
		while(true){
			int index = (int)(Math.random() * 100);
			index=10;
			String sql = "update game_usertable set openid=? where userid=1";
			baseDao.executeUpdate(sql, new Object[]{index});
			if(index%10==0){
				break;
			}
			baseDao.CloseAll();
			int time = (int)(Math.random() * 5)+1;
			try {
				Thread.sleep(time*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
}
