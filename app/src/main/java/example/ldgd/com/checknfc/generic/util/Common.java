package example.ldgd.com.checknfc.generic.util;

import com.st.st25sdk.TagHelper;
import com.st.st25sdk.type5.st25dv.ST25DVTag;

import static com.st.st25sdk.TagHelper.ReadWriteProtection.READ_AND_WRITE_PROTECTED_BY_PWD;

/**
 * Created by ldgd on 2018/12/19.
 * 功能： NFC 功能全局的设置
 * 说明：
 */

public class Common {

    /**
     *  设置四个区域的密码，有三个可选(ST25DVTag.ST25DV_PASSWORD_1;)
     */
  //  public final static ST25DVRegisterRfAiSS.ST25DVSecurityStatusPWDControl PROTECTED_BY_PWD = ST25DVRegisterRfAiSS.ST25DVSecurityStatusPWDControl.PROTECTED_BY_PWD2;
    public final static int PROTECTED_BY_PWD = ST25DVTag.ST25DV_PASSWORD_3;

    /**
     *  区域设置的读写权限、读写许可 READABLE_AND_WRITABLE、READ_AND_WRITE_PROTECTED_BY_PWD
     */
    public final static  TagHelper.ReadWriteProtection  READ_WRITE_PROTECTION = READ_AND_WRITE_PROTECTED_BY_PWD;


}
