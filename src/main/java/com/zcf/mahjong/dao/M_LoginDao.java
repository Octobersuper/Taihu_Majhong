package com.zcf.mahjong.dao;

import com.zcf.mahjong.bean.UserBean;
import com.zcf.mahjong.mahjong.Public_State;
import com.zcf.mahjong.util.BaseDao;
import com.zcf.mahjong.util.UtilClass;

import java.sql.SQLException;
import java.util.Map;


public class M_LoginDao {
	private BaseDao baseDao = null;
	public M_LoginDao(BaseDao baseDao){
		this.baseDao = baseDao;
	}
	/**
	 * 查询用户
	 * @param openid
	 * @return
	 */
	public UserBean getUser(String openid){
		String sql = "select * from user_table where openid=?";
		baseDao.executeAll(sql, new Object[]{openid});
		try {
			if(baseDao.resultSet.next()){
				return (UserBean) UtilClass.utilClass.getSqlBean(UserBean.class,"/sql.properties", baseDao.resultSet, "sql_getUser");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	/**
	 * 查询用户
	 * @return
	 */
	public UserBean getUser(Map<String,Object> map){
		UserBean userBean = null;
		String sql = "select * from user_table where openid=?";
		baseDao.executeAll(sql, new Object[]{map.get("openid")});
		try {
			if(baseDao.resultSet.next()){
				userBean = (UserBean) UtilClass.utilClass.getSqlBean(UserBean.class,"/sql.properties", baseDao.resultSet, "sql_getUser");
				String nickname = map.get("nickname").toString();
				String toname = "";
				char[] names = nickname.toCharArray();
				//过滤特殊字符
				for(int i=0;i<names.length;i++){
					if(UtilClass.utilClass.isValidChar(names[i])){
						toname+=names[i];
					}
				}
				if(!toname.equals("")){
					map.put("nickname", toname);
				}
				if(!userBean.getNickname().equals(map.get("nickname"))){
					//修改用户昵称
					updateUser(map);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userBean;
	}
	/**
	 * 修改用户昵称及头像
	 * @param map
	 * @return
	 */
	public String updateUser(Map<String,Object> map){
		String sql = "update user_table set nickname=?,avatarurl=? where openid=?";
		return baseDao.executeUpdate(sql, new Object[]{
				map.get("nickname").toString(),
				map.get("avatarurl").toString(),
				map.get("openid").toString()
		});
	}
	/****
	 * 查询是否存在指定得邀请码
	 * @param number
	 * @return
	 */
	public boolean getUser_Code(String number){
		String sql = "select code from user_table where code=?";
		baseDao.executeAll(sql, new Object[]{number});
		try {
			return baseDao.resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			baseDao.CloseAll();
		}
		return false;
	}
	/**
	 * 添加用户
	 * @return
	 */
	public String adduser(Map<String,Object> userMap) {
		String sql="insert into user_table(sex,openid,nickname,avatarurl,date,sdk,money,state,statetext,ispay,fid,code,award,diamond)values(?,?,?,?,NOW(),?,?,0,'',0,0,?,0,?) ";
		String nickname = userMap.get("nickname").toString();
		String toname = "";
		char[] names = nickname.toCharArray();
		//过滤特殊字符
		for(int i=0;i<names.length;i++){
			if(UtilClass.utilClass.isValidChar(names[i])){
				toname+=names[i];
			}
		}
		if(!toname.equals("")){
			userMap.put("nickname", toname);
		}
		//邀请码
		String code = UtilClass.utilClass.getRandom(6);
		while(getUser_Code(code)){
			code = UtilClass.utilClass.getRandom(6);
		}
		return baseDao.executeUpdate(sql, new Object[] {
				Integer.valueOf(userMap.get("sex").toString())==2?0:1,
				userMap.get("openid").toString(),
				userMap.get("nickname").toString(),
				userMap.get("avatarurl").toString(),
				userMap.get("sdk").toString(),
				Integer.parseInt(UtilClass.utilClass.getTableName("/parameter.properties", "diamond")),
				code,
				Public_State.diamond
		});
	}

}