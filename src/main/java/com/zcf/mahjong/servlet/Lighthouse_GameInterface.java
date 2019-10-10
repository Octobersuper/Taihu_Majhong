package com.zcf.mahjong.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.zcf.mahjong.bean.UserBean;
import com.zcf.mahjong.dao.M_LoginDao;
import com.zcf.mahjong.dao.Mg_GameDao;
import com.zcf.mahjong.mahjong.Public_State;
import com.zcf.mahjong.util.BaseDao;
import com.zcf.mahjong.util.UtilClass;

/**
 * 前端接口
 * 
 * @author Administrator
 *
 */
@WebServlet("/Lighthouse_GameInterface")
public class Lighthouse_GameInterface extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Map<String, Object> returnMap = new HashMap<String, Object>();
	private Gson gson = new Gson();

	public Lighthouse_GameInterface() {
		super();
	}

	public void destroy() {
		super.destroy();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 输出转码
		response.setContentType("text/json;charset=UTF-8");
		// 接受转码
		request.setCharacterEncoding("UTF-8");
		String type = request.getParameter("type");
		/* 允许跨域的主机地址 */
		response.setHeader("Access-Control-Allow-Origin", "*");
		/* 允许跨域的请求方法GET, POST, HEAD 等 */
		response.setHeader("Access-Control-Allow-Methods", "*");
		/* 重新预检验跨域的缓存时间 (s) */
		response.setHeader("Access-Control-Max-Age", "3600");
		/* 允许跨域的请求头 */
		response.setHeader("Access-Control-Allow-Headers", "*");
		/* 是否携带cookie */
		response.setHeader("Access-Control-Allow-Credentials", "true");
		BaseDao baseDao = new BaseDao();
		//接口
		Mg_GameDao mg_GameDao = new Mg_GameDao(baseDao);
		// 接收参数(解密后返回)
		Map<String, Object> requestmap = null;
		returnMap.clear();
		if (type != null) {
			// 接收参数(解密后返回)
			requestmap = UtilClass.utilClass.getRequestAdd(request, "/request.properties", type);
			System.out.println(requestmap.toString());
			// 检测参数是否合格(不合格则会更改type的值并增加返回map错误提示error)
			UtilClass.utilClass.isRequest("/request.properties", type, requestmap, returnMap);
			if (returnMap.get("error") != null) {
				type = "";
			}
		} else {
			returnMap.put("error", "参数错误");
			returnMap.put("state", "-1");
			type = "";
		}
		// 登录
		if ("login".equals(type)) {
			M_LoginDao loginDao = new M_LoginDao(baseDao);
			Map<String, Object> map = new HashMap<String, Object>();
			UserBean userBean = loginDao.getUser(requestmap);
			if (userBean == null) {
				// 添加此用户
				String state = loginDao.adduser(requestmap);
				if (!state.equals("success")) {
					// 记录错误日志
					// 异常
					returnMap.put("state", "999");
				} else {
					// 重新查询此用户
					userBean = loginDao.getUser(requestmap.get("openid").toString());
				}
			}
			if (userBean.getState() == 1) {
				// 账号被封
				map.put("state", "101");
				returnMap.put("user", map);
			} else {
				returnMap.put("state", "0");
				userBean.getUser_Custom("nickname-avatarurl-userid-diamond-openid-sex", returnMap);
				if(Public_State.clients.get(userBean.getOpenid())!=null){
					returnMap.put("state", "320"); //重复登录
				}
				// 检测是否需要重连
				String roomno = Public_State.ISUser_Room(userBean.getUserid());
				// 加入登陆日志
				// loginDao.addLogin_Log(userBean.getUserid());
				// 当天首次登陆领取日常任务
				//loginDao.addDaily_User(userBean.getUserid());
				// 需要重连
				if (roomno != null) {
					returnMap.put("state", "310");
					returnMap.put("roomno", roomno);
				}
				System.out.println(returnMap);
			}
		}

		// 查看玩家信息
		if ("getUser".equals(type)) {
			returnMap.put("type_s", type);
			int userid = Integer.parseInt(request.getParameter("userid"));
			returnMap.put("UserBean", mg_GameDao.getUserById(userid));
		}

		// 查看金币商品1
		if ("ckjin".equals(type)) {
			returnMap.put("type_s", type);
			returnMap.put("ckjin", mg_GameDao.ckjin());
		}

		// 查看钻石商品1
		if ("ckzuan".equals(type)) {
			returnMap.put("type_s", type);
			returnMap.put("ckzuan", mg_GameDao.ckzuan());
			returnMap.put("service",mg_GameDao.service());
		}

		// 查看客服信息1
		if ("ckservice".equals(type)) {
			returnMap.put("type_s",type);
			returnMap.put("ckservice", mg_GameDao.ckservice());
		}
		// 查看当日任务1
		if ("getdaily".equals(type)) {
			returnMap.put("type_s",type);
			int userid = Integer.parseInt(request.getParameter("userid"));
			returnMap.put("getdaily", mg_GameDao.getdaily(userid));
		}

		// 查看公告1
		if ("ckgong".equals(type)) {
			returnMap.put("type_s",type);
			returnMap.put("ckgong", mg_GameDao.ckgong());
		}

		// 查看系统消息1
		if ("cksystem".equals(type)) {
			returnMap.put("type_s",type);
			returnMap.put("cksystem", mg_GameDao.cksystem());
		}

		// 签到1
		if ("sign".equals(type)) {
			returnMap.put("type_s",type);
			returnMap.put("state", mg_GameDao.sign(Integer.parseInt(request.getParameter("userid")),
					Integer.parseInt(request.getParameter("signid"))));
		}
		// 查看签到记录1
		if ("signrecord".equals(type)) {
			returnMap.put("type_s", type);
			returnMap.put("signrecord", mg_GameDao.signrecord(Integer.parseInt(request.getParameter("userid"))));
			returnMap.put("awardlist", mg_GameDao.cksign());
		}
		/***************************  分享  ****************************/
		if("getShare".equals(type)){
			returnMap.put("share", mg_GameDao.getShare());
		}
		// 查看轮播图1
		if ("cklun".equals(type)) {
			returnMap.put("cklun", mg_GameDao.cklun());
		}
		// 提现1
		if ("withdraw".equals(type)) {
			int money = Integer.parseInt(request.getParameter("money"));
			int id = Integer.parseInt(request.getParameter("userid"));
			String account = request.getParameter("account");
			String realname = request.getParameter("realname");
			returnMap.put("state", mg_GameDao.withdraw(money, account, realname, id));
		}
		// 查看幸运转盘消耗金币数及规则1
		if ("ckluck".equals(type)) {
			returnMap.put("type_s", type);
			returnMap.put("ckluck", mg_GameDao.ckluck());
		}

		// 查看幸运转盘奖励1
		if ("ckluckpro".equals(type)) {
			returnMap.put("type_s", type);
			returnMap.put("ckluckpro", mg_GameDao.ckluckpro());
		}
		// 幸运转盘抽奖1
		if ("lottery".equals(type)) {
			returnMap.put("type_s", type);
			returnMap.put("ratio", Integer.parseInt(request.getParameter("ratio")));
			returnMap.put("award", mg_GameDao.lottery(Integer.parseInt(request.getParameter("userid")),
					Integer.parseInt(request.getParameter("ratio"))));
		}

		// 查看兑奖商品1
		if ("ckdui".equals(type)) {
			returnMap.put("type_s", type);
			returnMap.put("ckdui", mg_GameDao.ckdui());
		}

		// 兑奖1
		if ("conversion".equals(type)) {
			int userid = Integer.parseInt(request.getParameter("userid"));
			int conversionid = Integer.parseInt(request.getParameter("conversionid"));
			String phone = request.getParameter("phone");
			returnMap.put("state", mg_GameDao.conversion(userid, conversionid, phone));
		}

		// 查看游戏规则1
		if ("ckgui".equals(type)) {
			returnMap.put("type_s", type);
			returnMap.put("ckgui", mg_GameDao.ckgui());
		}
		//查看活动
        if("getActivily".equals(type)){
            returnMap.put("type_s", type);
            returnMap.put("author",mg_GameDao.getActivily());
        }
        //查看招募代理
		if("getAgency".equals(type)){
			returnMap.put("type_s", type);
			returnMap.put("author",mg_GameDao.getAgency());
		}

		//查看
		/*********************************  商城购买待处理 ****************************************/
		if("byMoney".equals(type)){
			returnMap.put("byMoney", mg_GameDao.byMoney());
		}
		if("byDiamond".equals(type)){
			returnMap.put("byDiamond", mg_GameDao.byDiamond());
		}
		/*************************************** 牌友圈 ***************************************/

		//添加俱乐部房卡
		if ("addClub_Money".equals(type)) {
			returnMap.put("type_s", type);
			String circlenumber = request.getParameter("circlenumber");
			int userid = Integer.parseInt(request.getParameter("userid"));
			int diamond = Integer.parseInt(request.getParameter("diamond"));
			returnMap.put("state", mg_GameDao.addClubDiamond(circlenumber,userid,diamond));
		}

		// 创建牌友圈--参数: 牌友圈名称,用户id
		if ("createClub".equals(type)) {
			String circlename = request.getParameter("circlename");
			int userid = Integer.parseInt(request.getParameter("userid"));
			returnMap.put("state", mg_GameDao.createClub(circlename, userid));
		}

		// 查看已加入的牌友圈列表1
		if ("ckclub".equals(type)) {
			returnMap.put("type_s", "ckclub");
			int userid = Integer.parseInt(request.getParameter("userid"));
			returnMap.put("ckclub", mg_GameDao.ckclub(userid));
		}

		// 查看牌友圈详情1
		if ("clubdetails".equals(type)) {
            returnMap.put("clubdetails", mg_GameDao.ckclubPlay(Integer.parseInt(request.getParameter("circlenumber"))));
            returnMap.put("type_s", type);
            returnMap.put("state", "-1");
		}

		// 申请加入牌友圈1
		if ("joinCircleid".equals(type)) {
			int circlenumber = Integer.parseInt(request.getParameter("circlenumber"));
			int userid = Integer.parseInt(request.getParameter("userid"));
			returnMap.put("state", mg_GameDao.joinCircleid(userid, circlenumber));
		}

		// 圈主查看牌友圈申请
		if ("circleApplication".equals(type)) {
			returnMap.put("type_s", type);
			returnMap.put("circleApplication", mg_GameDao.circleApplication(Integer.parseInt(request.getParameter("circlenumber")),
					Integer.parseInt(request.getParameter("userid"))));
		}

        // 同意加入
        if ("passjoinCard".equals(type)) {
            returnMap.put("state",
                    type+mg_GameDao.passjoinCard(
                            Integer.parseInt(request.getParameter("userid")),
                            Integer.parseInt(request.getParameter("applyid")),
                            Integer.parseInt(request.getParameter("circlenumber"))));
            returnMap.put("type_s", type);
        }

		// 拒绝加入
		if ("downjoinCard".equals(type)) {
			returnMap.put("state", type+mg_GameDao.downjoinCard(Integer.parseInt(request.getParameter("applyid"))));
		}

		// 查看牌友圈的成员
		if ("selectcarduser".equals(type)) {
			returnMap.put("type_s", type);
			int circlenumber = Integer.parseInt(request.getParameter("circlenumber"));
			returnMap.put("selectcardUser", mg_GameDao.selectcarduser(circlenumber));
		}

        // 查看俱乐部房间列表
        if ("selectclubroom".equals(type)) {
            returnMap.put("type_s", type);
            int clubid = Integer.parseInt(request.getParameter("clubid"));
            returnMap.put("selectclubroom", mg_GameDao.selectclubroom(clubid));
        }

		// 踢出牌友圈
		if ("downcricle".equals(type)) {
			// 牌友-牌友圈绑定id
			int id = Integer.parseInt(request.getParameter("id"));
			// 自身id
			int userid = Integer.parseInt(request.getParameter("userid"));
			// 牌友圈编号
			int circlenumber = Integer.parseInt(request.getParameter("circlenumber"));
			returnMap.put("state", mg_GameDao.downcricle(id, userid, circlenumber));
		}

		/********************************** 我的战绩 *****************************************/

		// 查看战绩列表
		if ("selectPK".equals(type)) {
            List<Map<String, Object>> list = mg_GameDao.getRecordRoom(Integer.parseInt(request.getParameter("userid")));
            List<Map<String, Object>> lists = new ArrayList<Map<String,Object>>();
            int num = 0;
            for (int i = 0; i < list.size(); i++) {
                if(i+1 <list.size()){
                    if(list.get(i).get("roomno").equals(list.get(i+1).get("roomno")) && list.get(i).get("game_number").equals(list.get(i+1).get("game_number"))){
                        num++;
                    }else{
                        String userid = "",nickname="" ,number="";
                        for (int j = 0; j <= num; j++) {
                            if(j==num){
                                userid += list.get(i-num+j).get("userid");
                                nickname+=list.get(i-num+j).get("nickname");
                                number+=list.get(i-num+j).get("number");
                            }else{
                                userid += list.get(i-num+j).get("userid")+"|";
                                nickname+=list.get(i-num+j).get("nickname")+"|";
                                number+=list.get(i-num+j).get("number")+"|";
                            }
                        }
                        list.get(i-num).put("userid", userid);
                        list.get(i-num).put("nickname", nickname);
                        list.get(i-num).put("number", number);
                        lists.add(list.get(i-num));
                        num=0;
                    }
                }else{
                    String userid = "",nickname="" ,number="";
                    for (int j = 0; j <= num; j++) {
                        if(j==num){
                            userid += list.get(i-num+j).get("userid");
                            nickname+=list.get(i-num+j).get("nickname");
                            number+=list.get(i-num+j).get("number");
                        }else{
                            userid += list.get(i-num+j).get("userid")+"|";
                            nickname+=list.get(i-num+j).get("nickname")+"|";
                            number+=list.get(i-num+j).get("number")+"|";
                        }
                    }
                    list.get(i-num).put("userid", userid);
                    list.get(i-num).put("nickname", nickname);
                    list.get(i-num).put("number", number);
                    lists.add(list.get(i-num));
                    num=0;
                }

            }
            returnMap.put("type_s",type);
            returnMap.put("state","-1");
            returnMap.put("selectPK",lists);
		}

		// 查看战绩详情
		if ("getRecordDetails".equals(type)) {
			returnMap.put("getRecordDetails", mg_GameDao.getRecordDetails(request.getParameter("roomno")));
		}
		//查看牌友圈战绩详情
		if("getClubDetails".equals(type)){
			returnMap.put("getClubDetails", mg_GameDao.getClubDetails(Integer.parseInt(request.getParameter("circlenumber"))));
		}
	
		/********************************************************/
		baseDao.CloseAll();
		String json = gson.toJson(returnMap).toString();
		System.out.println(json);
		response.getWriter().println(json);
	}

	public void init() throws ServletException {

	}
	
}
