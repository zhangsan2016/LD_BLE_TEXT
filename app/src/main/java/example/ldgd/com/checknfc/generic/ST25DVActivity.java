/*
  * @author STMicroelectronics MMY Application team
  *
  ******************************************************************************
  * @attention
  *
  * <h2><center>&copy; COPYRIGHT 2017 STMicroelectronics</center></h2>
  *
  * Licensed under ST MIX_MYLIBERTY SOFTWARE LICENSE AGREEMENT (the "License");
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at:
  *
  *        http://www.st.com/Mix_MyLiberty
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
  * AND SPECIFICALLY DISCLAIMING THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  ******************************************************************************
*/

package example.ldgd.com.checknfc.generic;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.ldgd.bletext.R;
import com.st.st25sdk.MultiAreaInterface;
import com.st.st25sdk.STException;
import com.st.st25sdk.TagHelper;
import com.st.st25sdk.type5.st25dv.ST25DVRegisterRfAiSS;
import com.st.st25sdk.type5.st25dv.ST25DVTag;

import java.util.ArrayList;
import java.util.List;

import example.ldgd.com.checknfc.activity.MyPwdDialogFragment;
import example.ldgd.com.checknfc.adapter.STPagerAdapter;
import example.ldgd.com.checknfc.fragment.PwdDialogFragment;
import example.ldgd.com.checknfc.fragment.STFragment;
import example.ldgd.com.checknfc.fragment.STType5PwdDialogFragment;
import example.ldgd.com.checknfc.generic.type4.ST25Menu;
import example.ldgd.com.checknfc.generic.util.Common;
import example.ldgd.com.checknfc.generic.util.UIHelper;

import static com.st.st25sdk.TagHelper.ReadWriteProtection.READABLE_AND_WRITE_PROTECTED_BY_PWD;
import static com.st.st25sdk.type5.st25dv.ST25DVTag.ST25DV_PASSWORD_3;
import static example.ldgd.com.checknfc.fragment.STType5PwdDialogFragment.STPwdAction.INIT_NFC;
import static example.ldgd.com.checknfc.fragment.STType5PwdDialogFragment.STPwdAction.KILL_TAG;
import static example.ldgd.com.checknfc.generic.util.Common.PROTECTED_BY_PWD;

public class ST25DVActivity extends STFragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener, STFragment.STFragmentListener, MyPwdDialogFragment.STType5PwdDialogListener {

    // Set here the Toolbar to use for this activity
    private int toolbar_res = R.menu.toolbar_empty;

    final static String TAG = "ST25DVActivity";
    public ST25DVTag mST25DVTag;
    public ST25Menu mMenu;

    STPagerAdapter mPagerAdapter;
    ViewPager mViewPager;

    private SlidingTabLayout mSlidingTabLayout;

    ListView lv;
    private STType5PwdDialogFragment.STPwdAction mCurrentAction;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_layout);

        if (super.getTag() instanceof ST25DVTag) {
            mST25DVTag = (ST25DVTag) super.getTag();
        }
        if (mST25DVTag == null) {
            showToast(R.string.invalid_tag);
            goBackToMainActivity();
            return;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(Common.MYLD_DEVICE_NAME);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mMenu = ST25Menu.newInstance(super.getTag());
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        mMenu.inflateMenu(navigationView);

        List<UIHelper.STFragmentId> fragmentList = new ArrayList<UIHelper.STFragmentId>();

        fragmentList.add(UIHelper.STFragmentId.TAG_INFO_FRAGMENT_ID);
        fragmentList.add(UIHelper.STFragmentId.NDEF_DETAILS_FRAGMENT_ID);
        fragmentList.add(UIHelper.STFragmentId.CC_FILE_TYPE5_FRAGMENT_ID);
        fragmentList.add(UIHelper.STFragmentId.SYS_FILE_TYP5_FRAGMENT_ID);
        fragmentList.add(UIHelper.STFragmentId.RAW_DATA_FRAGMENT_ID);

        mPagerAdapter = new STPagerAdapter(getSupportFragmentManager(), getApplicationContext(), fragmentList);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);

        // Check if the activity was started with a request to select a specific tab
        Intent mIntent = getIntent();
        int tabNbr = mIntent.getIntExtra("select_tab", -1);
        if (tabNbr != -1) {
            mViewPager.setCurrentItem(tabNbr);
        }


        // 初始化nfc模块权限
         initNFCProtection();

        // 检测NFC密码
     //   presentPassword();
    }

    private void presentPassword() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                STType5PwdDialogFragment.STPwdAction pwdAction = STType5PwdDialogFragment.STPwdAction.PRESENT_CURRENT_PWD;
                String message = " 输入区密码";

                int passwordNumber = ST25DVTag.ST25DV_CONFIGURATION_PASSWORD_ID;
                try {
                    passwordNumber = mST25DVTag.getPasswordNumber(MultiAreaInterface.AREA4);
                } catch (STException e) {
                    e.printStackTrace();
                }
                // 参数 pwdAction : Dialog标识，passwordNumber ：得到的当前密码，message ： Dialog提示消息
                STType5PwdDialogFragment pwdDialogFragment = STType5PwdDialogFragment.newInstance(pwdAction, passwordNumber, message);
                mCurrentAction = KILL_TAG;
                pwdDialogFragment.show(getSupportFragmentManager(), "pwdDialogFragment");

            }
        }.start();

    }

    /**
     *  初始化nfc模块权限
     */
    private void initNFCProtection() {

     new Thread(new Runnable(){
         @Override
         public void run() {
             // 设置区域密码
             try {
                 // 获取四个区域
                 List<ST25DVRegisterRfAiSS> rfAiSSRegisters = new ArrayList<ST25DVRegisterRfAiSS>();
                 ST25DVRegisterRfAiSS rfAiSSRegister1 = (ST25DVRegisterRfAiSS) mST25DVTag.getRegister(ST25DVTag.REGISTER_RFA1SS_ADDRESS);
                 ST25DVRegisterRfAiSS rfAiSSRegister2 = (ST25DVRegisterRfAiSS) mST25DVTag.getRegister(ST25DVTag.REGISTER_RFA2SS_ADDRESS);
                 ST25DVRegisterRfAiSS rfAiSSRegister3 = (ST25DVRegisterRfAiSS) mST25DVTag.getRegister(ST25DVTag.REGISTER_RFA3SS_ADDRESS);
                 ST25DVRegisterRfAiSS rfAiSSRegister4 = (ST25DVRegisterRfAiSS) mST25DVTag.getRegister(ST25DVTag.REGISTER_RFA4SS_ADDRESS);
                 rfAiSSRegisters.add(rfAiSSRegister1);
                 rfAiSSRegisters.add(rfAiSSRegister2);
                 rfAiSSRegisters.add(rfAiSSRegister3);
                 rfAiSSRegisters.add(rfAiSSRegister4);

                 for (int i = 0; i < 4; i++) {
                     ST25DVRegisterRfAiSS rfAiSSRegister =  rfAiSSRegisters.get(i);
                     ST25DVRegisterRfAiSS.ST25DVSecurityStatusPWDControl pwdNbr = rfAiSSRegister.getSSPWDControl();
                     TagHelper.ReadWriteProtection rwProtection =  rfAiSSRegister.getSSRWProtection();

                       if(i == 0){
                           if(rwProtection != READABLE_AND_WRITE_PROTECTED_BY_PWD){
                               // 如果是区域1 ，初始化为可读写，入密码保护
                               rfAiSSRegister.setSSReadWriteProtection(READABLE_AND_WRITE_PROTECTED_BY_PWD);
                           }
                           if(pwdNbr != Common.PWD_NBR){
                               mST25DVTag.setPasswordNumber(i+1, PROTECTED_BY_PWD);
                           }

                           continue;
                       }

                     if(pwdNbr != Common.PWD_NBR){
                         mST25DVTag.setPasswordNumber(i+1, PROTECTED_BY_PWD);
                     }
                     if(rwProtection != Common.READ_WRITE_PROTECTION){
                         rfAiSSRegister.setSSReadWriteProtection(Common.READ_WRITE_PROTECTION);
                     }
                 }


             /*    // 得到密码状态
                 ST25DVRegisterRfAiSS.ST25DVSecurityStatusPWDControl pwdNbr1 = rfAiSSRegister1.getSSPWDControl();
                 ST25DVRegisterRfAiSS.ST25DVSecurityStatusPWDControl pwdNbr2 = rfAiSSRegister2.getSSPWDControl();
                 ST25DVRegisterRfAiSS.ST25DVSecurityStatusPWDControl pwdNbr3 = rfAiSSRegister3.getSSPWDControl();
                 ST25DVRegisterRfAiSS.ST25DVSecurityStatusPWDControl pwdNbr4 = rfAiSSRegister4.getSSPWDControl();


                 // 判断密码是否设置成密码3 pwdNbr == NO_PWD_SELECTED
                 if (pwdNbr2 != PROTECTED_BY_PWD3 || pwdNbr3 != PROTECTED_BY_PWD3 || pwdNbr4 != PROTECTED_BY_PWD3) {
                     // 设置读写权限
                   //  rfAiSSRegister1.setSSReadWriteProtection(Common.READ_WRITE_PROTECTION);
                     rfAiSSRegister2.setSSReadWriteProtection(Common.READ_WRITE_PROTECTION);
                     rfAiSSRegister3.setSSReadWriteProtection(Common.READ_WRITE_PROTECTION);
                     rfAiSSRegister4.setSSReadWriteProtection(Common.READ_WRITE_PROTECTION);
                     // 设置密码
                     mST25DVTag.setPasswordNumber(1,Common.PROTECTED_BY_PWD);
                     mST25DVTag.setPasswordNumber(2,Common.PROTECTED_BY_PWD);
                     mST25DVTag.setPasswordNumber(3,Common.PROTECTED_BY_PWD);
                     mST25DVTag.setPasswordNumber(4,Common.PROTECTED_BY_PWD);
                 }*/
             } catch (STException e) {
                 switch (e.getError()) {
                     case TAG_NOT_IN_THE_FIELD:
                         showToast(R.string.tag_not_in_the_field);
                         break;
                     case CONFIG_PASSWORD_NEEDED:
                         mCurrentAction = INIT_NFC;
                         displayPasswordDialogBox();
                         break;
                     default:
                         e.printStackTrace();
                         showToast(R.string.error_while_updating_the_tag);
                 }
             }
         }
     }).start();
    }

    public void setmCurrentAction(STType5PwdDialogFragment.STPwdAction mCurrentAction) {
        this.mCurrentAction = mCurrentAction;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds read_list_items to the action bar if it is present.
        getMenuInflater().inflate(toolbar_res, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long

        // as you specify a parent activity in AndroidManifest.xml.


        return super.onOptionsItemSelected(item);
    }

    void processIntent(Intent intent) {
        Log.d(TAG, "Process Intent");
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        return mMenu.selectItem(this, item);
        //   return false;
    }

    public ST25DVTag getTag() {
        return mST25DVTag;
    }

    @Override
    public void onSTType5PwdDialogFinish(int result) {

        Log.v(TAG, "onSTType5PwdDialogFinish. result = " + result);
        if (result == PwdDialogFragment.RESULT_OK && mCurrentAction!=null) {
            switch (mCurrentAction) {
                case PRESENT_CURRENT_PWD:
                    // Old password entered successfully
                    // We can now enter the new password
                    enterNewPassword();
                    break;

                case ENTER_NEW_PWD:
                    showToast(R.string.change_pwd_succeeded);
                    break;
                case INIT_NFC:
                    initNFCProtection();
                    break;
            }

        } else {
            Log.e(TAG, "Action failed! Tag not updated!");
        }
    }


    private void enterNewPassword() {
        mCurrentAction = STType5PwdDialogFragment.STPwdAction.ENTER_NEW_PWD;
        MyST25DVAreaSecurity.getInstance().changePassword(getSupportFragmentManager(), ST25DV_PASSWORD_3);
/*   new Thread(new Runnable() {
            public void run() {
                int passwordNumber = getSelectedPassword();
                String message = getString(R.string.please_enter_new_pwd_x, getSelectedMessagePassword());

                Log.v(TAG, "enterNewPassword");
                mCurrentAction = STType5PwdDialogFragment.STPwdAction.ENTER_NEW_PWD;

                STType5PwdDialogFragment pwdDialogFragment = STType5PwdDialogFragment.newInstance(mCurrentAction, passwordNumber, message);
                pwdDialogFragment.show(mFragmentManager, "pwdDialogFragment");
            }
        }).start();*/
    }



    private void displayPasswordDialogBox() {
        Log.v(TAG, "displayPasswordDialogBox");

        // Warning: Function called from background thread! Post a request to the UI thread
        runOnUiThread(new Runnable() {
            public void run() {
                MyPwdDialogFragment pwdDialogFragment = MyPwdDialogFragment.newInstance(
                        MyPwdDialogFragment.STPwdAction.PRESENT_CURRENT_PWD,
                        ST25DVTag.ST25DV_CONFIGURATION_PASSWORD_ID,
                     //   ST25DVTag.ST25DV_PASSWORD_3,
                     // getResources().getString(R.string.enter_configuration_pwd));
                        getResources().getString(R.string.enter_configuration_pwd2));

                pwdDialogFragment.show(getSupportFragmentManager(), "pwdDialogFragment");
            }
        });
    }
}

