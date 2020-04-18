package com.zcf.mahjong.dao;

import com.zcf.mahjong.util.BaseDao;

import java.sql.SQLException;

public class ClubDao {
	private BaseDao baseDao;

	public ClubDao(BaseDao baseDao) {
		this.baseDao = baseDao;
	}
	/**
	 * 查询茶馆钻石数量
	 * @param clubid
	 * @return
	 */
	public int getClub_diamond(int clubid){
		String sql = "select diamond from game_card_circle where circlenumber=?";
		baseDao.executeAll(sql, new Object[]{clubid});
		try {
			if(baseDao.resultSet.next()){
				return baseDao.resultSet.getInt("diamond");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	/**
	 * 修改俱乐部钻石
	 * @param clubid
	 * @param diamond
	 * @param type
	 * @return
	 */
	public int Update_Club_Money(int clubid,int diamond,int type){
		//type:0 游戏减少钻石    1增加钻石
		String ss = "insert into diamond_record values(?,NOW(),?,?,?)";
		String content = type==0?"房卡扣除":"房卡充值";
		baseDao.executeUpdate(ss,
				new Object[] {null,diamond,clubid,content});

		String sql = "update game_card_circle set diamond=diamond"+(type==0?"-":"+")+"? where circlenumber=?";
		String state = baseDao.executeUpdate(sql, new Object[]{diamond,clubid});
		return state.equals("success")?0:-1;
	}
}
