package com.zcf.mahjong.service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zcf.mahjong.bean.RoomBean;
import com.zcf.mahjong.bean.UserBean;
import com.zcf.mahjong.dao.M_LoginDao;
import com.zcf.mahjong.mahjong.Public_State;
import com.zcf.mahjong.util.BaseDao;
import com.zcf.mahjong.util.MahjongUtils;
import com.zcf.mahjong.util.MapHelper;
import com.zcf.mahjong.util.System_Mess;

import static com.zcf.mahjong.util.Mahjong_Util.mahjong_Util;

@ServerEndpoint("/Mahjong/{openid}")
public class Mahjong_Socket {
    private Gson gson = new Gson();// json转换
    public Session session;
    // 用户信息
    public UserBean userBean;
    // 自己进入的房间
    public RoomBean roomBean;
    private BaseDao baseDao = new BaseDao();
    // 登陆dao
    private M_LoginDao loginDao = new M_LoginDao(baseDao);
    // 游戏逻辑类
    public M_GameService gameService = new M_GameService(baseDao);
    private Map<String, Object> returnMap = new HashMap<String, Object>();

    /**
     * 连接
     *
     * @param openid
     * @param session
     * @throws IOException
     */
    @OnOpen
    public void onOpen(@PathParam("openid") String openid, Session session) {
        boolean bool = true;
        if (openid != null) {
            // 查询出用户信息t
            userBean = loginDao.getUser(openid);
            baseDao.CloseAll();
            if (userBean != null) {
                String openids = userBean.getOpenid();
                // 验证是否已经在线
                if (Public_State.clients.get(openids) != null) {
                    returnMap.put("type", "Repeat");
                    this.session = session;
                    sendMessageTo(returnMap);
                    System_Mess.system_Mess.ToMessagePrint(userBean
                            .getNickname() + "重复登陆");
                } else {
                    // 将自己放入客户端集合中
                    Public_State.clients.put(openids, this);
                    this.session = session;
                    System_Mess.system_Mess.ToMessagePrint(userBean
                            .getNickname() + "已连接(麻将)");
                    bool = false;
                }
            }
        }
        // 如果没走正常业务则归类非法连接
        if (bool) {
            try {
                session.close();
            } catch (IOException e) {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
                System.out.println("消息" + e.getMessage());
            }
            System_Mess.system_Mess.ToMessagePrint("非法连接");
        }
    }

    /**
     * 关闭
     *
     * @throws IOException
     */
    @OnClose
    public void onClose() {
        if (userBean != null) {
            // 删除自己
            Public_State.clients.remove(userBean.getOpenid() + "");
            // if(roomBean!=null){
            // //启动托管线程
            // userBean.setThread_robot(new Thread_Robot(roomBean, userBean,
            // gameService));
            // }
            // 标识自己已经掉线
            userBean.setExit_state(1);
            System_Mess.system_Mess.ToMessagePrint(userBean.getNickname()
                    + "断开连接");
            // 如果已加入房间则通知其他人自己退出
            Exit_Room();
        }
        if (roomBean != null) {
            int num = 0;
            for (UserBean user : roomBean.getGame_userlist()) {
                if (user.getExit_state() == 1) {
                    num++;
                }
            }
            if (num == roomBean.getGame_userlist().size()) {
                Exit_All();
            }
        }
    }

    /**
     * 接收消息
     *
     * @param message
     * @throws IOException
     * @throws InterruptedException
     */
    @OnMessage
    public void onMessage(String message) {
        session.setMaxIdleTimeout(60000);
        Lock lock = userBean.getLock();
        lock.lock();
        returnMap.clear();
        // 解密
        // message = new String(AES.decrypt(message));

        Map<String, String> jsonTo = gson.fromJson(message,
                new TypeToken<Map<String, String>>() {
                }.getType());
        if (!"heartbeat".equals(jsonTo.get("type"))) {
            System_Mess.system_Mess.ToMessagePrint("接收" + userBean.getNickname()
                    + "的消息" + message);
        }
        // 心跳连接

        if ("heartbeat".equals(jsonTo.get("type"))) {
            returnMap.put("type",
                    "heartbeat");
            sendMessageTo(returnMap);
        }

        // 创建房间
        if ("Establish".equals(jsonTo.get("type"))) {
            returnMap.put("type", "Establish");
            returnMap.put("exittype", 0);
            returnMap.put("state", "0");
            if (roomBean != null
                    && Public_State.PKMap.get(roomBean.getRoomno()) != null) {
                returnMap.put("state", "106");// 重复创建s
            } else {
                roomBean = gameService.Establish(jsonTo, userBean,
                        Integer.parseInt(jsonTo.get("clubid")));
                if (roomBean == null) {
                    returnMap.put("state", "105");// 钻石不足
                } else {
                    roomBean.getRoomBean_Custom(
                            "roomno-user_positions-gps-max_number-dihua_ords-piao", returnMap,
                            "userid-nickname-avatarurl-ip-number-sex");
                    sendMessageTo(returnMap, roomBean);
                }
            }
            sendMessageTo(returnMap);
        }
        // 快捷语音
        if ("voice".equals(jsonTo.get("type"))) {
            returnMap.put("voiceType", jsonTo.get("voiceType"));
            returnMap.put("info", jsonTo.get("info"));
            returnMap.put("userid", userBean.getUserid());
            returnMap.put("sex", userBean.getSex());
            returnMap.put("type", "voice");
            sendMessageTo(returnMap);
            sendMessageTo(returnMap, roomBean);
        }
        // 加入房间
        if ("Matching".equals(jsonTo.get("type"))) {
            returnMap.put("state", "0");
            int state = gameService.ISMatching_Money(userBean,
                    jsonTo.get("roomno"));
            if (state == 0) {
                roomBean = gameService.Matching(jsonTo, userBean);
                if (roomBean == null) {
                    returnMap.put("state", "104");// 房间已满
                } else {
                    // 将自己的信息推送给房间内其他玩家
                    userBean.getUser_Custom(
                            "userid-nickname-avatarurl-number-sex", returnMap);
                    roomBean.getRoomBean_Custom("user_positions-dihua_ords-piao", returnMap);
                    returnMap.put("type", "Matching_User");
                    sendMessageTo(returnMap, roomBean);
                    returnMap.clear();
                    returnMap.put("state", "0");
                    returnMap.put("exittype", 1);
                    roomBean.getRoomBean_Custom(
                            "roomno-max_person-user_positions-max_number-game_type-fen-dihua_ords-piao",
                            returnMap,
                            "userid-nickname-avatarurl-ready_state-number-sex");
                }
            } else {
                // 100=房间不存在114=重复加入106=需要定位
                returnMap.put("state", String.valueOf(state));
            }
            returnMap.put("type", "Matching");
            sendMessageTo(returnMap);
        }

        // 准备
        if ("ready".equals(jsonTo.get("type"))) {
            // 如果处于等待加入或准备阶段
            if (roomBean.getState() == 0 || roomBean.getState() == 1) {
                int count = gameService.Ready(userBean, roomBean);
                returnMap.put("type", "ready");
                if (count == roomBean.getMax_person()) {
                    returnMap.put("type", "startgame");
                    returnMap.put("exittype", 3);
                    // 执行游戏开始
                    int state = gameService.StartGame(roomBean);
                    if (state == 0) {
                        roomBean.getRoomBean_Custom(
                                "game_number-max_number-draw_s-draw-brands_count-banker",
                                returnMap, "brands-number-userid");
                    }
                } else {
                    returnMap.put("userid", userBean.getUserid());
                }
                Random random = new Random();
                List<Integer> num = new ArrayList<>();
                num.add(random.nextInt(6) + 1);
                num.add(random.nextInt(6) + 1);
                Collections.sort(num);
                returnMap.put("dice", num);
                roomBean.setDice(num);
                sendMessageTo(returnMap);
                sendMessageTo(returnMap, roomBean);
            }
        }
        // 摸牌
        if ("get_brand".equals(jsonTo.get("type"))) {// 流局
            if (roomBean.getBrands().size() == 0) {
                returnMap.put("type", "flow");
                // 设置流局
                roomBean.setFlow(1);
                // 流局重置
                roomBean.InItReady();
                // roomBean.Initialization();
            } else // 当牌剩下数量和玩家人数相等时分牌
                /*if (roomBean.getBrands().size() <= roomBean.getGame_userlist()
                        .size()) {
                    // 分牌
                    returnMap.put("brand_nextid", roomBean.getNextUserId());
                    returnMap.put("type", "share_brand");
                    returnMap.put("user_brands", roomBean.GrantBrand(userBean));
                } else {*/ {
                // 摸牌
                int brand = roomBean.getBrand_Random();
                userBean.getBrands().add(brand);
                // 检测是否可以补杠
                // returnMap.put("is_repair_bar",Mahjong_Util.mahjong_Util.IS_Repair_Bar_Bump(userBean,roomBean
                // .getDraw()));
                returnMap.put("userid", userBean.getUserid());
                returnMap.put("brand", brand);
                returnMap.put("type", "get_brand");
            }
               // }
            sendMessageTo(returnMap);
            sendMessageTo(returnMap, roomBean);
        }
        // 出牌
        if ("out_brand".equals(jsonTo.get("type"))) {
            // 0代表不是自己杠后出牌就清空
            if (roomBean.IS_Bar_Userid(userBean.getUserid()) == 0) {
                roomBean.RemoveAll_Bar();
            }
            roomBean.setGangnum(0);
            roomBean.setHucount(0);
            returnMap.put("type", "out_brand");
            int outbrand = Integer.parseInt(jsonTo.get("outbrand"));
            // 检测是否是花牌

            int value = mahjong_Util.getBrand_Value(outbrand);
            if (value == 27 || value == 61 || value == 95 || value == 31
                    || value == 65 || value == 133 || value == 32
                    || value == 66 || value == 134 || value == 33
                    || value == 67 || value == 135) { // 设置不可以点炮
                roomBean.setBuhua_baruserid(userBean.getUserid());
                userBean.getShow_brands().add(value);
                userBean.Remove_Brands(Integer.parseInt(jsonTo.get("outbrand")));
                roomBean.setGangnum(roomBean.getGangnum() + 1);
                returnMap.put("mtype", 1);
                roomBean.setCannon(1);
                returnMap.put("cannon", roomBean.getCannon());
                returnMap.put("outbrand", outbrand);
                returnMap.put("out_userid", userBean.getUserid());
                sendMessageTo(returnMap);
                sendMessageTo(returnMap, roomBean);
            } else {
                roomBean.setLastBrand(outbrand);
                roomBean.setLastUserid(userBean.getUserid());
                // 出的牌
                returnMap.put("outbrand", outbrand);
                // 出牌人id
                returnMap.put("out_userid", userBean.getUserid());
                // 出牌类型
                returnMap.put("out_type", 0);

                roomBean.setEnd_userid(userBean.getUserid());
                //检测是否有人可胡 可胡的话发送胡牌消息   0没人能胡  1有人能胡
                //List<UserBean> checkHu = roomBean.checkHu(outbrand, userBean);
                int checkHu2 = 0;
                roomBean.getHu_user_list().clear();
                for (UserBean user :
                        roomBean.getGame_userlist()) {
                    if (userBean.getUserid() != user.getUserid()) {
                        int s = mahjong_Util.checkHu(user, outbrand);
                        if (s != -1 && user.getIshuthis() == 0) {
                            checkHu2++;
                        }
                    }
                }
                System.err.println("checkHu2:" + checkHu2);
                if (checkHu2 == 0) {
                    int state = gameService.OutBrand(roomBean, outbrand, userBean, returnMap);
                    System_Mess.system_Mess.ToMessagePrint("碰牌状态" + state);
                    sendMessageTo(returnMap);
                    sendMessageTo(returnMap, roomBean);
                } else {
                    gameService.OutBrand(userBean, outbrand);
                    sendMessageTo(returnMap);
                    sendMessageTo(returnMap, roomBean);
                }
            }
        }
        // 吃
        if ("eat".equals(jsonTo.get("type"))) {
            // 出牌人 出的牌22
            Integer brand = Integer.parseInt(jsonTo.get("brand"));
            // 吃的牌11-22-33
            String brands = jsonTo.get("brands");
            String[] split = brands.split("-");
            int[] b = new int[split.length];
            // 获取对方用户
            UserBean user = roomBean.getUserBean(Integer.valueOf(jsonTo
                    .get("userid")));
            // 删除出牌用户的牌
            user.Remove_Brands_Out(brand);
            // 将牌放入吃牌的用户
            for (int i = 0; i < split.length; i++) {
                userBean.getEat_brands().add(Integer.valueOf(split[i]));
                if (Integer.valueOf(split[i]) != brand) {
                    userBean.getBrands().remove(Integer.valueOf(split[i]));
                }
                b[i] = Integer.valueOf(split[i]);
            }

            // 记录被吃碰
            Integer integer = user.getBeichipeng().get(userBean.getUserid());
            returnMap.put("ishuthis", "");
            if (integer == null) {
                integer = 0;
            }
            user.getBeichipeng().put(userBean.getUserid(), integer + 1);
            System.out.println(user.getBeichipeng().toString());
            if (integer != 0) {
                if (user.getBeichipeng().get(userBean.getUserid()) >= 3) {
                    user.setIshuthis(1);
                    returnMap.put("ishuthis", user.getUserid() + "");
                }
            }
            // 记录吃碰
            Integer integer2 = userBean.getChipeng().get(user.getUserid());
            if (integer2 == null) {
                integer2 = 0;
            }
            userBean.getChipeng().put(user.getUserid(), integer2 + 1);
            System.out.println(userBean.getChipeng().toString());
            if (integer2 != 0) {
                if (userBean.getChipeng().get(user.getUserid()) >= 3) {
                    userBean.setIshuthis(1);
                    if (returnMap.get("ishuthis") != ""
                            && returnMap.get("ishuthis") != null) {
                        returnMap.put("ishuthis", returnMap.get("ishuthis")
                                + "-" + userBean.getUserid());
                    } else {
                        returnMap.put("ishuthis", userBean.getUserid() + "");
                    }
                }
            }

            returnMap.put("type", "eat");
            returnMap.put("eatuserid", jsonTo.get("userid"));
            returnMap.put("brand", brand);
            returnMap.put("eatbrands", b);
            returnMap.put("userid", userBean.getUserid());
            // 清空吃牌信息
            roomBean.getNextMap().clear();
            roomBean.setEnd_userid(userBean.getUserid());
            sendMessageTo(returnMap);
            sendMessageTo(returnMap, roomBean);
        }

        // 推送发牌
        if ("is_hu".equals(jsonTo.get("type"))) {
            int state = Integer.parseInt(jsonTo.get("state"));
            userBean.setIs_hustate(state);
            int count = gameService.Is_Hu(roomBean, state,userBean);
            System.out.println("hucount:" + count);
            boolean bool = false;
            if (count == 3) {
                bool = true;
                System.out.println(3);
            }
            if (count == 2 && roomBean.getGame_userlist().size() == 3) {
                bool = true;
                System.out.println(2);
            }
            if (count == 1 && roomBean.getGame_userlist().size() == 2) {
                bool = true;
                System.out.println(1);
            }
            if (bool) {
                jsonTo.put("type", "brand_nextid");
                roomBean.setHucount(0);
            }
        }
        // 碰
        if ("bump".equals(jsonTo.get("type"))) {
            // 获取对方用户
            UserBean user = roomBean.getUserBean(Integer.valueOf(jsonTo
                    .get("userid")));
            // 记录被吃碰
            Integer integer = user.getBeichipeng().get(userBean.getUserid());
            returnMap.put("ishuthis", "");
            if (integer == null) {
                integer = 0;
            }
            user.getBeichipeng().put(userBean.getUserid(), integer + 1);
            System.out.println(user.getBeichipeng().toString());
            if (integer != 0) {
                if (user.getBeichipeng().get(userBean.getUserid()) >= 3) {
                    user.setIshuthis(1);
                    returnMap.put("ishuthis", user.getUserid() + "");
                }
            }
            // 记录吃碰
            Integer integer2 = userBean.getChipeng().get(user.getUserid());
            if (integer2 == null) {
                integer2 = 0;
            }
            userBean.getChipeng().put(user.getUserid(), integer2 + 1);
            System.out.println(userBean.getChipeng().toString());
            if (integer2 != 0) {
                if (userBean.getChipeng().get(user.getUserid()) >= 3) {
                    userBean.setIshuthis(1);
                    if (returnMap.get("ishuthis") != ""
                            && returnMap.get("ishuthis") != null) {
                        returnMap.put("ishuthis", returnMap.get("ishuthis")
                                + "-" + userBean.getUserid());
                    } else {
                        returnMap.put("ishuthis", userBean.getUserid() + "");
                    }
                }
            }

            int[] brands = gameService.Bump_Brand(userBean,
                    Integer.parseInt(jsonTo.get("userid")),
                    Integer.parseInt(jsonTo.get("brand")), roomBean);
            returnMap.put("type", "bump");
            returnMap.put("bumpuserid", jsonTo.get("userid"));
            returnMap.put("brand", Integer.parseInt(jsonTo.get("brand")));
            returnMap.put("brands", brands);
            returnMap.put("userid", userBean.getUserid());
            // 清空碰牌信息
            roomBean.getNextMap().clear();
            sendMessageTo(returnMap);
            sendMessageTo(returnMap, roomBean);
        }
        // 胡牌_点炮
        if ("endhu".equals(jsonTo.get("type"))) {
            // 点炮用户id
            int p_userid = Integer.parseInt(jsonTo.get("p_userid"));
            returnMap.put("type", "endhu");
            if (roomBean.getState() != 4) {//第一
                // 结算500=已经结算 501=等待别人胡牌操作 502=等待别人结算
                int state = gameService.End_Game(userBean, roomBean, p_userid, 1, Integer.valueOf(jsonTo.get(
                        "brand")));
                returnMap.put("state", String.valueOf(state));
                returnMap.put("hu_user_list", roomBean.getHu_id());
                System_Mess.system_Mess.ToMessagePrint("点炮状态" + state);
                // 成功结算
                if (state == 0) {
                    roomBean.setVictoryid(userBean.getUserid());
                    jsonTo.put("type", "end");
                }
            }
        }

        // 胡牌_自摸
        if ("endhu_this".equals(jsonTo.get("type"))) {
            returnMap.put("type", "endhu_this");
            // 检测自摸胡牌0不胡牌 900=4朴一将 890=1癞子3朴1将 880=1癞子4朴 缺将
            int state = gameService.End_Hu_This(userBean, roomBean,
                    Integer.parseInt(jsonTo.get("brand")));
            // 结算检测
            MahjongUtils mahjongUtils = new MahjongUtils(roomBean, userBean,
                    Integer.parseInt(jsonTo.get("brand")));
            int beishu = roomBean.getDihua_ords();/*
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
                    if (integer2 >= 3) {
                        sum = 3;
                    }
                    if (integer1 >= 3) {
                        sum = 3;
                    }
                    sum = sum * roomBean.getDihua_ords();
                    if (sum > beishu) {
                        beishu = sum;
                    }
                }
            }*/
            // 检测是否杠后开花
            int bar_Userid = roomBean.IS_Bar_Userid(userBean.getUserid());
            if (bar_Userid != 0) {
                // 杠后花
                userBean.setPower((8 * roomBean.getGangnum()) * beishu);
                userBean.getRecordMsgList().add("杠后开花+" + (8 * roomBean.getGangnum()) * beishu);
            } else {
                // 自摸 4分
                userBean.setPower(4 * beishu);
                userBean.getRecordMsgList().add("自摸+" + 4 * beishu);
            }

            Map<String, Integer> show = userBean.getShowBars();
            if (show.size() != 0) {
                int fan = 0;
                if (show.get("wanG") != 0) {
                    fan += (show.get("wanG") * 5) * beishu;
                }
                if (show.get("fengG") != 0) {
                    fan += (show.get("fengG") * 6) * beishu;
                }
                if (fan != 0) {
                    userBean.getRecordMsgList().add("明杠+" + fan);
                    userBean.setPower(fan);
                }
            }
            Map<String, Integer> hide = userBean.getHideBars();
            if (hide.size() != 0) {
                int fan = 0;
                if (hide.get("wanG") != 0) {
                    fan += (hide.get("wanG") * 10) * beishu;
                }
                if (hide.get("fengG") != 0) {
                    fan += (hide.get("fengG") * 12) * beishu;
                }
                if (hide.get("huaG") != 0) {
                    fan += (hide.get("huaG") * 12) * beishu;
                }
                if (fan != 0) {
                    userBean.getRecordMsgList().add("暗杠+" + fan);
                    userBean.setPower(fan);
                }
            }
            // 自摸
            mahjongUtils.getBrandKe(roomBean, userBean,
                    Integer.parseInt(jsonTo.get("brand")), 2, beishu);


            roomBean.setVictoryid(userBean.getUserid());
            // 自摸结算
            state = gameService.End_Game_This(userBean, roomBean);
            System_Mess.system_Mess.ToMessagePrint("自摸状态" + state);
            if (state == 0) {
                // 胡牌的人信息
                roomBean.getRoomBean_Custom("", returnMap,
                        "number-userid-dqnumber-recordMsgList");
                returnMap.put("userid", userBean.getUserid());
                returnMap.put("brand", Integer.parseInt(jsonTo.get("brand")));
                returnMap.put("state", String.valueOf(state));
                sendMessageTo(returnMap, roomBean);
                roomBean.addLog(returnMap);
                // 记录战绩
                gameService.addRecord(roomBean);
                roomBean.InItReady();
            } else {
                returnMap.put("state", "999");
            }
            sendMessageTo(returnMap);
        }
        /******************************************* 杠牌 ***************************************/
        // 明杠
        if ("show_bar".equals(jsonTo.get("type"))) {
            // 获取对方用户
            UserBean user = roomBean.getUserBean(Integer.valueOf(jsonTo
                    .get("userid")));
            // 记录被吃碰
            Integer integer = user.getBeichipeng().get(userBean.getUserid());
            returnMap.put("ishuthis", "");
            if (integer == null) {
                integer = 0;
            }
            user.getBeichipeng().put(userBean.getUserid(), integer + 1);
            System.out.println(user.getBeichipeng().toString());
            if (integer != 0) {
                if (user.getBeichipeng().get(userBean.getUserid()) >= 3) {
                    user.setIshuthis(1);
                    returnMap.put("ishuthis", user.getUserid() + "");
                }
            }
            // 记录吃碰
            Integer integer2 = userBean.getChipeng().get(user.getUserid());
            if (integer2 == null) {
                integer2 = 0;
            }
            userBean.getChipeng().put(user.getUserid(), integer2 + 1);
            System.out.println(userBean.getChipeng().toString());
            if (integer2 != 0) {
                if (userBean.getChipeng().get(user.getUserid()) >= 3) {
                    userBean.setIshuthis(1);
                    if (returnMap.get("ishuthis") != ""
                            && returnMap.get("ishuthis") != null) {
                        returnMap.put("ishuthis", returnMap.get("ishuthis")
                                + "-" + userBean.getUserid());
                    } else {
                        returnMap.put("ishuthis", userBean.getUserid() + "");
                    }
                }
            }


            roomBean.setGangnum(roomBean.getGangnum() + 1);
            roomBean.setShow_baruserid(userBean.getUserid());
            // 明杠
            gameService.Show_Bar(userBean,
                    Integer.parseInt(jsonTo.get("userid")),
                    Integer.parseInt(jsonTo.get("brand")), roomBean);
            returnMap.put("type", "show_bar");
            returnMap.put("userid", userBean.getUserid());
            returnMap.put("baruserid", jsonTo.get("userid"));
            returnMap.put("brand", Integer.parseInt(jsonTo.get("brand")));
            sendMessageTo(returnMap);
            sendMessageTo(returnMap, roomBean);
        }
        // 暗杠
        if ("hide_bar".equals(jsonTo.get("type"))) {
            roomBean.setGangnum(roomBean.getGangnum() + 1);
            roomBean.setHide_baruserid(userBean.getUserid());
            // 暗杠
            int[] brands = gameService.Hide_Bar(userBean,
                    Integer.parseInt(jsonTo.get("brand")), roomBean);
            returnMap.put("type", "hide_bar");
            returnMap.put("brands", brands);
            returnMap.put("userid", userBean.getUserid());
            sendMessageTo(returnMap);
            sendMessageTo(returnMap, roomBean);
        }
        // 杠癞子
        if ("draw_bar".equals(jsonTo.get("type"))) {
            // 设置不可以点炮
            roomBean.setCannon(1);
            returnMap.put("type", "draw_bar");
            returnMap.put("userid", userBean.getUserid());
            sendMessageTo(returnMap);
            sendMessageTo(returnMap, roomBean);
        }
        // 补杠
        if ("repair_bar_bump".equals(jsonTo.get("type"))) {
            roomBean.setGangnum(roomBean.getGangnum() + 1);
            roomBean.setRepair_baruserid(userBean.getUserid());
            gameService.Repair_Bar_Bump(userBean,
                    Integer.parseInt(jsonTo.get("brand")), roomBean);
            returnMap.put("type", "repair_bar_bump");
            returnMap.put("userid", userBean.getUserid());
            returnMap.put("brand", Integer.parseInt(jsonTo.get("brand")));
            sendMessageTo(returnMap);
            sendMessageTo(returnMap, roomBean);
        }
        /************************************ 房间操作 ****************************************/
        // 退出房间
        if ("exit_room".equals(jsonTo.get("type"))) {
            Exit_Room();
        }
        // 解散房间
        if ("exit_all".equals(jsonTo.get("type"))) {
            // 房间在等待加入阶段并且自己是房主才可以解散
            if (roomBean.getState() == 0
                    && roomBean.getHouseid() == userBean.getUserid()) {
                Exit_All();
            }
        }
        // 游戏开始解散
        if ("exit_game".equals(jsonTo.get("type"))) {// exit_game 1同意 2不同意
            returnMap.put("type", "exit_game");
            // 添加自己的解散状态
            userBean.setExit_game(Integer.parseInt(jsonTo.get("exit_game")));
            // 301发起302同意303不同意304解散
            int state = gameService.Exit_GameUser(userBean, roomBean);
            // 执行解散
            if (state == 304) {
                roomBean.setExit_game(0);
                roomBean.getRoomBean_Custom("", returnMap, "number-userid-nickname-avatarurl");
                returnMap.put("type", "Settlement");
                sendMessageTo(returnMap);
                sendMessageTo(returnMap, roomBean);
                returnMap.clear();
                Exit_All();
            } else {
                if (state == 301) {
                    roomTimer rt = new roomTimer(roomBean, 60, this);
                    rt.start();
                    roomBean.getRoomBean_Custom("userList", returnMap,
                            "userid-nickname-avatarurl-exit_game");
                    returnMap.put("applyuserid", userBean.getUserid());
                } else {
                    returnMap.put("userid", userBean.getUserid());
                }
                returnMap.put("state", String.valueOf(state));
                sendMessageTo(returnMap);
                sendMessageTo(returnMap, roomBean);
            }
        }
        // 断线重连
        if ("end_wifi".equals(jsonTo.get("type"))) {
            roomBean = Public_State.PKMap.get(jsonTo.get("roomno"));
            if (roomBean == null) {
                returnMap.put("state", "100");
            } else {
                returnMap.put("type", "con_wifi");
                returnMap.put("userid", userBean.getUserid());
                // 告知其他人我已经在线
                userBean = roomBean.getUserBean(userBean.getUserid());
                userBean.setExit_state(0);
                sendMessageTo(returnMap, roomBean);
                // 查询出房间信息返回
                roomBean.getRoomBean_Custom(
                        "cannon-dice-exit_game-user_log-end_userid-roomno-user_positions-brands_count-draw_s-draw-fen" +
                                "-fan-money-gps-banker-game_number-max_number-max_person-state-end_userid-game_type",
                        returnMap,
                        "userid-nickname-avatarurl-brands-eat_brands-bump_brands-show_brands-hide_brands-out_brands" +
                                "-ip-log_lat-number-is_hustate-hu_state-exit_game");
                // //关闭托管
                // userBean.getThread_robot().setBool(false);
            }
            returnMap.put("type", "end_wifi");
            sendMessageTo(returnMap);
        }

        // 获取位置距离
        if ("get_position".equals(jsonTo.get("type"))) {
            returnMap.put("type", "get_position");
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < roomBean.getGame_userlist().size(); i++) {
                UserBean user = roomBean.getGame_userlist().get(i);
                for (int j = i + 1; j < roomBean.getGame_userlist().size(); j++) {
                    UserBean user2 = roomBean.getGame_userlist().get(j);
                    double distance = MapHelper.GetPointDistance(
                            user.getLog_lat(), user2.getLog_lat());
                    String re = user.getUserid() + "-"
                            + user2.getUserid() + "-"
                            + String.valueOf(distance);
                    list.add(re);
                }
            }
            returnMap.put("list", list);
            returnMap.put("position", roomBean.getUser_positions());
            sendMessageTo(returnMap);
        }


        if ("Settlement".equals(jsonTo.get("type"))) {
            roomBean.getRoomBean_Custom("", returnMap, "number-userid-nickname-avatarurl");
            returnMap.put("type", "Settlement");
            sendMessageTo(returnMap);
        }

        // 弃胡
        if ("giveup_hu".equals(jsonTo.get("type"))) {
            userBean.setPass_hu(1);
            returnMap.put("type", "giveup_hu");
            sendMessageTo(returnMap);
            if (roomBean.getHu_user_list().size() <= 1) {
                int state = gameService.OutBrand(roomBean, roomBean.getLastBrand(),
                        roomBean.getUserBean(roomBean.getLastUserid()), returnMap);
                System_Mess.system_Mess.ToMessagePrint("碰牌状态" + state);
                if (state == 300) {
                    Map<String, Object> userMap = new HashMap<String, Object>();
                    for (UserBean user : roomBean.getGame_userlist()) {
                        if (user.getUserid() == roomBean.getNextUserId3(roomBean.getLastUserid())) {
                            // 检测下家是否可吃
                            int[] user_eat = mahjong_Util.IS_Eat(
                                    user.getBrands(), roomBean.getLastBrand());
                            if (user_eat != null) {
                                userMap.put("userid", user.getUserid());
                                userMap.put("eat", user_eat);
                                returnMap.put("type", "detectionEat");
                                returnMap.put("eat", userMap);
                                returnMap.put("eat_type", 1);
                                // 出的牌
                                returnMap.put("outbrand", roomBean.getLastBrand());
                                // 出牌人id
                                returnMap.put("out_userid", roomBean.getLastUserid());
                                // returnMap.put("type","canEat");
                                sendMessageTo(returnMap, roomBean);
                                sendMessageTo(returnMap);
                                roomBean.setNextMap(userMap);
                            } else {
                                jsonTo.put("type", "brand_nextid");
                                roomBean.getNextMap().clear();
                            }
                        }
                    }
                } else {
                    returnMap.put("type", "out_brand");
                    // 出的牌
                    returnMap.put("outbrand", roomBean.getLastBrand());
                    // 出牌人id
                    returnMap.put("out_userid", roomBean.getLastUserid());
                    // 出牌类型0正常出牌   1弃胡检测
                    returnMap.put("out_type", 1);
                    sendMessageTo(returnMap);
                    sendMessageTo(returnMap, roomBean);
                }
                roomBean.HuState_InIt();
                roomBean.getNextMap().clear();
            } else {
                roomBean.getHu_user_list().remove(userBean);
            }
        }
        if ("end".equals(jsonTo.get("type"))) {
            // 胡牌的人信息
            roomBean.getRoomBean_Custom_HU("", returnMap,
                    "number-userid-dqnumber-recordMsgList");
            // 点炮用户的信息
            roomBean.getUserBean(Integer.parseInt(jsonTo.get("p_userid")))
                    .getUser_Custom("userid-number-dqnumber-recordMsgList", returnMap);
            returnMap.put("type", "endhu");
            sendMessageTo(returnMap);
            sendMessageTo(returnMap, roomBean);
            roomBean.addLog(returnMap);
            // 记录战绩
            gameService.addRecord(roomBean);
            roomBean.InItReady();
            // roomBean.Initialization();
        }
        // 弃碰
        if ("giveup_bump2".equals(jsonTo.get("type"))) {
            int state = Integer.valueOf(jsonTo.get("code"));
            if (state == 1) {
                Integer outbrand = Integer.valueOf(jsonTo.get("outbrand"));// 出的牌
                String out_userid = jsonTo.get("out_userid");// 出牌人ID
                Map<String, Object> userMap = new HashMap<String, Object>();
                for (UserBean user : roomBean.getGame_userlist()) {
                    if (user.getUserid() == roomBean.getNextUserId3(Integer
                            .valueOf(out_userid))) {
                        // 检测下家是否可吃
                        int[] user_eat = mahjong_Util.IS_Eat(
                                user.getBrands(), outbrand);
                        if (user_eat != null) {
                            userMap.put("userid", user.getUserid());
                            userMap.put("eat", user_eat);
                            returnMap.put("type", "detectionEat");
                            returnMap.put("eat", userMap);
                            returnMap.put("eat_type", 1);
                            // 出的牌
                            returnMap.put("outbrand", outbrand);
                            // 出牌人id
                            returnMap.put("out_userid",
                                    Integer.valueOf(out_userid));
                            // returnMap.put("type","canEat");
                            //roomBean.setEnd_userid(user.getUserid());
                            sendMessageTo(returnMap, roomBean);
                            sendMessageTo(returnMap);
                            roomBean.setNextMap(userMap);
                        } else {
                            jsonTo.put("type", "brand_nextid");
                            roomBean.getNextMap().clear();
                        }
                    }
                }
            } else {
                jsonTo.put("type", "brand_nextid");
                roomBean.getNextMap().clear();
            }
        }
        // 弃碰
        if ("giveup_bump".equals(jsonTo.get("type"))) {
            jsonTo.put("type", "brand_nextid");
            roomBean.getNextMap().clear();
        }
        // 需要摸牌
        if ("brand_nextid".equals(jsonTo.get("type"))) {
            // 查找下一个摸牌用户
            returnMap.put("type", "brand_nextid");
            returnMap.put("brand_nextid", roomBean.getNextUserId());
            sendMessageTo(returnMap);
            sendMessageTo(returnMap, roomBean);
        }
        // 记录操作日志
        if (roomBean != null) {
            for (String value : Public_State.types) {
                if (value.equals(jsonTo.get("type"))) {
                    roomBean.addLog(returnMap);
                    break;
                }
            }
        }
        // 消息通道
        if ("message".equals(jsonTo.get("type"))) {
            returnMap.put("type", "message");
            returnMap.put("userid", userBean.getUserid());
            returnMap.put("text", jsonTo.get("text"));
            sendMessageTo(returnMap, roomBean);
        }
        baseDao.CloseAll();
        lock.unlock();
    }

    /***
     * 退出房间
     *
     * @throws IOException
     */
    public void Exit_Room() {
        returnMap.clear();
        // 已加入房间且房间在等待加入阶段则退出
        if (roomBean != null && roomBean.getState() == 0
                && roomBean.getHouseid() != userBean.getUserid()) {
            // 将自己从房间内清除
            roomBean.User_Remove(userBean.getUserid());
            returnMap.put("type", "exit");
            returnMap.put("userid", userBean.getUserid());
            sendMessageTo(returnMap, roomBean);
            roomBean = null;
            userBean.Initialization();
            // sendMessageTo(returnMap);
        } else if (roomBean != null && roomBean.getState() != 0) {
            // 告知其他人我已经掉线
            returnMap.put("type", "toUser_exit");
            returnMap.put("end_user", userBean.getUserid());
            sendMessageTo(returnMap, roomBean);
            // sendMessageTo(returnMap);
            // 房间存在且房间为开始且自己是房主的情况则解散房间
        } else if (roomBean != null && roomBean.getState() == 0
                && roomBean.getHouseid() == userBean.getUserid()) {
            Exit_All();
        }
        roomBean = null;
    }

    /***
     * 解散房间
     *
     * @throws IOException
     */
    public void Exit_All() {
        if (roomBean != null) {
            Public_State.PKMap.remove(roomBean.getRoomno());
            returnMap.put("type", "exit_all");
            sendMessageTo(returnMap);
            sendMessageTo(returnMap, roomBean);
            roomBean.Ready_InIt();
        }
        roomBean = null;
    }

    @OnError
    public void onError(Session session, Throwable error) {
        if (!"远程主机强迫关闭了一个现有的连接".equals(error.getMessage())
                && error.getMessage() != null) {
            error.printStackTrace();
            System_Mess.system_Mess.ToMessagePrint(userBean.getNickname()
                    + "异常" + error.getLocalizedMessage() + "***"
                    + error.getMessage());
        }
    }

    /**
     * 发送消息(房间所有人)
     *
     * @throws IOException
     */
    public synchronized void sendMessageTo(Map<String, Object> returnMap,
                                           RoomBean roomBean) {
        for (UserBean user : roomBean.getGame_userlist()) {
            Mahjong_Socket webSocket = (Mahjong_Socket) Public_State.clients
                    .get(user.getOpenid());
            if (webSocket != null && webSocket.session.isOpen()) {
                // 不等于自己的则发送消息
                if (!user.getOpenid().equals(userBean.getOpenid())) {
                    webSocket.sendMessageTo(returnMap);
                }
            }
            // else{
            // System.out.println("推送托管");
            // user.getThread_robot().setReturnMap(returnMap);
            // user.getThread_robot().setStatus(1);
            // }
        }
    }

    /**
     * 给自己返回信息
     *
     * @param returnMap
     * @throws IOException
     */
    public synchronized void sendMessageTo(Map<String, Object> returnMap) {
        if (session.isOpen()) {
            String returnjson = gson.toJson(returnMap).toString();
            // 加密
            // returnjson=AES.encrypt(returnjson);
            try {
                session.getBasicRemote().sendText(returnjson);
            } catch (IOException e) {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
            }
            if (!returnjson.contains("heartbeat")) {
                System_Mess.system_Mess.ToMessagePrint(userBean.getNickname()
                        + "返回消息(自己)" + returnjson);
            }
        }
    }

    /**
     * 给指定用户发送
     *
     * @param returnMap
     * @throws IOException
     */
    public synchronized void sendMessageTo(Map<String, Object> returnMap,
                                           String openid) throws IOException {
        Mahjong_Socket websocket = (Mahjong_Socket) Public_State.clients
                .get(openid);
        if (websocket != null && websocket.session.isOpen()) {
            String returnjson = gson.toJson(returnMap).toString();
            // 加密
            // returnjson=AES.encrypt(returnjson);
            websocket.session.getBasicRemote().sendText(returnjson);
            if (!returnjson.contains("heartbeat")) {
                System_Mess.system_Mess.ToMessagePrint(userBean.getNickname()
                        + "返回消息(自己)" + returnjson);
            }
        }
    }
}
