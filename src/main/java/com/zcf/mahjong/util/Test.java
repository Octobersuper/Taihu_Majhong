/*package util;

import java.util.HashMap;
import java.util.Map;

import bean.RoomBean;
import bean.UserBean;
import service.M_GameService;

public class Test {
	Mahjong_Util mu = Mahjong_Util.mahjong_Util;
	*//**
	 * 测试麻将牌对应的牌值及花色
	 *//*
	public void Test_Brand(){
		int[] brands = new int[]{10,11,12,13,28,0,1,2,3,4,5,6,7,8,9};
		//brands = mu.Brands_OrderBy();
		for(int brand:brands){
			int[] index = mu.ISUser_Mahjong(brand);
			String color = "";
			if(index[1]==0){
				color="万";
			}else if(index[1]==1){
				color="筒";
			}else if(index[1]==2){
				color="条";
			}else if(index[1]==27){
				color="东";
			}else if(index[1]==28){
				color="南";
			}else if(index[1]==29){
				color="西";
			}else if(index[1]==30){
				color="北";
			}else if(index[1]==31){
				color="中";
			}else if(index[1]==32){
				color="发";
			}else if(index[1]==33){
				color="白";
			}
			String name = index[0]+1+"";
			if(index[0]>26)name="";
			System.out.println(name+"-"+color);
		}
	}
	*//***
	 * 检测牌型
	 *//*
	public void ISBrand(){
		
	}
	*//**
	 * 胡牌检测
	 *//*
	public void End_Hu(){
		RoomBean roomBean = new RoomBean();
		M_GameService service = new M_GameService(new BaseDao());
		UserBean user1 = new UserBean();
		user1.setUserid(1);
		UserBean user2 = new UserBean();
		user2.setUserid(2);
		UserBean user3 = new UserBean();
		user3.setUserid(3);
		roomBean.getGame_userlist().add(user1);
		roomBean.getGame_userlist().add(user2);
		roomBean.getGame_userlist().add(user3);
		roomBean.setBanker(1);
		roomBean.Initialization();
		roomBean.setDraw_s(11);
		roomBean.setDraw(12);
		//roomBean.Deal_Bug();
		System.out.println("混皮儿:"+getBrand_str(roomBean.getDraw_s()));
		System.out.println("混:"+getBrand_str(roomBean.getDraw()));
		System.out.println();
		user1.getBrands().add(12);
		user1.getBrands().add(0);
		user1.getBrands().add(3);
		user1.getBrands().add(4);
		user1.getBrands().add(5);
		user1.getBrands().add(13);
		user1.getBrands().add(14);
		user1.getBrands().add(48);
		user1.getBrands().add(82);
		user1.getBrands().add(15);
		user1.getBrands().add(24);
		user1.getBrands().add(25);
		user1.getBrands().add(26);
		user1.getBrands().add(36);
		//摸一张牌
		int index = roomBean.getBrand_Random();
		index=36;
		//0不胡牌 900=4朴一将 890=1癞子3朴1将 880=1癞子4朴 缺将 
		int state = service.End_Hu_This(user1, roomBean);
		System.out.println("胡牌检测"+state+"胡牌人数"+roomBean.getHu_user_list().size());
//		if(state!=0){
//			//用户2胡牌
//			state = service.End_Game(user2, roomBean,3,1);
//			System.out.println("结算结果:"+state);
//			state=state==0?100:0;
//		}
//		state = service.End_Hu(user1,index, roomBean,2);
//		System.out.println("胡牌检测"+state+"胡牌人数"+roomBean.getHu_user_list().size());
//		if(state!=0){
//			//用户1胡牌
//			state = service.End_Game(user1, roomBean,3,1);
//			System.out.println("结算结果:"+state);
//			state=state==0?100:0;
//		}
//		if(state==100){
//			Map<String,Object> returnMap = new HashMap<String,Object>();
//			//胡牌的人信息
//			roomBean.getRoomBean_Custom_HU("", returnMap, "number-userid");
//			//点炮用户的信息
//			roomBean.getUserBean(3).getUser_Custom("userid-number", returnMap);
//			System.out.println("结算信息"+returnMap);
//		}
		
	}
	*//**
	 * 
	 *//*
	public void Test_Deal(){
		RoomBean roomBean = new RoomBean();
		UserBean user1 = new UserBean();
		user1.setUserid(1);
		UserBean user2 = new UserBean();
		user2.setUserid(2);
		UserBean user3 = new UserBean();
		user3.setUserid(3);
		roomBean.getGame_userlist().add(user1);
		roomBean.getGame_userlist().add(user2);
		roomBean.getGame_userlist().add(user3);
		roomBean.setBanker(1);
		roomBean.Initialization();
		roomBean.setDraw_s(20);
		roomBean.setDraw(21);
		//roomBean.Deal();
		System.out.println("混皮儿:"+getBrand_str(roomBean.getDraw_s()));
		System.out.println("混:"+getBrand_str(roomBean.getDraw()));
		roomBean.Deal_Bug();
		for(int index:user1.getBrands()){
			System.out.print(getBrand_str(index)+"-");
		}
		System.out.println();
//		for(int index:user3.getBrands()){
//			System.out.print(getBrand_str(index)+"-");
//		}
		//摸一张牌
		int index = roomBean.getBrand_Random();
		index=73;
		//int[] eat = mu.IS_Eat(user1.getBrands(), index, roomBean.getDraw());
		System.out.println("来牌"+getBrand_str(index));
		//System.out.println("吃检测"+getBrand_str(eat[0])+"-"+getBrand_str(eat[1])+"-"+getBrand_str(eat[2])+"-"+getBrand_str(eat[3]));
		int[] bump = mu.IS_Bump(user1.getBrands(), index, roomBean.getDraw());
		if(bump!=null){
			System.out.println("碰/杠检测"+getBrand_str(bump[0])+"-"+getBrand_str(bump[1])+"-"+getBrand_str(bump[2]));
		}
		//mu.IS_Seven(user2.getBrands(), index, roomBean.getDraw());
		//检测将
		System.out.println(user1.getBrands());
		for(int index2:user1.getBrands()){
			System.out.print(getBrand_str(index2)+"-");
		}
		System.out.println(mu.IS_Victory(user1,index,roomBean.getDraw()));
	}
	private String getBrand_str(int i){
		String color = "";
		int[] index = Mahjong_Util.mahjong_Util.ISUser_Mahjong(Mahjong_Util.mahjong_Util.getBrand_Value(i));
		if(index[1]==0){
			color="万";
		}else if(index[1]==1){
			color="筒";
		}else if(index[1]==2){
			color="条";
		}else if(index[1]==27){
			color="东";
		}else if(index[1]==28){
			color="南";
		}else if(index[1]==29){
			color="西";
		}else if(index[1]==30){
			color="北";
		}else if(index[1]==31){
			color="中";
		}else if(index[1]==32){
			color="发";
		}else if(index[1]==33){
			color="白";
		}
		String name = index[0]+1+"";
		if(index[0]>26)name="";
		return name+color;
	}
	public void fapai(){
		RoomBean roomBean = new RoomBean();
		UserBean user1 = new UserBean();
		user1.setUserid(1);
		UserBean user2 = new UserBean();
		user2.setUserid(2);
		UserBean user3 = new UserBean();
		user3.setUserid(3);
		roomBean.getGame_userlist().add(user1);
		roomBean.getGame_userlist().add(user2);
		roomBean.getGame_userlist().add(user3);
		roomBean.setBanker(1);
		roomBean.Initialization();
		roomBean.Deal();
		System.out.println(roomBean.getBrands().size() );
		for(int index:roomBean.getBrands()){
			System.out.print(getBrand_str(index)+"-");
		}
	}
	public static void main(String[] args) {
		new Test().End_Hu();
	}
}
*/