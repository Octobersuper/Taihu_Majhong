package mall;


import com.zcf.mahjong.util.Mahjong_Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IDEA
 * author:ZhaoQ
 * className:
 * Date:2019/6/19
 * Time:10:18
 */
public class Test {

    public static void main(String[] args) {
        Mahjong_Util util = Mahjong_Util.mahjong_Util;
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(19);
        list.add(23);
        list.add(24);
        list.add(25);
        list.add(26);
        list.add(54);
        list.add(89);
        list.add(99);
        list.add(110);
        list.add(130);
        list.add(135);
        int[] ints = util.IS_Eat(list, 24);

    }
}
