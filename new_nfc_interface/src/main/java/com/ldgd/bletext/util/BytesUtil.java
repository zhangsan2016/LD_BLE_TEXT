package com.ldgd.bletext.util;

/**
 * Created by ldgd on 2018/5/11.
 */

public class BytesUtil {

    /**
     * 两位byte数组转int
     * @category byte[]
     * */
    public static int bytesToInt2(byte[] src) {
        int value;
        value = (int) (((src[0] & 0xFF)<<8)
                |(src[1] & 0xFF));
        return value;
    }


}
