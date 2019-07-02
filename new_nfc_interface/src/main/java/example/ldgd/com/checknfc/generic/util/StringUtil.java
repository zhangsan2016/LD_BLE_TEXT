package example.ldgd.com.checknfc.generic.util;

/**
 * Created by ldgd on 2019/2/16.
 * 功能：Sting工具类
 * 说明：
 */

public class StringUtil {


    /**
     *  数字String字符串转byte数组
     * @param str
     * @return
     */
    public static byte[] stringToArray(String str){

        byte[] bt = new byte[str.length()];
        for (int i = 0; i < str.length(); i++) {
            char numberChar = str.charAt(i);
            int number =  Character.getNumericValue(numberChar);
            bt[i] = (byte) number;
        }
        return bt;
    }


}
