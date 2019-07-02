package example.ldgd.com.checknfc.generic.util;

import android.content.Context;
import android.content.SharedPreferences;


public class CacheUtils {


    public static void putString(Context context, String key, String values) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("ldgd", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(key, values).commit();

    }

    public static byte[] getString(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("ldgd", Context.MODE_PRIVATE);
        String str = sharedPreferences.getString(key, null);

        byte[] arr = null;
        if (str != null) {
            str = str.substring(1, str.length() - 1);
            String[] splitStr = str.split(",");
            arr = new byte[splitStr.length];
            for (int i = 0; i < splitStr.length; i++) {
                arr[i] = Byte.parseByte(splitStr[i].trim());
            }
        }
        return arr;
    }





}
