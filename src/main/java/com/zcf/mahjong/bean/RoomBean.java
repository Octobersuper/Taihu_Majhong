package com.zcf.mahjong.bean;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.google.gson.Gson;
import com.zcf.mahjong.dao.M_GameDao;
import com.zcf.mahjong.mahjong.Public_State;
import com.zcf.mahjong.util.BaseDao;
import com.zcf.mahjong.util.Mahjong_Util;

public class RoomBean {
	//房间号
	private String roomno;
	//房间扣费
	private int money;
	//麻将总牌
	private List<Integer> brands;
	//玩家集合
	private List<UserBean> game_userlist;
	//房间操作记录
	private List<Map<String,Object>> user_log;
	//GPS 0默认不启用1启用
	private int gps;
	//当前庄家id
	private int banker;
	//房主id
	private int houseid;
	//当前局数
	private int game_number;
	//房间最大局数
	private int max_number;
	//房间最大人数
	private int max_person;
	//房间状态0等待加入1准备阶段2已开始3正在结算4已结算
	private int state;
	//房间解散状态0未发起1正在解散
	private int exit_game;
	//最后操作人的id
	private int end_userid;
	//是否可以点炮0可以1不可以
	private int cannon;
	//番
	private int fan;
	//房间锁
	private Lock lock;
	//胡的人数
	private List<UserBean> hu_user_list;
	//俱乐部id房间类型默认个人创建0
	private int clubid;
	//作弊
	private int onkey=-1;
	//用户位置0-3下标代表1-4的座位，值代表用户id
	private int[] user_positions;
	//明杠的人
	private int show_baruserid;
	//暗杠的人
	private int hide_baruserid;
	//补杠的人
	private int repair_baruserid;
	//补花的人
	private int buhua_baruserid;
	//胜利的人
	private int victoryid=-1;
	//是否有人胡牌
	private int hucount;
	//是否流局
	private int flow;
	//最后一次操作
	private Map<String,Object> nextMap;
	
	private List<Integer> dice;
	
	//支付方式
	private int paytype;
	//自摸底翻番 0翻番  1不翻番
	private int zimo;
	//点胡炮 0可以点胡 1不可以点胡 
	private int dianhu;
	//底花倍数 1 2 3 5 10倍
	private int dihua_ords;
	//飘 0不飘 
	private int piao;
	private int gangnum;//连续杠的次数
	//最后一次出的牌
	private int lastBrand;
	//最后一次出牌人id
	private int lastUserid;

	public int getLastBrand() {
		return lastBrand;
	}

	public void setLastBrand(int lastBrand) {
		this.lastBrand = lastBrand;
	}

	public int getLastUserid() {
		return lastUserid;
	}

	public void setLastUserid(int lastUserid) {
		this.lastUserid = lastUserid;
	}

	public int getGangnum() {
		return gangnum;
	}

	public void setGangnum(int gangnum) {
		this.gangnum = gangnum;
	}

	public int getBuhua_baruserid() {
		return buhua_baruserid;
	}
	public void setBuhua_baruserid(int buhua_baruserid) {
		this.buhua_baruserid = buhua_baruserid;
	}

	Map<String,Object> map = new HashMap<String, Object>();
	public RoomBean(){
		//用户座位
		this.user_positions = new int[]{-1,-1,-1,-1};
		//麻将集合
		this.brands = new ArrayList<Integer>();
		//房间锁
		this.lock = new ReentrantLock(true);
		//玩家集合
		this.game_userlist = new ArrayList<UserBean>();
		//房间操作记录
		this.user_log = new ArrayList<Map<String,Object>>();
		//胡牌人数
		this.hu_user_list = new ArrayList<UserBean>();
		//记录碰的操作
		this.nextMap = new HashMap<String, Object>();
		this.gangnum = 1;
	}
	/**
	 * 房间信息初始化
	 */
	public void Initialization(){
		//初始化麻将
		Init_Brands();
		//初始化用户
		for(UserBean user:game_userlist){
			user.Initialization();
		}
		//操作日志
		this.user_log.clear();
		//flow==1=流局
		if(flow==0){
			this.game_number++;
			System.out.println("game_number========="+game_number);
		}
		//胡牌人数
		this.hu_user_list.clear();
		//清空杠的人
		this.show_baruserid=0;
		this.hide_baruserid=0;
		this.repair_baruserid=0;
		this.buhua_baruserid=0;
		this.onkey=-1;
		this.state=1;
		this.gangnum=1;
	}
	
	/**
	 * 初始化一副麻将
	 */
	private void Init_Brands(){
		brands.clear();
		for(int i=0;i<136;i++){
			brands.add(i);
		}
		//提出风头牌
		//RemoveBrand(new int[]{27,28,29,30,31,32,33});
	}
	/**
	 * 发牌
	 */
	public void Deal(){
		//获取混皮儿和混
		int number = 13;
		for(UserBean userBean:this.game_userlist){
			if(userBean.getUserid()==this.banker){
				number=14;
			}else{
				number=13;
			}
			int num = new Random().nextInt(10);//随机数
			String s = String.valueOf(userBean.getNumber_5());
			Integer c = 0;
			if(s.equals("100")){
				c = 10;
			}else{
				c = Integer.valueOf(s.substring(0, 1));//玩家的概率
			}
			if(num<c){//获取随机好牌
				BaseDao baseDao = new BaseDao();
				M_GameDao dao = new M_GameDao(baseDao);
				String card_type = dao.getCard_type();
				String[] split = card_type.split("-");
				for(int i=0;i<number;i++){
					if(i==13){
						int brands = getBrand_Random();
						userBean.getBrands().add(brands);
						userBean.getCopy_brands().add(brands);
					}else{
						Integer brands = Integer.valueOf(split[i]);
						if(this.brands.contains(brands)){
							this.brands.remove(brands);
							userBean.getBrands().add(brands);
							userBean.getCopy_brands().add(brands);
						}else{
							brands = getBrand_Random();
							userBean.getBrands().add(brands);
							userBean.getCopy_brands().add(brands);
						}
					}
				}
				baseDao.CloseAll();
			}else{//正常发牌
				for(int i=0;i<number;i++){
					int brands = getBrand_Random();
					userBean.getBrands().add(brands);
					userBean.getCopy_brands().add(brands);
				}
			}
			/*List<Integer> list = new ArrayList<>();
			if (userBean.getUserid() == this.banker) {
				//int[] arr = new int[]{31,32,33,65,66,67,99,100,101,133,134,15,16,17};//测火箭 大三元用的
				//int[] arr = new int[]{0,34,1,35,2,36,3,37,4,38,5,70,49,48};//测大板子用的 万
				int[] arr = new int[]{9,43,10,44,11,45,12,46,13,47,22,23,35,34};//测大板子用的 两人场
				for (int i = 0; i < arr.length; i++) {
					list.add(arr[i]);
				}
				RemoveBrand(arr);
				userBean.getBrands().addAll(list);
				userBean.getCopy_brands().addAll(list);
			} else {
				for (int i = 0; i < 13; i++) {
					int brands = getBrand_Random();
					userBean.getBrands().add(brands);
					userBean.getCopy_brands().add(brands);
				}
			}*/
			//排序
			Mahjong_Util.mahjong_Util.Order_Brands(userBean.getBrands());
		}
	}
	/**
     * 定庄
     */
    public void Select_Banker(){
        int index = -1;
        //第一局随机坐庄
        if(game_number==1){
			//随机0~3
			index = (int)(Math.random() * this.game_userlist.size());
			this.banker = this.game_userlist.get(index).getUserid();
        }else{
        	if(victoryid!=-1){
				//赢家坐庄
				this.banker=victoryid;
			}else{
				//随机0~3
				index = (int)(Math.random() * this.game_userlist.size());
				this.banker = this.game_userlist.get(index).getUserid();
			}
        }
        this.end_userid=this.banker;
    }

	/**
	 * 寻找庄家
	 * @param userid
	 * @return
	 */
	private int IS_Banker(int userid){
		int index=-1;
		//找寻当前庄家id
		for(int i=0;i<user_positions.length;i++){
			if(userid==user_positions[i]){
				index=i;
				break;
			}
		}
		//找寻庄家的下家id
		for(int i=0;i<user_positions.length;i++){
			index++;
			//如果庄家是最后一个人则从第一个开始找
			if(index==user_positions.length)index=0;
			if(user_positions[index]!=-1)return user_positions[index]; 
		}
		return -1;
	}

	/**
	 * 检测用户胡牌
	 * @param userBean
	 * @param brand
	 * @return
	 */
	public int ISBrand(UserBean userBean,int brand,RoomBean roomBean){
		return Mahjong_Util.mahjong_Util.IS_Victory(userBean,brand);
	}
	/**
	 * 检测胡牌的人数
	 * @param userBean
	 * @param brand
	 * @param roomBean
	 * @return
	 */
	public int IS_HU(UserBean userBean,int brand,RoomBean roomBean,int userid){
		lock.lock();
		if(hu_user_list.size()!=0){
			lock.unlock();
			return hu_user_list.size();
		}
		for(UserBean user:getGame_userlist()){
			//如果用户已弃牌则不检测
			if(user.getHu_state()==2)continue;
			if(user.getUserid()==userid)continue;
			if(ISBrand(user,brand,roomBean)!=0){
				hu_user_list.add(user);
			}
		}
		lock.unlock();
		return hu_user_list.size();
	}
	/**
	 * 从主牌中删除作弊牌
	 * @param user
	 */
	private void Deal_Bug(String[] user){
		for(int i=0;i<user.length;i++){
			for(int j=0;j<this.brands.size();j++){
				if(Integer.parseInt(user[i])==this.brands.get(j)){
					this.brands.remove(j);
					break;
				}
			}
		}
	}
	
	public static void main(String[] args) {
		//逻辑区
		
		//打印区
		System.out.println("Hello Word!");
	}
	
	/**
	 * 获取下个操作人的id
	 * @return
	 */
	public int getNextUserId(){
		int userid=-1;
		int thisindex=-1;
		for(int i=0;i<user_positions.length;i++){
			if(user_positions[i]==-1)continue;
			if(user_positions[i]==end_userid){
				thisindex=i;
			}
		}
		for(int i=0;i<user_positions.length;i++){
			if((thisindex+1)==user_positions.length){
				thisindex=0;
			}else{
				thisindex++;
			}
			if(user_positions[thisindex]==-1)continue;
			userid = user_positions[thisindex];
			break;

		}
		//为此用户创建一个倒计时线程
		//new Time_User(getUserBean(userid),this).start();
		//this.time_user.setUser(getUserBean(userid));
		end_userid=userid;
		return userid;
	}

	/**
	 *  删除指定的一张牌(单牌值)
	 * @param brand
	 */
	public void Remove_Brands_Index(int brand){
		for(int i=0;i<this.brands.size();i++){
			int index = Mahjong_Util.mahjong_Util.getBrand_Value(this.brands.get(i));
			if(index==brand){
				this.brands.remove(i);
				break;
			}
		}
	}
	/**
	 * 删除指定的一张牌(牌id)
	 * @param brand_index
	 */
	public void Remove_Brands(int brand_index){
		for(int i=0;i<this.brands.size();i++){
			if(this.brands.get(i)==brand_index){
				this.brands.remove(i);
				break;
			}
		}
	}
	/**
	 * 随机获取一张牌
	 * @return
	 */
	public int getBrand_Random(){
		if(this.brands.size()==0)return -1;
		int index = (int)(Math.random() * this.brands.size());
		//查看是否有作弊
		if(this.onkey!=-1){
			index=this.onkey;
			this.onkey=-1;
			return index;
		}
		//获取单幅牌值
		int brand = this.brands.get(index);
		this.brands.remove(index);
		return brand;
	}
	/**
	 * 删除指定牌
	 * @param remove_brands
	 */
	private void RemoveBrand(int[] remove_brands){
		for(int i=0;i<remove_brands.length;i++){
			for(int j=0;j<this.brands.size();j++){
				//获取单幅牌值
				int brand = Mahjong_Util.mahjong_Util.getBrand_Value(this.brands.get(j));
				if(brand==remove_brands[i]){
					this.brands.remove(j);
					j--;
				}
			}
		}
	}
	/**
	 * 自定义获取房间列及用户列
	 * @param table
	 * @param returnMap
	 * @param usertable
	 */
	public void getRoomBean_Custom(String table,Map<String,Object> returnMap,String usertable){
		getRoomBean_Custom(table, returnMap);
		returnMap.put("userList", getGame_userList(usertable,0));
		
	}
	/**
	 * 自定义获取房间列及用户列(胡牌用户)
	 * @param table
	 * @param returnMap
	 * @param usertable
	 */
	public void getRoomBean_Custom_HU(String table,Map<String,Object> returnMap,String usertable){
		getRoomBean_Custom(table, returnMap);
		returnMap.put("userList", getGame_userList(usertable,1));
	}
	/**
	 * 自定义获取房间列
	 * @param table
	 * @param returnMap
	 */
	public void getRoomBean_Custom(String table,Map<String,Object> returnMap){
		String[] names = table.split("-");
		for(String name:names){
			if("roomno".equals(name))returnMap.put("roomno", this.roomno);
			if("money".equals(name))returnMap.put("money", this.money);
			if("gps".equals(name))returnMap.put("gps", this.gps);
			if("banker".equals(name))returnMap.put("banker", this.banker);
			if("game_number".equals(name))returnMap.put("game_number", this.game_number);
			if("max_number".equals(name))returnMap.put("max_number", this.max_number);
			if("max_person".equals(name))returnMap.put("max_person", this.max_person);
			if("state".equals(name))returnMap.put("state", this.state+"");
			if("end_userid".equals(name))returnMap.put("end_userid", this.end_userid);
			if("fan".equals(name))returnMap.put("fan", this.fan);
			if("user_positions".equals(name))returnMap.put("user_positions", this.user_positions);
			if("brands_count".equals(name))returnMap.put("brands_count", this.brands.size());
			if("nextMap".equals(name))returnMap.put("nextMap", this.nextMap);
			if("dice".equals(name))returnMap.put("dice", this.dice);
			if("cannon".equals(name))returnMap.put("cannon", this.cannon);
			if("piao".equals(name))returnMap.put("piao", this.piao);
			if("dihua_ords".equals(name))returnMap.put("dihua_ords", this.dihua_ords);
			if("dianhu".equals(name))returnMap.put("dianhu", this.dianhu);
			if("zimo".equals(name))returnMap.put("zimo", this.zimo);
			
			if("user_log".equals(name)){
				if(this.user_log.size()==0){
//					Map<String,Object> map = new HashMap<String, Object>();
//					map.put("type", "123");
					returnMap.put("user_log", null);
				}else{
					returnMap.put("user_log", this.user_log.get(this.user_log.size()-1));
				}
			}
			if("exit_game".equals(name))returnMap.put("exit_game", this.exit_game);
			
		}
	}
	/***
	 * 自定义获取用户信息
	 * @param tablename
	 * @return
	 */
	public List<Map<String,Object>> getGame_userList(String tablename,int type) {
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		List<UserBean> userList;
		userList = type==1?getHu_user_list():getGame_userlist();
		for(int i=0;i<userList.size();i++){
			Map<String,Object> map = new HashMap<String, Object>();
			userList.get(i).getUser_Custom(tablename, map);
			list.add(map);
		}
		return list;
	}
	/**
	 * 删除一名用户
	 * @param userid
	 */
	public void User_Remove(int userid){
		for(int i=0;i<game_userlist.size();i++){
			if(game_userlist.get(i).getUserid()==userid){
				game_userlist.remove(i);
				break;
			}
		}
	}
	/***
	 * 获取指定id的用户实例
	 * @param userid
	 * @return
	 */
	public UserBean getUserBean(int userid){
		for(UserBean userBean:game_userlist){
			if(userBean.getUserid()==userid)return userBean;
		}
		return null;
	}
	/**
	 * 剔除指定用户的位置
	 * @param userid
	 */
	public void remove_opsitions(int userid){
		for(int i=0;i<user_positions.length;i++){
			if(user_positions[i]==userid){
				user_positions[i]=-1;
			}
		}
	}
	/***
	 * 加入用户位置-顺序加入
	 */
	public void setUser_positions(int userid){
		//2人时坐第三个位置
		if(max_person==2){
			if(user_positions[2]==-1){
				user_positions[2]=userid;
			}
		}else{
			for(int i=0;i<user_positions.length;i++){
				if(user_positions[i]==-1){
					user_positions[i]=userid;
					break;
				}
			}
		}
	}
	/**
	 * 添加操作日志
	 * @param logMap
	 */
	public void addLog(Map<String,Object> logMap){
		Map<String,Object> map = new HashMap<String, Object>();
		for(String key:logMap.keySet()){
			map.put(key, logMap.get(key));
		}
		this.user_log.add(map);
	}
	/**
	 * 分牌
	 * @throws IOException
	 */
	public List<Map<String,Object>> GrantBrand(UserBean userBean){
		int index=-1;
		List<Map<String,Object>> listMap = new ArrayList<Map<String,Object>>();
		for(int i=0;i<user_positions.length;i++){
			if(user_positions[i]==-1)continue;
			if(userBean.getUserid()==user_positions[i]){
				index=i;
				break;
			}
		}
		for(int i=index;i<index+user_positions.length;i++){
			if(i==user_positions.length)i=0;
			if(user_positions[i]==-1)continue;
			UserBean user = getUserBean(user_positions[i]);
			if(this.brands.size()==0)break;
			int brand = getBrand_Random();
			user.getBrands().add(brand);
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("brand", brand);
			map.put("userid", user.getUserid());
			listMap.add(map);
		}
		return listMap;
	}
		
//		for(int i=0;i<user_positions.length;i++){
//			if(i==user_positions.length)index=0;
//			if(user_positions[index]==-1)continue;
//			UserBean user = getUserBean(user_positions[index]);
//			int brand = getBrand_Random();
//			user.getBrands().add(brand);
//			Map<String,Object> map = new HashMap<String, Object>();
//			
//			map.put("brand", brand);
//			map.put("userid", user.getUserid());
//			listMap.add(map);
//		}
		
	/**
	 * 结算—减分
	 * @param victoryid
	 */
	public void End_Game(int victoryid,int number){
		for(UserBean userBean:game_userlist){
			if(userBean.getUserid()!=victoryid){
				userBean.setNumber(number);
			}
		}
	}
	/**
	 *清空杠记录
	 */
	public void RemoveAll_Bar(){
		show_baruserid=0;
		hide_baruserid=0;
		repair_baruserid=0;
		buhua_baruserid=0;
	}
	/**
	 * 检测杠操作是否是自己
	 * @param userid
	 * @return
	 */
	public int IS_Bar_Userid(int userid){
		if(show_baruserid==userid)return 1;
		if(hide_baruserid==userid)return 2;
		if(repair_baruserid==userid)return 3;
		if(buhua_baruserid==userid)return 4;
		return 0;
	}
	//所有用户重置准备
	public void InItReady(){
		if(this.game_number==this.max_number){
			//清空房间信息
			Public_State.PKMap.remove(this.roomno);
		}else{
			//设置不流局
			this.flow=0;
			//设置准备阶段
			this.state=1;
			Ready_InIt();
		}
	}
	//初始化所有用户准备状态
	public void Ready_InIt(){
		for(UserBean userBean:this.game_userlist){
			userBean.setReady_state(0);
			userBean.setExit_game(0);
		}
	}
	//初始化所有用户胡状态
	public void HuState_InIt(){
		for(UserBean userBean:this.game_userlist){
			userBean.setHu_state(0);
		}
		this.hu_user_list.clear();
	}
	/*******************************************get/set******************************************************/
	
	
	public List<Integer> getBrands() {
		return brands;
	}
	public Map<String, Object> getNextMap() {
		return nextMap;
	}
	public void setNextMap(Map<String, Object> nextMap) {
		this.nextMap = nextMap;
	}
	public int getFlow() {
		return flow;
	}
	public void setFlow(int flow) {
		this.flow = flow;
	}
	public int getVictoryid() {
		return victoryid;
	}
	public void setVictoryid(int victoryid) {
		this.victoryid = victoryid;
	}
	public int getHucount() {
		return hucount;
	}
	public void setHucount(int hucount) {
		this.hucount = hucount;
	}
	public int getClubid() {
		return clubid;
	}
	public void setClubid(int clubid) {
		this.clubid = clubid;
	}
	public int getMax_number() {
		return max_number;
	}
	public void setMax_number(int max_number) {
		this.max_number = max_number;
	}
	public int getHouseid() {
		return houseid;
	}
	public void setHouseid(int houseid) {
		this.houseid = houseid;
	}
	public int[] getUser_positions() {
		return user_positions;
	}
	public void setUser_positions(int[] user_positions) {
		this.user_positions = user_positions;
	}
	public int getExit_game() {
		return exit_game;
	}
	public void setExit_game(int exit_game) {
		this.exit_game = exit_game;
	}
	public int getGps() {
		return gps;
	}
	public void setGps(int gps) {
		this.gps = gps;
	}
	public int getFan() {
		return fan;
	}
	public void setFan(int fan) {
		this.fan = fan;
	}
	public String getRoomno() {
		return roomno;
	}
	public void setRoomno(String roomno) {
		this.roomno = roomno;
	}
	public int getOnkey() {
		return onkey;
	}
	public void setOnkey(int onkey) {
		this.onkey = onkey;
	}
	public void setBrands(List<Integer> brands) {
		this.brands = brands;
	}
	public List<UserBean> getGame_userlist() {
		return game_userlist;
	}
	public void setGame_userlist(List<UserBean> game_userlist) {
		this.game_userlist = game_userlist;
	}
	
	public List<Map<String, Object>> getUser_log() {
		return user_log;
	}
	public String getUser_log_text() {
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < game_userlist.size(); i++) {
			Map<String,Object> usermap = new HashMap<String, Object>(); 
			usermap.put("userid", game_userlist.get(i).getUserid());
			usermap.put("brands",game_userlist.get(i).getCopy_brands());
			usermap.put("nickName",  game_userlist.get(i).getNickname());
			usermap.put("avatarurl", game_userlist.get(i).getAvatarurl());
			list.add(usermap);
		}
		Map<String,Object> map1 = new HashMap<String, Object>(); 
		map1.put("brands_count",this.brands.size() );
		map1.put("game_number", game_number);
		map1.put("banker", banker);
		map1.put("max_number", max_number);
		map1.put("dice", dice);
		map.put("roominfo", map1);
		map.put("usercards", list);
		map.put("user_positions", user_positions);
		map.put("user_log", this.user_log);
		return new Gson().toJson(map);
	}
	public void setUser_log(List<Map<String, Object>> user_log) {
		this.user_log = user_log;
	}
	public int getBanker() {
		return banker;
	}
	public void setBanker(int banker) {
		this.banker = banker;
	}
	public int getGame_number() {
		return game_number;
	}
	public void setGame_number(int game_number) {
		this.game_number = game_number;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getEnd_userid() {
		return end_userid;
	}
	public void setEnd_userid(int end_userid) {
		this.end_userid = end_userid;
	}

	public int getCannon() {
		return cannon;
	}
	public void setCannon(int cannon) {
		this.cannon = cannon;
	}
	public int getMoney() {
		return money;
	}
	public void setMoney(int money) {
		this.money = money;
	}
	
	public Lock getLock() {
		return lock;
	}
	public void setLock(Lock lock) {
		this.lock = lock;
	}
	public int getMax_person() {
		return max_person;
	}
	public void setMax_person(int max_person) {
		this.max_person = max_person;
	}
	public List<UserBean> getHu_user_list() {
		return hu_user_list;
	}
	public void setHu_user_list(List<UserBean> hu_user_list) {
		this.hu_user_list = hu_user_list;
	}
	public int getShow_baruserid() {
		return show_baruserid;
	}
	public void setShow_baruserid(int show_baruserid) {
		this.show_baruserid = show_baruserid;
	}
	public int getHide_baruserid() {
		return hide_baruserid;
	}
	public void setHide_baruserid(int hide_baruserid) {
		this.hide_baruserid = hide_baruserid;
	}
	public int getRepair_baruserid() {
		return repair_baruserid;
	}
	public void setRepair_baruserid(int repair_baruserid) {
		this.repair_baruserid = repair_baruserid;
	}
	public List<Integer>  getDice() {
		return dice;
	}
	public void setDice(List<Integer>  dice) {
		this.dice = dice;
	}
	public int getDihua_ords() {
		return dihua_ords;
	}
	public void setDihua_ords(int dihua_ords) {
		this.dihua_ords = dihua_ords;
	}
	public int getPiao() {
		return piao;
	}
	public void setPiao(int piao) {
		this.piao = piao;
	}
	public int getZimo() {
		return zimo;
	}
	public void setZimo(int zimo) {
		this.zimo = zimo;
	}
	public int getDianhu() {
		return dianhu;
	}
	public void setDianhu(int dianhu) {
		this.dianhu = dianhu;
	}
	public int getPaytype() {
		return paytype;
	}
	public void setPaytype(int paytype) {
		this.paytype = paytype;
	}


    /**
     * 获取下个操作人的id
     * @return
     */
    public int getNextUserId2(){
        int userid=-1;
        int thisindex=-1;
        for(int i=0;i<user_positions.length;i++){
            if(user_positions[i]==-1)continue;
            if(user_positions[i]==end_userid){
                thisindex=i;
            }
        }
        for(int i=0;i<user_positions.length;i++){
            if((thisindex+1)==user_positions.length){
                thisindex=0;
            }else{
                thisindex++;
            }
            if(user_positions[thisindex]==-1)continue;
            userid = user_positions[thisindex];
            break;

        }
        //为此用户创建一个倒计时线程
        //new Time_User(getUserBean(userid),this).start();
        //this.time_user.setUser(getUserBean(userid));
        //end_userid=userid;
        return userid;
    }

    /**
     * 获取出牌人的下家
     * @return
     */
    public int getNextUserId3(int uid){
        int userid=-1;
        int thisindex=-1;
        for(int i=0;i<user_positions.length;i++){
            if(user_positions[i]==-1)continue;
            if(user_positions[i]==uid){
                thisindex=i;
            }
        }
        for(int i=0;i<user_positions.length;i++){
            if((thisindex+1)==user_positions.length){
                thisindex=0;
            }else{
                thisindex++;
            }
            if(user_positions[thisindex]==-1)continue;
            userid = user_positions[thisindex];
            break;

        }
        //为此用户创建一个倒计时线程
        //new Time_User(getUserBean(userid),this).start();
        //this.time_user.setUser(getUserBean(userid));
        //end_userid=userid;
        return userid;
    }

	public Object getHu_id() {
		ArrayList<Object> list = new ArrayList<>();
		for (UserBean user:
			 hu_user_list) {
			list.add(user.getUserid());
		}
		return list;
	}
}
