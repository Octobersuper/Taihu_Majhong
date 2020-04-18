package com.zcf.mahjong.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.zcf.mahjong.bean.RoomBean;
import com.zcf.mahjong.bean.UserBean;
import com.zcf.mahjong.dao.ClubDao;
import com.zcf.mahjong.dao.M_GameDao;
import com.zcf.mahjong.mahjong.Establish_PK;
import com.zcf.mahjong.mahjong.Matching_PK;
import com.zcf.mahjong.mahjong.Public_State;
import com.zcf.mahjong.util.BaseDao;
import com.zcf.mahjong.util.MahjongUtils;
import com.zcf.mahjong.util.Mahjong_Util;

public class M_GameService {
	private M_GameDao gameDao;
	private ClubDao clubDao;

	public M_GameService(BaseDao baseDao) {
		this.gameDao = new M_GameDao(baseDao);
		this.clubDao = new ClubDao(baseDao);
	}

	/**
	 * 创建房间
	 * 
	 * @return
	 */
	public RoomBean Establish(Map<String, String> map, UserBean userBean,
			int clubid) {
		// 房主支付
		int index = 0;
		if (Integer.parseInt(map.get("max_number")) == 4) {
			index = 0;
		}
		if (Integer.parseInt(map.get("max_number")) == 8) {
			index = 1;
		}
		if (Integer.parseInt(map.get("max_number")) == 16) {
			index = 2;
		}
		int num = 0;
		Integer paytype = Integer.valueOf(map.get("paytype"));//0房主  1AA
		if(paytype==0){
			// 创建房间所需钻石(8局-16局-32局)
			if (Integer.parseInt(map.get("max_person")) == 4) {
				num = Integer.parseInt(Public_State.establish_four[index]);
			} else if (Integer.parseInt(map.get("max_person")) == 3) {
				num = Integer.parseInt(Public_State.establish_three[index]);
			} else {
				num = Integer.parseInt(Public_State.establish_two[index]);
			}
		}else{
			// 创建房间所需钻石(8局-16局-32局)
			if (Integer.parseInt(map.get("max_person")) == 4) {
				num = Integer.parseInt(Public_State.establish_four_AA[index]);
			} else if (Integer.parseInt(map.get("max_person")) == 3) {
				num = Integer.parseInt(Public_State.establish_three_AA[index]);
			} else {
				num = Integer.parseInt(Public_State.establish_two_AA[index]);
			}
		}
		int diamond = clubid == 0 ? gameDao
				.getUserDiamond(userBean.getUserid()) : clubDao
				.getClub_diamond(clubid);
		// 检测 钻石不足
		if (diamond < num) {
			return null;
		}
		// 创建房间
		RoomBean roomBean = Establish_PK.Establish();
		// 指定房主
		roomBean.setHouseid(userBean.getUserid());
		// 房间价格
		roomBean.setMoney(num);
		// 加入自己
		roomBean.getGame_userlist().add(userBean);
		//支付方式
		roomBean.setPaytype(paytype);
		// 更新分数
		userBean.setNumber(0);
		// 添加坐标
		userBean.setLog_lat(String.valueOf(map.get("loglat")));
		// 加入座位
		roomBean.setUser_positions(userBean.getUserid());
		// 房间最大局数
		roomBean.setMax_number(Integer.parseInt(map.get("max_number")));
		// 人数
		roomBean.setMax_person(Integer.parseInt(map.get("max_person")));
		// 0为个人创建
		roomBean.setClubid(clubid);
		// 底花倍率
		roomBean.setDihua_ords(Integer.parseInt(map.get("dihua_ords")));
		// 飘
		roomBean.setPiao(Integer.parseInt(map.get("piao")));
		// 点炮胡
		roomBean.setDianhu(Integer.parseInt(map.get("dianhu")));
		// 自摸
		roomBean.setZimo(Integer.parseInt(map.get("zimo")));
		return roomBean;
	}

	/**
	 * 加入房间
	 * 
	 * @param map
	 * @param userBean
	 * @return
	 */
	public RoomBean Matching(Map<String, String> map, UserBean userBean) {
		// 初始化分数
		userBean.setNumber(0);

		// 添加坐标
		userBean.setLog_lat(String.valueOf(map.get("loglat")));
		// 加入房间
		return Matching_PK.Matching(userBean, map.get("roomno"));
	}

	/**
	 * 检测钻石是否可以加入房间
	 * 
	 * @param userBean
	 * @param roomno
	 * @return
	 */
	public int ISMatching_Money(UserBean userBean, String roomno) {
		if (Public_State.PKMap.get(roomno) == null) {
			return 100;// 房间不存在
		}
		if (ISMatching(roomno, userBean)) {
			return 114;// 重复加入
		}
		// 检测房间类型
		RoomBean roomBean = Public_State.PKMap.get(roomno);
		// 检测钻石是否满足
		// 用户钻石
		/*
		 * int money = gameDao.getUserDiamond(userBean.getUserid()); if (money <
		 * roomBean.getMoney()) { return 105; }
		 */
		return 0;
	}

	/**
	 * 查询茶馆钻石
	 * 
	 * @param clubid
	 * @return
	 */
	public int getClub_Money(int clubid) {
		return clubDao.getClub_diamond(clubid);
	}

	/**
	 * 准备
	 * 
	 * @param userBean
	 * @param roomBean
	 * @return
	 */
	public int Ready(UserBean userBean, RoomBean roomBean) {
		// 准备
		userBean.setReady_state(1);
		int count = 0;
		// 判断是否所有人都准备
		roomBean.getLock().lock();
		for (UserBean user : roomBean.getGame_userlist()) {
			if (user.getReady_state() == 1)
				count++;
		}
		roomBean.getLock().unlock();
		return count;
	}

	/**
	 * 检测是否有人可以胡牌
	 * 
	 * @param roomBean
	 * @param state
	 * @return
	 */
	public int Is_Hu(RoomBean roomBean, int state,UserBean user) {
		if (state == 0) {
			roomBean.setHucount(roomBean.getHucount() + 1);
		}else{
			user.setHu_state(0);
			roomBean.getHu_user_list().add(user);
		}
		return roomBean.getHucount();
	}

	/**
	 * 胡检测(点炮)
	 * 
	 * @param userBean
	 * @return
	 */
	public int End_Hu(UserBean userBean, int brand, RoomBean roomBean,
			int userid) {
		// 检测胡牌人数(不包含点炮人自己)
		roomBean.IS_HU(userBean, brand, roomBean, userid);
		return userBean.getHu_type();
	}

	/**
	 * 胡检测(自摸)
	 * 
	 * @param userBean
	 * @param roomBean
	 * @return
	 */

	public int End_Hu_This(UserBean userBean, RoomBean roomBean, int brand) {
		return Mahjong_Util.mahjong_Util.IS_Victory(userBean.getBrands(),
				userBean, brand);
	}

	/**
	 * 结算(自摸)
	 * 
	 * @param userBean
	 * @param roomBean
	 * @return
	 */
	public int End_Game_This(UserBean userBean, RoomBean roomBean) {
		
		int sumFen = 0;
		int beishu = 1;
		for (UserBean user : roomBean.getGame_userlist()) {
			if (user.getUserid() != userBean.getUserid()) {
				Integer integer1 = userBean.getBeichipeng().get(
						user.getUserid());
				Integer integer2 = userBean.getChipeng().get(user.getUserid());
				if (integer2 == null) {
					integer2 = 0;
				}
				if (integer1 == null) {
					integer1 = 0;
				}
				int sum = 1;
				int chifen = 0;
				if (integer2 >= 3 || integer1>=3) {
					sum = 3;
					chifen = (userBean.getPower_number() * sum)-userBean.getPower_number();
				}
				if (sum > beishu) {
					beishu = sum;
				}
				int fen = (userBean.getPower_number() + roomBean.getPiao() + chifen);
				// 扣除失败者分数
				user.setNumber(user.getNumber() - fen);
				user.setDqnumber(-fen);
				sumFen += fen;
			}
		}
		int huafen = userBean.getHuafen();// 花分
		if (huafen != 0) {
			userBean.getRecordMsgList().add("花+" + huafen);
		}
		if (roomBean.getPiao() != 0) {
			userBean.getRecordMsgList().add("飘+" + roomBean.getPiao());
		}

		userBean.setNumber(sumFen + userBean.getNumber());
		userBean.setDqnumber(sumFen);
		return EndGame(roomBean, userBean);
	}

	/**
	 * 扣除开房钻石
	 * 
	 * @param roomBean
	 * @return
	 */
	private int EndGame(RoomBean roomBean, UserBean userBean) {
		int state = 0;
		// boolean time_bool =
		// UtilClass.utilClass.IFTDate(Public_State.start_time + ":00",
		// Public_State.end_time + ":00");
		// 判断是什么模式0个人
		if (roomBean.getClubid() == 0) {

			// 判断当前第一局则扣除钻石0房主模式1AA
			if (roomBean.getGame_number() == 1 && roomBean.getPaytype()==0) {
				// 扣除房主钻石
				gameDao.UpdateUserDiamond(roomBean.getHouseid(),
						roomBean.getMoney(), 0);
			} else if (roomBean.getGame_number() == 1 && roomBean.getPaytype()==1) {
				for (UserBean user : roomBean.getGame_userlist()) {
					// 扣除用户钻石
					gameDao.UpdateUserDiamond(user.getUserid(),
							roomBean.getMoney()
									/ roomBean.getGame_userlist().size(), 0);
				}
			}

		} else {
			if (roomBean.getGame_number() == 1 && roomBean.getPaytype()==0) {
				// 俱乐部模式
				state = clubDao.Update_Club_Money(roomBean.getClubid(),
						roomBean.getMoney(), 0);
			} else if (roomBean.getGame_number() == 1 && roomBean.getPaytype()==1) {
				for (UserBean user : roomBean.getGame_userlist()) {
					// 扣除用户钻石
					gameDao.UpdateUserDiamond(user.getUserid(),
							roomBean.getMoney()
									/ roomBean.getGame_userlist().size(), 0);
				}
				//记录俱乐部AA总流水
				gameDao.updateAA(roomBean.getClubid(),roomBean.getMoney()*roomBean.getGame_userlist().size());
			}
		}
		// 更改结算状态
		roomBean.setState(4);
		// 初始化房间小局
		// roomBean.Initialization();
		// roomBean.getLock().unlock();
		return state;
	}

	public void addRecord(RoomBean roomBean) {
		// 记录战绩
		gameDao.addPK_Record(roomBean);
	}

	/**
	 * 点炮结算(金币)
	 * 
	 * @param userBean
	 * @param roomBean
	 * @param p_userid
	 * @param state
	 * @return
	 */
	public int End_GameM(UserBean userBean, RoomBean roomBean, int p_userid,
			int state) {
		roomBean.getLock().lock();
		int sum_num = 0;
		for (UserBean user : roomBean.getHu_user_list()) {
			// 计算分数=房间底分*用户番数
			int fen = roomBean.getDihua_ords() * user.getPower_number();
			user.setNumber(user.getNumber() + fen);
			user.setDqnumber(fen);
			sum_num += fen;
			// 如果是金币模式则修改用户金币

		}
		gameDao.UpdateUserMoney(p_userid, sum_num, 0);
		roomBean.getLock().unlock();
		return EndGame(roomBean, userBean);
	}

	/**
	 * 结算(点炮)
	 * 
	 * @param userBean
	 * @param roomBean
	 * @return
	 */
	public int End_Game(UserBean userBean, RoomBean roomBean, int p_userid,
			int state,int brand) {
		roomBean.getLock().lock();
		// 设置所有人同意结算
		for (UserBean user:roomBean.getHu_user_list()
			 ) {
			user.setHu_state(state);
		}
		if (roomBean.getState() == 4) {
			roomBean.getLock().unlock();
			return 500;// 已经结算
		}
		if (roomBean.getState() == 3) {
			roomBean.getLock().unlock();
			return 502;// 等待结算
		}
		// 胡牌人数大于1的时候需要检测
		if (roomBean.getHu_user_list().size() > 1) {
			for (int i = 0; i < roomBean.getHu_user_list().size(); i++) {
				// 当前用户的胡牌状态
				if (roomBean.getHu_user_list().get(i).getHu_state() == 0) {
					roomBean.getLock().unlock();
					return 501;// 等待胡牌
				} else if (roomBean.getHu_user_list().get(i).getHu_state() == 2) {
					roomBean.getHu_user_list().remove(
							roomBean.getHu_user_list().get(i));
					i--;
				}
			}
		}


		// 结算用户分数及总数
		int sum_num = 0;
		// 正在结算
		roomBean.setState(3);
		UserBean userBean2 = roomBean.getUserBean(p_userid);//点炮者

		for (UserBean user :
				roomBean.getHu_user_list()) {
			Integer integer1 = user.getBeichipeng().get(userBean2.getUserid());
			Integer integer2 = user.getChipeng().get(userBean2.getUserid());
			if (integer2 == null) {
				integer2 = 0;
			}
			if (integer1 == null) {
				integer1 = 0;
			}
			int sum = 1;
			if (integer2 >= 3) {
				sum = 3;
			}
			if (integer1 >= 3) {
				sum = 3;
			}
			sum = sum * roomBean.getDihua_ords();// 吃三口乘以底花倍数
			// 点炮 2分
			user.setPower(2*sum);
			user.getRecordMsgList().add("吃胡+"+2*sum);
			if (roomBean.getPiao() != 0) {
				user.getRecordMsgList().add("飘+" + roomBean.getPiao());
			}
			Map<String, Integer> show = user.getShowBars();
			if(show.size()!=0){
				int fan = 0;
				if(show.get("wanG")!=0){
					fan += (show.get("wanG")*5)*sum;
				}
				if(show.get("fengG")!=0){
					fan += (show.get("fengG")*6)*sum;
				}
				if(fan!=0){
					user.getRecordMsgList().add("明杠+" + fan);
					user.setPower(fan);
				}
			}

			Map<String, Integer> hide = user.getHideBars();
			if(hide.size()!=0){
				int fan = 0;
				if(hide.get("wanG")!=0){
					fan += (hide.get("wanG")*10)*sum;
				}
				if(hide.get("fengG")!=0){
					fan += (hide.get("fengG")*12)*sum;
				}
				if(hide.get("huaG")!=0){
					fan += (hide.get("huaG")*12)*sum;
				}
				if(fan!=0){
					user.getRecordMsgList().add("暗杠+" + fan);
					user.setPower(fan);
				}
			}

			// 结算检测
			MahjongUtils mahjongUtils = new MahjongUtils();
			mahjongUtils.getBrandKe(roomBean, user, brand, 1,sum);
			int huafen = user.getHuafen();// 花分 算法（花分+胡分）*sum+飘分
			if (huafen != 0) {
				user.getRecordMsgList().add("花+" + huafen);
			}
			// 计算分数=房间底分*用户番数
			int fen = user.getPower_number() + roomBean.getPiao();
			user.setNumber(user.getNumber() + fen);
			user.setDqnumber(fen);
			sum_num += fen;
		}
		// 扣除失败者分数
		userBean2.setNumber(userBean2.getNumber() - sum_num);
		userBean2.setDqnumber(-sum_num);

		roomBean.getLock().unlock();
		return EndGame(roomBean, userBean);
	}

	/**
	 * 开始游戏
	 * 
	 * @param roomBean
	 * @return
	 */
	public int StartGame(RoomBean roomBean) {

		// 初始化
		roomBean.Initialization();
		// 房间状态
		roomBean.setState(2);
		// 不流局才选庄
		if (roomBean.getFlow() == 0) {
			// 选庄
			roomBean.Select_Banker();
		}
		// 发牌
		roomBean.Deal();
		return 0;
	}

	/**
	 * 出牌
	 *
	 * @param roomBean
	 * @param outbrand
	 * @param userBean
	 * @return
	 */
	public int OutBrand(RoomBean roomBean, int outbrand, UserBean userBean,
			Map<String, Object> returnMap) {
		int state = 0;
		// 计算是否有人可碰
		Map<String, Object> newReturnMap = new HashMap<>();
		for (UserBean user : roomBean.getGame_userlist()) {
			// 是自己则跳过
			if (user.getUserid() == userBean.getUserid())
				continue;
			Map<String, Object> userMap = new HashMap<String, Object>();
			newReturnMap = new HashMap<>();
			// 检测吃
			if (user.getUserid() == roomBean.getNextUserId2()) {
				int[] user_eat = Mahjong_Util.mahjong_Util.IS_Eat(
						user.getBrands(), outbrand);
				userMap.put("userid", user.getUserid());
				userMap.put("eat", user_eat);
				userMap.put("eat_type", 0);
				newReturnMap.put("eat", userMap);
			}
			// 检测碰/杠
			int[] user_bump = Mahjong_Util.mahjong_Util.IS_Bump(
					user.getBrands(), outbrand);
			if (user_bump != null) {
				Map<String, Object> userMap2 = new HashMap<String, Object>();
				// 可碰杠人的id
				userMap2.put("userid", user.getUserid());
				userMap2.put("bump", user_bump);
				newReturnMap.put("bump", userMap2);
				state = 1;
				if (newReturnMap.get("eat") != null) {
					state = 0;
				}
				returnMap.putAll(newReturnMap);
				break;
			} else {
				roomBean.getNextMap().clear();
				returnMap.putAll(newReturnMap);
			}
		}
		if (state == 1) {
			returnMap.put("code", state);
			returnMap.remove("eat");
			int[] user_eat = new int[] { -1, -1, -1, -1 };
			Map<String, Object> userMap = new HashMap<String, Object>();
			userMap.put("userid", roomBean.getNextUserId3(userBean.getUserid()));
			userMap.put("eat", user_eat);
			userMap.put("eat_type", 0);
			returnMap.put("eat", userMap);
		} else {
			returnMap.put("code", state);
		}
		// 出的牌
		returnMap.put("outbrand", outbrand);
		// 出牌人id
		returnMap.put("out_userid", userBean.getUserid());
		// 出牌
		OutBrand(userBean, outbrand);
		if (returnMap.get("bump") == null) {
			// 需要摸牌
			return 300;
		}
		// 有碰
		return 0;
	}

	/**
	 * 碰牌
	 * 
	 * @param userBean
	 * @param userid
	 * @param brand
	 * @param roomBean
	 * @return
	 */
	public int[] Bump_Brand(UserBean userBean, int userid, int brand,
			RoomBean roomBean) {
		int[] brands = new int[2];
		// 获取对方用户
		UserBean user = roomBean.getUserBean(userid);
		// 删除出牌用户的牌
		user.Remove_Brands_Out(brand);
		// 将牌放入碰牌用户
		userBean.getBump_brands().add(brand);
		// 删除碰牌用户手里
		brand = Mahjong_Util.mahjong_Util.getBrand_Value(brand);
		int count = 0;
		for (int i = 0; i < userBean.getBrands().size(); i++) {
			if (brand == Mahjong_Util.mahjong_Util.getBrand_Value(userBean
					.getBrands().get(i))) {
				userBean.getBump_brands().add(userBean.getBrands().get(i));
				brands[count] = userBean.getBrands().get(i);
				userBean.Remove_Brands(userBean.getBrands().get(i));
				i--;
				count++;
				if (count == 2) {
					break;
				}
			}
		}
		// 设置当前操作用户为自己
		roomBean.setEnd_userid(userBean.getUserid());
		return brands;
	}

	/**
	 * 明杠（手中3张）
	 *
	 *
	 * @param userBean
	 * @param userid
	 * @param brand
	 * @param roomBean
	 * @return
	 */
	public int Show_Bar(UserBean userBean, int userid, int brand,
			RoomBean roomBean) {
		// 删除碰牌用户手里
		brand = Mahjong_Util.mahjong_Util.getBrand_Value(brand);
		// “筒条万”明杠5分 0-26
		ArrayList<Integer> ttwList = new ArrayList<>();
		for (int i = 0; i < 27; i++) {
			ttwList.add(i);
		}
		if (ttwList.contains(brand)) {
			userBean.getShowBars().put("wanG",userBean.getShowBars().get("wanG")+1);
		}
		// “南西北”明杠10分 28-30
		ArrayList<Integer> nxbList = new ArrayList<>();
		nxbList.add(28);
		nxbList.add(29);
		nxbList.add(30);
		if (nxbList.contains(brand)) {
			userBean.getShowBars().put("fengG",userBean.getShowBars().get("fengG")+1);
		}
		// 获取出牌用户
		UserBean user = roomBean.getUserBean(userid);
		// 删除出牌用户出得牌
		user.Remove_Brands(brand);
		// 将牌放入自己明杠牌集合
		userBean.getShow_brands().add(brand);
		for (int i = 0; i < userBean.getBrands().size(); i++) {
			if (brand == Mahjong_Util.mahjong_Util.getBrand_Value(userBean
					.getBrands().get(i))) {
				// 将牌放入自己明杠牌集合
				userBean.getShow_brands().add(userBean.getBrands().get(i));
				// 删除手中得牌
				userBean.Remove_Brands(userBean.getBrands().get(i));
				i--;
			}
		}
		// 设置当前操作用户为自己
		roomBean.setEnd_userid(userBean.getUserid());
		// 如果杠的是癞子就不可以点炮
		/*
		 * if (brand == roomBean.getDraw()) { roomBean.setCannon(1); return 1;//
		 * 不可以点炮 }
		 */
		return 0;
	}

	/**
	 * 补杠（碰3张）
	 * 
	 * @param userBean
	 * @param brand
	 * @param roomBean
	 * @return
	 */
	public int Repair_Bar_Bump(UserBean userBean, int brand, RoomBean roomBean) {
		brand = Mahjong_Util.mahjong_Util.getBrand_Value(brand);
		for (int i = 0; i < userBean.getBrands().size(); i++) {
			if (brand == Mahjong_Util.mahjong_Util.getBrand_Value(userBean
					.getBrands().get(i))) {
				// 将牌放入自己得明杠牌集合
				userBean.getShow_brands().add(userBean.getBrands().get(i));
				// 删除用户手里得牌
				userBean.Remove_Brands(userBean.getBrands().get(i));
				i--;
			}
		}
		// “筒条万”明杠5分 0-26
		ArrayList<Integer> ttwList = new ArrayList<>();
		for (int i = 0; i < 27; i++) {
			ttwList.add(i);
		}
		if (ttwList.contains(brand)) {
			userBean.getShowBars().put("wanG",userBean.getShowBars().get("wanG")+1);
		}
		// “南西北”明杠10分 28-30
		ArrayList<Integer> nxbList = new ArrayList<>();
		nxbList.add(28);
		nxbList.add(29);
		nxbList.add(30);
		if (nxbList.contains(brand)) {
			userBean.getShowBars().put("fengG",userBean.getShowBars().get("fengG")+1);
		}
		// 删除碰的牌
		for (int i = 0; i < userBean.getBump_brands().size(); i++) {
			if (brand == Mahjong_Util.mahjong_Util.getBrand_Value(userBean
					.getBump_brands().get(i))) {
				// 将牌放入自己得明杠牌集合
				userBean.getShow_brands().add(userBean.getBump_brands().get(i));
				// 删除用户手里碰得牌
				userBean.Remove_Brands_Bump(userBean.getBump_brands().get(i));
				i--;
			}
		}
		// 设置当前操作用户为自己
		roomBean.setEnd_userid(userBean.getUserid());
		// 如果杠的是癞子就不可以点炮
		/*
		 * if (brand == roomBean.getDraw()) { roomBean.setCannon(1); return 1;//
		 * 不可以点炮 }
		 */
		return 0;
	}

	/**
	 * 杠牌（暗）（手中4张牌）
	 * 
	 * @param userBean
	 * @param brand
	 * @param roomBean
	 * @return
	 */
	public int[] Hide_Bar(UserBean userBean, int brand, RoomBean roomBean) {
		int[] brands = new int[4];
		int count = 0;
		brand = Mahjong_Util.mahjong_Util.getBrand_Value(brand);
		// “筒条万”暗杠10分； 0-26
		ArrayList<Integer> ttwList = new ArrayList<>();
		for (int i = 0; i < 27; i++) {
			ttwList.add(i);
		}
		if (ttwList.contains(brand)) {
			userBean.getHideBars().put("wanG",userBean.getHideBars().get("wanG")+1);
		}
		// “南西北”暗杠12分； 28-30
		ArrayList<Integer> nxbList = new ArrayList<>();
		nxbList.add(28);
		nxbList.add(29);
		nxbList.add(30);
		if (nxbList.contains(brand)) {
			userBean.getHideBars().put("fengG",userBean.getHideBars().get("fengG")+1);
		}
		// 东、中、发、白 暗杠12分； 27-31-32-33
		ArrayList<Integer> tempList = new ArrayList<>();
		tempList.add(27);
		tempList.add(31);
		tempList.add(32);
		tempList.add(33);
		if (tempList.contains(brand)) {
			userBean.getHideBars().put("huaG",userBean.getHideBars().get("huaG")+1);
		}
		for (int i = 0; i < userBean.getBrands().size(); i++) {
			if (brand == Mahjong_Util.mahjong_Util.getBrand_Value(userBean
					.getBrands().get(i))) {
				brands[count] = userBean.getBrands().get(i);
				count++;
				// 将牌放入自己得暗杠牌集合
				userBean.getHide_brands().add(userBean.getBrands().get(i));
				// 删除用户手里得牌
				userBean.Remove_Brands(userBean.getBrands().get(i));
				i--;
			}
		}
		return brands;
	}

	/**
	 * 出牌
	 * 
	 * @param userBean
	 * @param index
	 */
	public void OutBrand(UserBean userBean, int index) {
		if(!userBean.getOut_brands().contains(index)){
			userBean.getOut_brands().add(index);
		}
		if(userBean.getBrands().contains(index)){
			userBean.Remove_Brands(index);
		}
	}

	/**
	 * 检测是否重复加入
	 * 
	 * @param userBean
	 * @return
	 */
	public boolean ISMatching(String roomno, UserBean userBean) {
		RoomBean roomBean = Public_State.PKMap.get(roomno);
		for (UserBean user : roomBean.getGame_userlist()) {
			if (user.getUserid() == userBean.getUserid()) {
				return true;
			}
		}
		return false;
	}

	/***
	 * 发起/同意/解散房间
	 * 
	 * @param userBean
	 * @param roomBean
	 * @return
	 */
	public int Exit_GameUser(UserBean userBean, RoomBean roomBean) {
		roomBean.getLock().lock();
		// 不同意
		if (userBean.getExit_game() == 2) {
			roomBean.setExit_game(0);
			for (UserBean user : roomBean.getGame_userlist()) {
				user.setExit_game(0);
			}
			roomBean.getLock().unlock();
			// 取消解散
			return 303;
		} else {
			// 第一次发起
			if (roomBean.getExit_game() == 0) {
				roomBean.setExit_game(1);
				roomBean.getLock().unlock();
				// 发起解散
				return 301;
			}
			// 检测是否都同意解散
			for (UserBean user : roomBean.getGame_userlist()) {
				// 只要有一个用户未操作都返回同意
				if (user.getExit_game() == 0) {
					// user.setExit_game(1);
					roomBean.getLock().unlock();
					return 302;
				}
			}
			roomBean.getLock().unlock();
			return 304;
		}
	}

	/**
	 * 是否吃牌
	 * 
	 * @param userBean
	 * @param roomBean
	 * @throws
	 */
	public int[] Is_Eat(UserBean userBean, int userid, int brand,
			RoomBean roomBean) {
		int[] brands = new int[2];
		// 获取对方用户
		UserBean user = roomBean.getUserBean(userid);
		// 删除出牌用户的牌
		user.Remove_Brands_Out(brand);
		// 将牌放入吃牌用户
		userBean.getEat_brands().add(brand);
		// 删除吃牌用户手里
		brand = Mahjong_Util.mahjong_Util.getBrand_Value(brand);
		int count = 0;
		for (int i = 0; i < userBean.getBrands().size(); i++) {
			if (brand == Mahjong_Util.mahjong_Util.getBrand_Value(userBean
					.getBrands().get(i))) {
				userBean.getEat_brands().add(userBean.getBrands().get(i));
				brands[count] = userBean.getBrands().get(i);
				userBean.Remove_Brands(userBean.getBrands().get(i));
				i--;
				count++;
				if (count == 2) {
					break;
				}
			}
		}
		// 设置当前操作用户为自己
		roomBean.setEnd_userid(userBean.getUserid());
		return brands;
	}
}
