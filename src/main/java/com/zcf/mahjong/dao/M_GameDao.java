package com.zcf.mahjong.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zcf.mahjong.bean.RoomBean;
import com.zcf.mahjong.bean.UserBean;
import com.zcf.mahjong.mahjong.Public_State;
import com.zcf.mahjong.util.BaseDao;
import com.zcf.mahjong.util.UtilClass;
import com.zcf.mahjong.util.AES;

public class M_GameDao {
	private BaseDao baseDao;

	public M_GameDao(BaseDao baseDao) {
		this.baseDao = baseDao;
	}

	/**
	 * 扣除用户金币
	 * 
	 * @param userid
	 * @param money
	 * @return
	 */
	public String UpdateUserMoney(int userid, int money, int type) {
		String sql = "update user_table set money=? where userid=?";
		return baseDao.executeUpdate(sql, new Object[] { money, userid });
	}
	/**
	 * 增加用户金币
	 *
	 * @param userid
	 * @param diamond
	 * @return
	 */
	public String Adddiamond(int userid, int diamond) {
		String sql = "update user_table set diamond=diamond+? where userid=?";
		return baseDao.executeUpdate(sql, new Object[] { diamond, userid });
	}
	/**
	 * 扣除用户金币
	 * 
	 * @param userid
	 * @return
	 */
	public String UpdateUserDiamond(int userid, int diamond, int type) {
		String sql = "update user_table set diamond=diamond" + (type == 0 ? '-' : '+') + "? where userid=?";
		return baseDao.executeUpdate(sql, new Object[] { diamond, userid });
	}
	/**
	 * 查询用户钻石
	 * 
	 * @param userid
	 * @return
	 */
	public int getUserDiamond(int userid) {
		String sql = "select diamond from user_table where userid=?";
		baseDao.executeAll(sql, new Object[] { userid });
		try {
			if (baseDao.resultSet.next()) {
				return baseDao.resultSet.getInt("diamond");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 绑定邀请码
	 * 
	 * @param userid
	 * @param code
	 * @return
	 */
	/*public int bindinguser(int userid, int code) {
		// 首先判断 邀请码是否存在 或者是否填自己
		boolean b = selectcode(userid, code);
		if (!b) {
			return 101;// 邀请码不存在
		}
		boolean c = selfcode(userid, code);
		if (!c) {
			return 102;// 不可填自己
		}
		// 首先根据userid判断pid是否为空
		String sql = "SELECT * from  user_table where  userid=?";
		baseDao.executeAll(sql, new Object[] { userid });
		try {
			if (baseDao.resultSet.next()) {
				int pid = baseDao.resultSet.getInt("pid");
				if (pid == 0) {
					int userids = getupuser(code);
					Object success = bduser(userid, userids);
					if ("success".equals(success)) {
						return 0;// 绑定成功
					}
				} else {
					return 103;// 请勿重复绑定
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}*/

	/**
	 * code是否存在
	 * 
	 * @param userid
	 * @param code
	 * @return
	 */
	private boolean selectcode(int userid, int code) {
		// code是否存在
		String sql = "select * from user_table where code=?";
		baseDao.executeAll(sql, new Object[] { code });
		try {
			return baseDao.resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 判断是否是自己
	 * 
	 * @param userid
	 * @param code
	 */
	private boolean selfcode(int userid, int code) {
		String sql = "select * from user_table where userid=? and code=?";
		baseDao.executeAll(sql, new Object[] { userid, code });
		try {
			if (baseDao.resultSet.next()) {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 *	获取邀请人 
	 * @param code
	 * @return
	 */
	private int getupuser(int code) {
		String sql = "SELECT * from  user_table where code=?";
		baseDao.executeAll(sql, new Object[] { code });
		try {
			if (baseDao.resultSet.next()) {
				int userids = baseDao.resultSet.getInt("userid");
				return userids;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	/**
	 * 绑订用户
	 * @param userid
	 * @param userids
	 * @return
	 */
	private Object bduser(int userid, int userids) {
		int money = Integer.parseInt(UtilClass.utilClass.getTableName("/parameter.properties", "award_money"));
		String sql = "update  user_table SET pid=?,money=money+? where userid=?";
		return baseDao.executeUpdate(sql, new Object[] { userids, money, userid });
	}

	/**
	 * 查看个人信息
	 * 
	 * @param userid
	 * @return
	 */
	public Object getuser(int userid) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String sql = "select * from user_table where userid=?";
		String executeAll = baseDao.executeAll(sql, new Object[] { userid });
		try {
			while (baseDao.resultSet.next() && "success".equals(executeAll)) {
				Map<String, Object> map = UtilClass.utilClass.getSqlMap("/sql.properties", baseDao.resultSet,
						"sql_getUserList");
				list.add(map);
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 查看公告
	 * 
	 * @return
	 */
	public Object getnotice() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String sql = "select * from game_notice";
		String executeAll = baseDao.executeAll(sql, new Object[] {});
		try {
			while (baseDao.resultSet.next() && "success".equals(executeAll)) {
				Map<String, Object> map = UtilClass.utilClass.getSqlMap("/sql.properties", baseDao.resultSet,
						"sql_getnotice");
				list.add(map);
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 查看广播
	 * 
	 * @return
	 */
	public Object getbroadcast() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String sql = "select * from game_broadcast";
		String executeAll = baseDao.executeAll(sql, new Object[] {});
		try {
			while (baseDao.resultSet.next() && "success".equals(executeAll)) {
				Map<String, Object> map = UtilClass.utilClass.getSqlMap("/sql.properties", baseDao.resultSet,
						"sql_getBroadcast");
				list.add(map);
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 个人充值记录
	 * 
	 * @param userid
	 * @return
	 */
	public Object getrecord(int userid) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String sql = "select * from game_record where uid=?";
		String executeAll = baseDao.executeAll(sql, new Object[] { userid });
		try {
			while (baseDao.resultSet.next() && "success".equals(executeAll)) {
				Map<String, Object> map = UtilClass.utilClass.getSqlMap("/sql.properties", baseDao.resultSet,
						"sql_getrecord");
				list.add(map);
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 查看软件介绍
	 * 
	 * @return
	 */
	public Object getexplain() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String sql = "select * from game_explain";
		String executeAll = baseDao.executeAll(sql, new Object[] {});
		try {
			while (baseDao.resultSet.next() && "success".equals(executeAll)) {
				Map<String, Object> map = UtilClass.utilClass.getSqlMap("/sql.properties", baseDao.resultSet,
						"sql_getexplain");
				list.add(map);
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 添加俱乐部圈主金币
	 * @param clubno
	 * @param money
	 */
	public String addClub_Money(int clubid,int money){
		String sql = "update user_table set money=money+? where userid in (select user_id from game_card_circle where id=?)";
		return baseDao.executeUpdate(sql, new Object[]{money,clubid});
	}
	/**
	 * 查看游戏内语音文件
	 * 
	 * @return
	 */
	public Object getvoice() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String sql = "select * from game_voice";
		String executeAll = baseDao.executeAll(sql, new Object[] {});
		try {
			while (baseDao.resultSet.next() && "success".equals(executeAll)) {
				Map<String, Object> map = UtilClass.utilClass.getSqlMap("/sql.properties", baseDao.resultSet,
						"sql_getVoiceList");
				list.add(map);
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 获取配置信息
	 * @return
	 */
	public void getConfig(){
		String sql = "select * from config_table limit 0,1";
		baseDao.executeAll(sql, null);
		try {
			if(baseDao.resultSet.next()){
				Public_State.exit_time=baseDao.resultSet.getInt("exit_time");
				Public_State.establish_two=baseDao.resultSet.getString("establish_two").split("-");
				Public_State.establish_three=baseDao.resultSet.getString("establish_three").split("-");
				Public_State.establish_four=baseDao.resultSet.getString("establish_four").split("-");
				Public_State.start_time=baseDao.resultSet.getString("start_time");
				Public_State.end_time=baseDao.resultSet.getString("end_time");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 添加每局用户战绩
	 * @param roomBean
	 * @return
	 */
	public int addPK_Record(RoomBean roomBean){
		//插入房间信息
		int roomid = add_PK_Room(roomBean);
		String sql ="insert into pk_record_table values(null,?,?,?,?,?)";
		for(UserBean userBean:roomBean.getGame_userlist()){
			baseDao.executeUpdate(sql, new Object[]{
					userBean.getUserid(),
					userBean.getNumber(),
					roomBean.getGame_number(),
					roomBean.getBanker()==userBean.getUserid()?1:0,
					roomid
			});
		}
		return 0;
	}
	/**
	 * 插入房间信息
	 * @param roomBean
	 * @return
	 */
	public int add_PK_Room(RoomBean roomBean){
		System.out.println("roomBean.getUser_log_text()========"+roomBean.getUser_log_text());
		System.out.println("roomBean.getUser_log_text()========"+roomBean.getUser_log_text().toString());
		String sql = "insert into pk_table (pkid,roomno,start_date,max_person,houseid,max_number,log,clubid,game_number) values(null,?,NOW(),?,?,?,?,?,?)";
		String state = baseDao.executeUpdate(sql, new Object[]{
				roomBean.getRoomno(),
				roomBean.getMax_person(),
				roomBean.getHouseid(),
				roomBean.getMax_number(),
				AES.encrypt(roomBean.getUser_log_text()),
				roomBean.getClubid(),
				roomBean.getGame_number()
		});
		if("success".equals(state)){
			sql = "SELECT LAST_INSERT_ID() as id";
			baseDao.executeAll(sql, null);
			try {
				if(baseDao.resultSet.next()){
					return baseDao.resultSet.getInt("id");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	public void updateAA(int clubid,int i) {
		String sql = "update game_card_circle set aarecord=aarecord+? where circlenumber=?";
		baseDao.executeUpdate(sql, new Object[]{i,clubid});
	}

    public String getCard_type() {
		String sql = "SELECT * FROM cardtype  WHERE id >= (SELECT FLOOR( MAX(id) * RAND()) FROM cardtype ) ORDER BY id LIMIT 1;";
		baseDao.executeAll(sql, new Object[] {});
		try {
			if (baseDao.resultSet.next()) {
				return baseDao.resultSet.getString("value");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
    }
}
