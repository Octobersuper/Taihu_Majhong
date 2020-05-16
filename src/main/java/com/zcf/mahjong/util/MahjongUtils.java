package com.zcf.mahjong.util;

import com.zcf.mahjong.bean.RoomBean;
import com.zcf.mahjong.bean.UserBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 检测胡牌牌型
 */
public class MahjongUtils {
    public static MahjongUtils mahjongUtil = new MahjongUtils();
    //玩家属性
    //private UserBean userBean;
    //房间属性
    //private RoomBean roomBean;
    //触发胡牌牌值
    //private int brand;
    private int sum = 1;

    public MahjongUtils() {

    }

    /**
     * 构造器
     */
    public MahjongUtils(RoomBean roomBean, UserBean userBean, int brand) {
        //this.userBean = userBean;
        //this.roomBean = roomBean;
        //this.brand = brand;
    }


    /**
     * 返回胡牌应加颗数
     *
     * @return
     */
    public void getBrandKe(RoomBean roomBean, UserBean userBean, Integer brand, int type,int sum) {
    	if(type == 1){
    		userBean.getBrands().add(brand);
    	}
    	this.sum = sum;
        //风刻子 1分 （ 风刻子即是：南、西、北其中三张同样的牌）； 28.29.30
        fkz(userBean);
        //1花1分，手中有三个同样的花算4分、四个同样的花6分（花牌暗杠则不再额外算花分）无花果牌型不算花分---------东、中、发、白为花牌
        yhyf(userBean);
        //碰碰胡  点炮5分、自摸10分；（胡牌时的牌型是AAA+AAA+AAA+AAA+AA的形式）
        pph(userBean,type);
        //清一色 5、点炮5分、自摸10分；
        qys(userBean,type);
        //小吊车 碰完杠完吃完手里的12张牌（必须有吃牌），余下一张吊将了，叫“小吊车”点炮5分、自摸10分；
        xdc(userBean,brand,type);
        //大吊车 （碰完杠完手里的12张牌，余下一张吊将了，叫“大吊车”。） 点炮10分、自摸20分；
        ddc(userBean,brand,type);
        //大七对  （AA+AA+AA+AA+AA+AAAA模式，其中AAAA是没开的暗杠） 点炮10分、自摸20分；
        boolean l = dqd(userBean,type,brand);
        if(!l){
            //小七对  （AA+AA+AA+AA+AA+AA+AA模式，最后胡牌是7个对子）点炮5分、自摸10分；
            xqd(userBean,brand,type);
        }
        //无花果  （手中没有东、中、发、白任意一张牌）自摸6分；
        if (type==2){
            whg(userBean);
        }
    }

    /**
     * 无花果  （手中没有东、中、发、白任意一张牌）  27 31 32 33
     * @param userBean
     * @return
     */
    private void whg(UserBean userBean) {
        List<Integer> userBrand = User_Brand_Value(userBean.getBrands());
        List<Integer> integers1 = User_Brand_Value(userBean.getShow_brands());
        for (Integer integer : integers1) {
            userBrand.add(integer);
        }
        List<Integer> integers2 = User_Brand_Value(userBean.getBump_brands());
        for (Integer integer : integers2) {
            userBrand.add(integer);
        }
        List<Integer> integers3 = User_Brand_Value(userBean.getHide_brands());
        for (Integer integer : integers3) {
            userBrand.add(integer);
        }
        List<Integer> integers4 = User_Brand_Value(userBean.getEat_brands());
        for (Integer integer : integers4) {
            userBrand.add(integer);
        }
        Collections.sort(userBrand);
        ArrayList<Integer> tempList = new ArrayList<>();
        if(integers1.size()==0 && integers3.size()==0){
            for (Integer brand: userBrand) {
                if (brand==27||brand==31||brand==32||brand==33){
                    tempList.add(brand);
                }
            }
        }else{
            tempList.add(1);
        }
        if (tempList.size()==0){
            userBean.setPower(6*sum);
			userBean.getRecordMsgList().add("无花果+"+2*sum);
        }

    }

    /**
     * 大七对  （AA+AA+AA+AA+AA+AAAA模式，其中AAAA是没开的暗杠） 大七对点炮10分、自摸20分；
     * @param userBean
     * @return
     */
    private boolean dqd(UserBean userBean,Integer type,Integer brand) {
    	 List<Integer> cards = User_Brand_Value(userBean.getBrands());
         //cards.add(getBrand_Value(brand));
         Collections.sort(cards);
         List<Integer> pan = new ArrayList<>();
         int count = 0;
         boolean islong = false;
         int sizhang = 0;
         for(int i=0;i<cards.size();i++){
             int num = 0;
             for(int j=i+1;j<cards.size();j++){
                 if(cards.get(j)==cards.get(i)){
                     if(!pan.contains(cards.get(i))){
                         count++;
                     }
                     pan.add(cards.get(i));
                     num++;
                 }
             }
             if(num == 3&&getBrand_Value(brand)==cards.get(i)){
                 islong=true;
             }
             if(num==3){
                 sizhang++;
             }
         }
         boolean no = false;
         if(count==5&&sizhang==2&&islong==true){
        	 no = true;
         }
         if(count==6&&islong==true) {
             no = true;
         }
         if(no){
        	 if (type==1){
                 userBean.setPower(10);
 				userBean.getRecordMsgList().add("大七对+10");
             }else{
                 userBean.setPower(20);
  				userBean.getRecordMsgList().add("大七对+20");
             }
        	 return true;
         }
         return false;
    }

    /**
     *  小七对（AA+AA+AA+AA+AA+AA+AA模式，最后胡牌是7个对子）
     * @param userBean
     * @param brand
     * @param type
     * @return
     */
    private boolean xqd(UserBean userBean, Integer brand, Integer type) {
    	List<Integer> userBrand = User_Brand_Value(userBean.getBrands());
        //userBrand.add(getBrand_Value(brand));
        if (userBrand.size() == 14) {
            Collections.sort(userBrand);
            boolean num = userBrand.get(0) == userBrand.get(1);
            boolean num1 = userBrand.get(2) == userBrand.get(3);
            boolean num2 = userBrand.get(4) == userBrand.get(5);
            boolean num3 = userBrand.get(6) == userBrand.get(7);
            boolean num4 = userBrand.get(8) == userBrand.get(9);
            boolean num5 = userBrand.get(10) == userBrand.get(11);
            boolean num6 = userBrand.get(12) == userBrand.get(13);
            if (num && num1 && num2 && num3 && num4 && num4 && num5 && num6) {
            	 if (type==1){
                     userBean.setPower(5*sum);
      				userBean.getRecordMsgList().add("小七对+"+5*sum);
                 }else{
                     userBean.setPower(10*sum);
      				userBean.getRecordMsgList().add("小七对+"+10*sum);
                 }
            	 return true;
            }
        }
        return false;
    }

    /**
     * 大吊车 （碰完杠完手里的12张牌，余下一张吊将了，叫“大吊车”。）
     * @param userBean
     * @param type
     * @param brand
     */
    private void ddc(UserBean userBean, Integer brand, Integer type) {
        List<Integer> eatList = User_Brand_Value(userBean.getEat_brands());
        if (eatList.size()==0){
            List<Integer> userBrand = User_Brand_Value(userBean.getBrands());
            if (userBrand.size()==2&&userBrand.get(0)==getBrand_Value(brand)){
                if (type==1){
                    userBean.setPower(10*sum);
     				userBean.getRecordMsgList().add("大吊车+"+10*sum);
                }else{
                    userBean.setPower(20*sum);
     				userBean.getRecordMsgList().add("大吊车+"+20*sum);
                }
            }
        }
    }

    /**
     * 小吊车 碰完杠完吃完手里的12张牌（必须有吃牌），余下一张吊将了，叫“小吊车”
     * @param userBean
     * @param type
     * @return
     */
    private void xdc(UserBean userBean, Integer brand, Integer type) {
        List<Integer> eatList = User_Brand_Value(userBean.getEat_brands());
        if (eatList.size()>0){
            List<Integer> userBrand = User_Brand_Value(userBean.getBrands());
            if (userBrand.size()==2&&userBrand.get(0)==getBrand_Value(brand)){
                if (type==1){
                    userBean.setPower(5*sum);
     				userBean.getRecordMsgList().add("小吊车+"+5*sum);
                }else{
                    userBean.setPower(10*sum);
     				userBean.getRecordMsgList().add("小吊车+"+10*sum);
                }
            }
        }
    }

    /**
     * 清一色  没有 27-33
     * @param userBean
     * @param type
     */
    private void qys(UserBean userBean, int type) {
        
    	 //手牌整合
        List<Integer> userBrand = User_Brand_Value(userBean.getBrands());
        for (Integer integer : User_Brand_Value(userBean.getShow_brands())) {
        	if(integer!=27&&integer!=31&&integer!=32&&integer!=33){
                userBrand.add(integer);
        	}
        }
        for (Integer integer : User_Brand_Value(userBean.getBump_brands())) {
            userBrand.add(integer);
        }
        for (Integer integer : User_Brand_Value(userBean.getHide_brands())) {
            userBrand.add(integer);
        }
        for (Integer integer : User_Brand_Value(userBean.getEat_brands())) {
            userBrand.add(integer);
        }
        boolean no = false;
        //去重
        List<Integer> newList = new ArrayList<>();
        for (Integer ban : userBrand) {
            if (!newList.contains(ban)) {
                newList.add(ban);
            }
        }
        //排序
        Collections.sort(userBrand);
        //创建万牌集合
        List<Integer> wanList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            wanList.add(i);
        }
        //判断
        if (wanList.containsAll(newList)) {
            no = true;
        }
        //创建筒牌集合
        List<Integer> tongList = new ArrayList<>();
        for (int i = 9; i < 18; i++) {
            tongList.add(i);
        }
        //判断
        if (tongList.containsAll(newList)) {
            no = true;
        }
        //创建条牌集合
        List<Integer> tiaoList = new ArrayList<>();
        for (int i = 18; i < 27; i++) {
            tiaoList.add(i);
        }
        //判断
        if (tiaoList.containsAll(newList)) {
            no = true;
        }
        if(no){
        	if (type==1){
                userBean.setPower(5*sum);
 				userBean.getRecordMsgList().add("清一色+"+5*sum);
            }else{
                userBean.setPower(10*sum);
 				userBean.getRecordMsgList().add("清一色+"+10*sum);
            }
        }
    }

    /**
     * 碰碰胡  点炮5分、自摸10分；（胡牌时的牌型是AAA+AAA+AAA+AAA+AA的形式）
     * @param userBean
     * @param type
     * @return
     */
    private void pph(UserBean userBean, int type) {
        if(userBean.getEat_brands().size()!=0 || userBean.getBrands().size()<=2){
            return;
        }
        List<Integer> userBrands = User_Brand_Value(userBean.getBrands());
        for (Integer integer : User_Brand_Value(userBean.getShow_brands())) {
            if(integer!=27&&integer!=31&&integer!=32&&integer!=33){
                userBrands.add(integer);
            }
        }
        for (Integer integer : User_Brand_Value(userBean.getBump_brands())) {
            userBrands.add(integer);
        }
        for (Integer integer : User_Brand_Value(userBean.getHide_brands())) {
            userBrands.add(integer);
        }
        Collections.sort(userBrands);

        if(userBean.getHide_brands().size()==0){
            List<Integer> th = new ArrayList<>();
            List<Integer> two = new ArrayList<>();
            for (Integer card:userBrands) {
                int t3 = 0;
                for (Integer c:userBrands) {
                    if(card.equals(c)){
                        t3++;
                    }
                }
                if (t3>=3){
                    th.add(card);
                }else if(t3==2){
                    two.add(card);
                }else{
                    return;
                }
            }

            //去重
            List<Integer> newList = new ArrayList<>();
            for (Integer ban : th) {
                if (!newList.contains(ban)) {
                    newList.add(ban);
                }
            }
            //去重
            List<Integer> two2 = new ArrayList<>();
            for (Integer ban : two) {
                if (!two2.contains(ban)) {
                    two2.add(ban);
                }
            }
            if(newList.size()==4&&two2.size()==1){
                if (type==1){
                    userBean.setPower(5*sum);
                    userBean.getRecordMsgList().add("碰碰胡+"+5*sum);
                }else{
                    userBean.setPower(10*sum);
                    userBean.getRecordMsgList().add("碰碰胡+"+10*sum);
                }
            }
        }
    }

    /**
     * 1花1分，手中有三个同样的花算4分、四个同样的花 6分（花牌暗杠则不再额外算花分）无花果牌型不算花分---------东、中、发、白为花牌
     * @param userBean  27  31  32  33
     * @return
     */
    private void yhyf(UserBean userBean) {
        List<Integer> userBrand = User_Brand_Value(userBean.getBrands());
        
        List<Integer> integers1 = User_Brand_Value(userBean.getShow_brands());
        for (Integer integer : integers1) {
            userBrand.add(integer);
        }
        
        List<Integer> integers2 = User_Brand_Value(userBean.getBump_brands());
        for (Integer integer : integers2) {
            userBrand.add(integer);
        }
        
        List<Integer> integers3 = User_Brand_Value(userBean.getHide_brands());
        for (Integer integer : integers3) {
            userBrand.add(integer);
        }
        
        List<Integer> integers4 = User_Brand_Value(userBean.getEat_brands());
        for (Integer integer : integers4) {
            userBrand.add(integer);
        }
        Collections.sort(userBrand);
        int dcount=0;
        int zcount=0;
        int fcount=0;
        int bcount=0;
        List<Integer> hua = new ArrayList<>();
        for (Integer brand : userBrand) {
            if (brand==27){
                dcount++;
                hua.add(brand);
            }
            if (brand==31){
                zcount++;
                hua.add(brand);
            }
            if (brand==32){
                fcount++;
                hua.add(brand);
            }
            if (brand==33){
                bcount++;
                hua.add(brand);
            }
        }
        //东东东 白白白 发发 红   10花
        int i = 0;

        if (dcount==3){
            i += 4*sum;
        }else if (dcount==4){
            i += 6*sum;
        }else{
            i += dcount*sum;
        }

        if (zcount==3){
            i += 4*sum;
        }else if (zcount==4){
            i += 6*sum;
        }else{
            i += zcount*sum;
        }

        if (fcount==3){
            i += 4*sum;
        }else if (fcount==4){
            i += 6*sum;
        }else{
            i += fcount*sum;
        }

        if (bcount==3){
            i += 4*sum;
        }else if (bcount==4){
            i += 6*sum;
        }else{
            i += bcount*sum;
        }
        userBean.setPower(i);

        userBean.setHuafen(i);
        /*if (dcount==3){
            userBean.setPower((4+s-dcount)*sum);
            i += (4+s-dcount)*sum;
            state=1;
        }else if (dcount==4){
            userBean.setPower((6+s-dcount)*sum);
            i += (6+s-dcount)*sum;
            state=1;
        }

        if (zcount==3){
            userBean.setPower((4+s-zcount)*sum);
            i += (4+s-zcount)*sum;
            state=1;
        }else if (zcount==4){
            userBean.setPower((6+s-zcount)*sum);
            i += (6+s-zcount)*sum;
            state=1;
        }
        if (fcount==3){
            userBean.setPower((4+s-fcount)*sum);
            i += (4+s-fcount)*sum;
            state=1;
        }else if (fcount==4){
            userBean.setPower((6+s-fcount)*sum);
            i += (6+s-fcount)*sum;
            state=1;
        }
        if (bcount==3){
            userBean.setPower((4+s-bcount)*sum);
            i += (4+s-bcount)*sum;
            state=1;
        }else if (bcount==4){
            userBean.setPower((6+s-bcount)*sum);
            i += (6+s-bcount)*sum;
            state=1;
        }

        if(state==0){
            i = s*sum;
        }
        userBean.setHuafen(i);*/
    }

    /**
     * 是否存在风刻子 （ 风刻子即是：南、西、北其中三张同样的牌）； 28.29.30
     * @param userBean
     * @return
     */
    private void fkz(UserBean userBean) {
        List<Integer> userBrand = User_Brand_Value(userBean.getBrands());
        List<Integer> integers1 = User_Brand_Value(userBean.getShow_brands());
        for (Integer integer : integers1) {
            userBrand.add(integer);
        }
        List<Integer> integers2 = User_Brand_Value(userBean.getBump_brands());
        for (Integer integer : integers2) {
            userBrand.add(integer);
        }
        List<Integer> integers3 = User_Brand_Value(userBean.getHide_brands());
        for (Integer integer : integers3) {
            userBrand.add(integer);
        }
        List<Integer> integers4 = User_Brand_Value(userBean.getEat_brands());
        for (Integer integer : integers4) {
            userBrand.add(integer);
        }
        Collections.sort(userBrand);
            int ncount=0;
            int xcount=0;
            int bcount=0;
        for (Integer brand : userBrand) {
            if (brand==28){
                ncount++;
            }
            if (brand==29){
                xcount++;
            }
            if (brand==30){
                bcount++;
            }
        }
        int feng = 0;
        if (ncount==3){
            feng += 1;
        }
        if (xcount==3){
            feng += 1;
        }
        if (bcount==3){
            feng += 1;
        }
        if(feng*sum!=0){
            userBean.setPower(feng*sum);
            userBean.getRecordMsgList().add("风刻子+"+feng*sum);
        }
    }
/*******************************工具类*******************************/
    /**
     * 排序（升序）[万筒条东南西北中发白]
     * @param list
     */
    public void Order_Brands(List<Integer> list){
        //升序
        Collections.sort(list);
    }
    /**
     * 获取单幅牌
     * @param index
     * @return
     */
    public int getBrand_Value(int index){
        return index-34*(index/34);
    }
    /**
     * 获取单张牌值和花色
     * @param index
     * @return
     */
    public int[] ISUser_Mahjong(int index){
        int[] indexs = new int[2];
        int color = -1;
        //获取单幅
        int value = getBrand_Value(index);
        //小于27的是万筒条
        if(value<27){
            //获取牌值ֵ
            index = value-9*(value/9);
            //获取花色
            color = (value/9);
        }else{
            color = value;
        }
        indexs[0] = index;
        indexs[1] = color;
        return indexs;
    }

    /**
     * 转换牌值
     * @return
     */
    public List<Integer> User_Brand_Value(List<Integer> user_Brand){
        List<Integer> list = new ArrayList<Integer>();
        for(int brand:user_Brand){
            list.add(getBrand_Value(brand));
        }
        return list;
    }

    /**
     * 检测不完整的朴
     * @param brands
     * @return
     */
    private boolean IS_DrawBrands(int[] brands){
        int[] brand = new int[2];
        int count=0;
        for(int i=0;i<brands.length;i++){
            if(brands[i]==-1)continue;
            if(count==2)return false;
            brand[count]=brands[i];
            count++;
        }
        int[] brand_value = ISUser_Mahjong(brand[0]);
        int[] brand_value2 = ISUser_Mahjong(brand[1]);
        //AB
        if((brand_value[0]+1)==brand_value2[0]&&brand_value[1]==brand_value2[1]){
            return true;
        }
        //AC
        if((brand_value[0]+2)==brand_value2[0]&&brand_value[1]==brand_value2[1]){
            return true;
        }
        //AA
        if(brand_value[0]==brand_value2[0]&&brand_value[1]==brand_value2[1]){
            return true;
        }
        return false;
    }


    /**
     * 检测将
     * @return
     */
    public int ISDouble_Count(int[] brands){
        int count =0;
        for(int i=0;i<brands.length;i++){
            if(brands[i]==-1)continue;
            if((i+1)<brands.length&&brands[i]==brands[i+1]){
                brands[i]=-1;
                brands[i+1]=-1;
                count++;
                if(count==1){
                    break;
                }
            }
        }
        return count;
    }
    /**
     * 检测朴（123）
     * @return
     */
    public int ISPave_Count(int[] brands,int type){
        int pave_count=0;
        for(int i=0;i<brands.length;i++){
            //不检测癞子
            if(brands[i]==-1)continue;
            if(IS_Brands_A(brands, brands[i])){
                brands[i]=-1;
                pave_count++;
                if(type==1)return pave_count;
            }
        }
        return pave_count;
    }
    /**
     * 检测朴（321）
     * @return
     */
    public int ISPave_Count_A(int[] brands,int type){
        int pave_count=0;
        for(int i=brands.length-1;i>0;i--){
            //不检测癞子
            if(brands[i]==-1)continue;
            if(IS_Brands_CBA(brands, brands[i])){
                brands[i]=-1;
                pave_count++;
                if(type==1)return pave_count;
            }
        }
        return pave_count;
    }
    /**
     * 检测朴(321)
     * @param brands
     * @param index
     * @return
     */
    private boolean IS_Brands_CBA(int[] brands,int index){
        int brand_index=-1;
        int[] brand_t = ISUser_Mahjong(index);
        for(int i=brands.length-1;i>0;i--){
            if(brands[i]==-1)continue;
            int[] brand = ISUser_Mahjong(brands[i]);
            //牌值大1且花色相同的牌
            if((brand_t[0]-1)==brand[0]&&brand_t[1]==brand[1]){
                brand_index=i;
            }
            //牌值大2且花色相同的牌
            if((brand_t[0]-2)==brand[0]&&brand_t[1]==brand[1]){
                if(brand_index!=-1){
                    brands[brand_index]=-1;
                    brands[i]=-1;
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * 检测朴(123)
     * @param brands
     * @param index
     * @return
     */
    private boolean IS_Brands_A(int[] brands,int index){
        int brand_index=-1;
        int[] brand_t = ISUser_Mahjong(index);
        for(int i=0;i<brands.length;i++){
            if(brands[i]==-1)continue;
            int[] brand = ISUser_Mahjong(brands[i]);
            //牌值大1且花色相同的牌
            if((brand_t[0]+1)==brand[0]&&brand_t[1]==brand[1]){
                brand_index=i;
            }
            //牌值大2且花色相同的牌
            if((brand_t[0]+2)==brand[0]&&brand_t[1]==brand[1]){
                if(brand_index!=-1){
                    brands[brand_index]=-1;
                    brands[i]=-1;
                    return true;
                }
            }
        }

        return false;
    }
    /**
     * 检测朴（111）
     * @param brands
     * @return
     */
    public int ISPave_Count_s(int[] brands,int type){
        int pave_count = 0;
        for(int i=0;i<brands.length;i++){
            if(brands[i]==-1)continue;
            if(IS_Brands_B(brands, brands[i])){
                brands[i]=-1;
                pave_count++;
                if(type==1)return pave_count;
            }
        }
        return pave_count;
    }
    /**
     * 检测朴（AAAA）
     * @param brands
     * @param index
     * @return
     */
    private boolean IS_Brands_B(int[] brands,int index){
        int brand_index=0;
        for(int i=0;i<brands.length;i++){
            if(brands[i]==index){
                brand_index++;
            }
        }
        if(brand_index>2){
            Remove_index(brands, index);
            return true;
        }
        return false;
    }
    /**
     * 删除指定牌值
     * @param brands
     * @param index
     */
    private void Remove_index(int[] brands,int index){
        int count=0;
        for(int i=0;i<brands.length;i++){
            if(brands[i]==index){
                brands[i]=-1;
                count++;
                if(count==3){
                    break;
                }
            }
        }
    }
    /**
     * 集合转数组
     * @param list
     * @return
     */
    public int[] getBrands(List<Integer> list){
        int[] brands = new int[list.size()];
        for(int i=0;i<list.size();i++){
            brands[i]=getBrand_Value(list.get(i));
        }
        return brands;
    }
    /**
     * 七对检测
     * @param list
     * @param index
     */
    public void IS_Seven(List<Integer> list,int index){
        //将来牌放入用户手中
        list.add(index);
        //重新进行排序
        Order_Brands(list);
        int count=0;
        int draw_count=0;
        for(int i=0;i<list.size();i++){
            if((i+1)<list.size()){
                if(list.get(i)==list.get(i+1)){
                    count++;
                    i++;
                }
            }
        }
        if(count==7)System.out.println("七对胡");
        if(draw_count==1&&count==6)System.out.println("七对胡[1混]");
        if(draw_count==2&&count==5)System.out.println("七对胡[2混]");
        if(draw_count==3&&count==4)System.out.println("七对胡[3混]");
    }
    /**
     * 返回中文牌面
     * @param i
     * @return
     */
    public String getBrand_str(int i){
        String color = "";
        int[] index = ISUser_Mahjong(i);
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

}