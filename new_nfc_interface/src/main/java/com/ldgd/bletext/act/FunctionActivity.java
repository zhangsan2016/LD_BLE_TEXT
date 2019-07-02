package com.ldgd.bletext.act;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.ldgd.bletext.blespp.BluetoothLeService;
import com.ldgd.bletext.crc.checkCRC;
import com.ldgd.bletext.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class FunctionActivity extends Activity implements View.OnClickListener {
    private final static String TAG = FunctionActivity.class.getSimpleName();

    private static final int SHOW_PROGRESS = 0;  // 显示加载框
    private static final int STOP_PROGRESS = -1;  // 关闭加载框
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final int UP_ELECTRIC_PARAMETER = 1;  // 读电参
    private static final int READ_CALIBRATION = 2;  // 读标定
    private static final int MEASURE = 3;  // 计量返回
    private static final int READ_VERSIONS_FIRMWARE = 4; // 读取固件版本号
    private static final int READ_CHIPID = 5;  // 读取芯片ID
    private static final int SHOW_TOAST = 6;


    static long recv_cnt = 0;

    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";


/*    private TextView mDataRecvText;
    private TextView mRecvBytes;
    private TextView mDataRecvFormat;
    private EditText mEditBox;
    private TextView mSendBytes;
    private TextView mDataSendFormat;
    private TextView mNotify_speed_text;*/

    private SeekBar sb_brightness1, sb_brightness2, sb_brightness3, sb_brightness4;
    private TextView tv_progress1, tv_progress2, tv_progress3, tv_progress4;
    private Button bt_fully_on, bt_fully_off;

    private final static int SCANNIN_GREQUEST_CODE = 1;
    /**
     * 显示扫描结果
     */
    private EditText et_qr_code_san;

    private Button bt_calibration_measure, bt_calibration_demarcates; // 计量、标定
    /**
     * 读取版本固件
     */
    private Button bt_read_versions_firmware;
    /**
     * 校时
     */
    private Button bt_timing;
    /**
     * 恢复出厂设置
     */
    private Button bt_restore_factory_settings;
    /**
     * 版本号、固件大小、当前版本时间
     */
    private TextView tv_setting_voltage, tv_setting_firmware_MB, tv_current_time;
    /**
     * 电参
     */
    private TextView tv_para_voltage, tv_para_electricity, tv_para_power, tv_para_power_factor, tv_para_active_power;
    /**
     * 读写标定显示EditText
     */
    private EditText tv_calibration_voltage, et_calibration_electricity, et_calibration_power;

    /**
     * 二维码
     */
    private Button bt_QR_code;
    /**
     * 电压频率、电流频率、功率频率
     */
    private EditText et_voltage_frequency, et_electricity_frequency, et_power_frequency;
    private TextView tv_voltage_frequency, tv_electricity_frequency, tv_power_frequency;

    /**
     * 二维码扫描结果
     */
    private TextView tv_qr_code_result;
    /**
     * 清除报警，关闭报警，开启报警
     */
    private Button bt_eliminate_warning, bt_stop_warning, bt_start_warning;

    private ProgressDialog mProgress;


    private long recvBytes = 0;
    private long lastSecondBytes = 0;
    private long sendBytes;
    private StringBuilder mData;

    int sendIndex = 0;
    int sendDataLen = 0;
    byte[] sendBuf;

    //测速
    private Timer timer;
    private TimerTask task;

    private Button bt_read_electric_parameter;
    private Button bt_read_calibration, bt_write_calibration; // 读写标定


    private Handler upHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            byte[] data = msg.getData().getByteArray("data");
            switch (msg.what) {
                case SHOW_PROGRESS:
                    showProgress();
                    break;
                case STOP_PROGRESS:
                    stopProgress();
                    break;
                case SHOW_TOAST:
                    String src = (String) msg.obj;
                    Toast.makeText(FunctionActivity.this, src, Toast.LENGTH_SHORT).show();

                case UP_ELECTRIC_PARAMETER:
                    byte[] byteArrayExtra = msg.getData().getByteArray("byteArrayExtra");

                    if (byteArrayExtra != null) {

                        // 电压
                        double volt = (double) bytes2int(byteArrayExtra[6], byteArrayExtra[7]) / 100;
                        // 电流
                        double electricity = (double) bytes2int(byteArrayExtra[8], byteArrayExtra[9]) / 100;
                        // 功率
                        double power = (double) bytes2int(byteArrayExtra[10], byteArrayExtra[11]) / 10;
                        // 有功功率
                        double activeower = (double) bytes2int(byteArrayExtra[12], byteArrayExtra[13]);
                        // 功率因数
                        double poweractor = (double) bytes2int(byteArrayExtra[14], byteArrayExtra[15]);

                        tv_para_voltage.setText(String.format("%.2f", volt) + " V");
                        tv_para_electricity.setText(String.format("%.2f", electricity) + " A");
                        tv_para_power.setText(String.format("%.2f", power) + " V");
                        tv_para_power_factor.setText(String.format("%.2f", poweractor) + " W");
                        tv_para_active_power.setText(String.format("%.2f", activeower) + " W");

                    }

                    break;
                case READ_CALIBRATION:
                    byte[] read_calibration = msg.getData().getByteArray("read_calibration");
                    if (read_calibration != null) {

                        // 电压
                        double volt = (double) bytes2int(read_calibration[6], read_calibration[7]) / 100;
                        // 电流
                        double electricity = (double) bytes2int(read_calibration[8], read_calibration[9]) / 1000;
                        // 功率
                        double power = (double) bytes2int(read_calibration[10], read_calibration[11]) / 10;

                        tv_calibration_voltage.setText(volt + "");
                        et_calibration_electricity.setText(electricity + "");
                        et_calibration_power.setText(power + "");


                    }
                    break;
                case MEASURE:  // 计量

                    if (data != null) {
                /*        if (data[6] == 0) {
                            Toast.makeText(FunctionActivity.this, "计量失败", Toast.LENGTH_SHORT).show();
                        }else if(data[6] == 1){
                            Toast.makeText(FunctionActivity.this, "计量成功", Toast.LENGTH_SHORT).show();
                        }*/
                        if (data[5] == 1) {  // 1 = 计量  2 = 标定
                            int voltageFrequency = bytes2int(data[7], data[8]);
                            int electricityFrequency = bytes2int(data[10], data[11]);
                            int powerFrequency = bytes2int(data[13], data[14]);


                            et_voltage_frequency.setText(voltageFrequency + "");
                            et_electricity_frequency.setText(electricityFrequency + "");
                            et_power_frequency.setText(powerFrequency + "");

                            tv_voltage_frequency.setText(data[9] + "%");
                            tv_electricity_frequency.setText(data[12] + "%");
                            tv_power_frequency.setText(data[15] + "%");

                        } else if (data[5] == 2) {
                            if (data[6] == 0) {
                                Toast.makeText(FunctionActivity.this, "标定失败", Toast.LENGTH_SHORT).show();
                            } else if (data[6] == 1) {
                                Toast.makeText(FunctionActivity.this, "标定成功", Toast.LENGTH_SHORT).show();
                            }

                        }

                    }

                    break;
                case READ_VERSIONS_FIRMWARE:

                    if (data != null) {
                        byte[] versionsByte = new byte[3];
                        System.arraycopy(data, 6, versionsByte, 0, 3);
                        int version = bytes2int2(versionsByte);
                        int firmware_MB = bytes2int(data[9], data[10]);

                        tv_setting_voltage.setText(version + "");
                        tv_setting_firmware_MB.setText(firmware_MB + " MB");
                        tv_current_time.setText(getSystemTime());
                    }

                    break;
                case READ_CHIPID:
                    if (data != null) {
                        byte[] readChipid = new byte[12];
                        System.arraycopy(data, 6, readChipid, 0, 12);

                        if (Arrays.equals(readChipid, splitId)) {
                            LogUtil.e("两个数组中的元素值相同");
                            tv_qr_code_result.setText("写入芯片ID成功！");
                        } else {
                            tv_qr_code_result.setText("写入芯片ID失败！");
                        }

                    }

                    break;
            }

        }
    };
    private byte[] splitId;

    /**
     * 得到当前Android系统的时间
     */
    private String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                mBluetoothLeService.connect(mDeviceAddress);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                //特征值找到才代表连接成功
                mConnected = true;
                invalidateOptionsMenu();
                updateConnectionState(R.string.connected);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_NO_DISCOVERED.equals(action)) {
                mBluetoothLeService.connect(mDeviceAddress);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
//                final byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
//                final StringBuilder stringBuilder = new StringBuilder();
//                 for(byte byteChar : data)
//                      stringBuilder.append(String.format("%02X ", byteChar));
//                Log.v("log",stringBuilder.toString());

                //     displayData(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));


                showData(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));

            } else if (BluetoothLeService.ACTION_WRITE_SUCCESSFUL.equals(action)) {
                if (sendDataLen > 0) {
                    Log.v("log", "Write OK,Send again");
                    onSendBtnClicked();
                } else {
                    Log.v("log", "Write Finish");
                }
            }

        }
    };


    /**
     * 把数据显示到View中
     *
     * @param byteArrayExtra 数据
     */
    private void showData(byte[] byteArrayExtra) {   // 读参数
        LogUtil.e("  把数据显示到View中 showData =" + Arrays.toString(byteArrayExtra));
        if (byteArrayExtra.length > 4) {
            if (byteArrayExtra[3] == 67) {
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putByteArray("byteArrayExtra", byteArrayExtra);
                message.setData(bundle);
                message.what = UP_ELECTRIC_PARAMETER;
                upHandler.sendMessage(message);

            } else if (byteArrayExtra[3] == 88) {  // 读标定
                if (byteArrayExtra[5] == 0) {
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("read_calibration", byteArrayExtra);
                    message.setData(bundle);
                    message.what = READ_CALIBRATION;
                    upHandler.sendMessage(message);
                }

            } else if (byteArrayExtra[3] == 89) { // 计量返回
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putByteArray("data", byteArrayExtra);
                message.setData(bundle);
                message.what = MEASURE;
                upHandler.sendMessage(message);

            } else if (byteArrayExtra[3] == -93) {   // 读取固件版本号
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putByteArray("data", byteArrayExtra);
                message.setData(bundle);
                message.what = READ_VERSIONS_FIRMWARE;
                upHandler.sendMessage(message);
            } else if (byteArrayExtra[3] == -125) {   // 芯片id返回
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putByteArray("data", byteArrayExtra);
                message.setData(bundle);
                message.what = READ_CHIPID;
                upHandler.sendMessage(message);
            }
        }


    }

    //高位在前，低位在后
    public int bytes2int(byte byte1, byte byte2) {
        int result = 0;
        int c = (byte1 & 0xff) << 8;
        int d = (byte2 & 0xff);
        result = c | d;
        return result;
    }

    //高位在前，低位在后
    public static byte[] int2bytes(int num) {
        byte[] result = new byte[2];
        result[0] = (byte) ((num >>> 8) & 0xff);
        result[1] = (byte) ((num >>> 0) & 0xff);
        return result;
    }

    //高位在前，低位在后
    public int bytes2int2(byte[] bytes) {
        int result = 0;
        if (bytes.length == 3) {
            int b = (bytes[0] & 0xff) << 16;
            int c = (bytes[1] & 0xff) << 8;
            int d = (bytes[2] & 0xff);
            result = b | c | d;
        }
        return result;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.gatt_services_characteristics);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_function);


        //获取蓝牙的名字和地址
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);


        intitView();


        mData = new StringBuilder();

        final int SPEED = 1;
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SPEED:
                        lastSecondBytes = recvBytes - lastSecondBytes;
                        lastSecondBytes = recvBytes;
                        break;
                }
            }
        };

        task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = SPEED;
                message.obj = System.currentTimeMillis();
                handler.sendMessage(message);
            }
        };

        timer = new Timer();
        // 参数：
        // 1000，延时1秒后执行。
        // 1000，每隔2秒执行1次task。
        timer.schedule(task, 1000, 1000);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void intitView() {

        // 初始化
        sb_brightness1 = (SeekBar) this.findViewById(R.id.sb_brightness1);
        sb_brightness2 = (SeekBar) this.findViewById(R.id.sb_brightness2);
        sb_brightness3 = (SeekBar) this.findViewById(R.id.sb_brightness3);
        sb_brightness4 = (SeekBar) this.findViewById(R.id.sb_brightness4);
        tv_progress1 = (TextView) this.findViewById(R.id.tv_progress1);
        tv_progress2 = (TextView) this.findViewById(R.id.tv_progress2);
        tv_progress3 = (TextView) this.findViewById(R.id.tv_progress3);
        tv_progress4 = (TextView) this.findViewById(R.id.tv_progress4);
        bt_fully_on = (Button) this.findViewById(R.id.bt_fully_on);
        bt_fully_off = (Button) this.findViewById(R.id.bt_fully_off);

        bt_read_electric_parameter = (Button) this.findViewById(R.id.bt_read_electric_parameter);
        bt_read_calibration = (Button) this.findViewById(R.id.bt_read_calibration);
        bt_write_calibration = (Button) this.findViewById(R.id.bt_write_calibration);

        bt_calibration_measure = (Button) this.findViewById(R.id.bt_calibration_measure);
        bt_calibration_demarcates = (Button) this.findViewById(R.id.bt_calibration_demarcates);

        bt_read_versions_firmware = (Button) this.findViewById(R.id.bt_read_versions_firmware);
        bt_timing = (Button) this.findViewById(R.id.bt_timing);
        bt_restore_factory_settings = (Button) this.findViewById(R.id.bt_restore_factory_settings);

        tv_setting_voltage = (TextView) this.findViewById(R.id.tv_setting_voltage);
        tv_setting_firmware_MB = (TextView) this.findViewById(R.id.tv_setting_firmware_MB);
        tv_current_time = (TextView) this.findViewById(R.id.tv_current_time);

        bt_QR_code = (Button) this.findViewById(R.id.bt_QR_code);

        et_qr_code_san = (EditText) this.findViewById(R.id.et_qr_code_san);

        tv_para_voltage = (TextView) this.findViewById(R.id.tv_para_voltage);
        tv_para_electricity = (TextView) this.findViewById(R.id.tv_para_electricity);
        tv_para_power = (TextView) this.findViewById(R.id.tv_para_power);
        tv_para_power_factor = (TextView) this.findViewById(R.id.tv_para_power_factor);
        tv_para_active_power = (TextView) this.findViewById(R.id.tv_para_active_power);

        tv_calibration_voltage = (EditText) this.findViewById(R.id.tv_calibration_voltage);
        et_calibration_electricity = (EditText) this.findViewById(R.id.et_calibration_electricity);
        et_calibration_power = (EditText) this.findViewById(R.id.et_calibration_power);

        et_voltage_frequency = (EditText) this.findViewById(R.id.et_voltage_frequency);
        et_electricity_frequency = (EditText) this.findViewById(R.id.et_electricity_frequency);
        et_power_frequency = (EditText) this.findViewById(R.id.et_power_frequency);
        tv_voltage_frequency = (TextView) this.findViewById(R.id.tv_voltage_frequency);
        tv_electricity_frequency = (TextView) this.findViewById(R.id.tv_electricity_frequency);
        tv_power_frequency = (TextView) this.findViewById(R.id.tv_power_frequency);

        tv_qr_code_result = (TextView) this.findViewById(R.id.tv_qr_code_result);

        bt_eliminate_warning = (Button) this.findViewById(R.id.bt_eliminate_warning);
        bt_stop_warning = (Button) this.findViewById(R.id.bt_stop_warning);
        bt_start_warning = (Button) this.findViewById(R.id.bt_start_warning);


        // 设置监听
        sb_brightness1.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
        sb_brightness2.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
        sb_brightness3.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
        sb_brightness4.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
        bt_fully_on.setOnClickListener(this);
        bt_fully_off.setOnClickListener(this);
        bt_read_electric_parameter.setOnClickListener(this);
        bt_read_calibration.setOnClickListener(this);
        bt_write_calibration.setOnClickListener(this);
        bt_calibration_measure.setOnClickListener(this);
        bt_calibration_demarcates.setOnClickListener(this);

        bt_read_versions_firmware.setOnClickListener(this);
        bt_timing.setOnClickListener(this);
        bt_restore_factory_settings.setOnClickListener(this);

        bt_QR_code.setOnClickListener(this);

        bt_eliminate_warning.setOnClickListener(this);
        bt_stop_warning.setOnClickListener(this);
        bt_start_warning.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // mConnectionState.setText(resourceId);
            }
        });
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_WRITE_SUCCESSFUL);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_NO_DISCOVERED);
        return intentFilter;
    }

    //动态效果
    public void convertText(final TextView textView, final int convertTextId) {
        final Animation scaleIn = AnimationUtils.loadAnimation(this,
                R.anim.text_scale_in);
        Animation scaleOut = AnimationUtils.loadAnimation(this,
                R.anim.text_scale_out);
        scaleOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                textView.setText(convertTextId);
                textView.startAnimation(scaleIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        textView.startAnimation(scaleOut);
    }

    //获取输入框十六进制格式
    private String getHexString(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (('0' <= c && c <= '9') || ('a' <= c && c <= 'f') ||
                    ('A' <= c && c <= 'F')) {
                sb.append(c);
            }
        }
        if ((sb.length() % 2) != 0) {
            sb.deleteCharAt(sb.length());
        }
        return sb.toString();
    }


    private byte[] stringToBytes(String s) {
        byte[] buf = new byte[s.length() / 2];
        for (int i = 0; i < buf.length; i++) {
            try {
                buf[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return buf;
    }

    public String asciiToString(byte[] bytes) {
        char[] buf = new char[bytes.length];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (char) bytes[i];
            sb.append(buf[i]);
        }
        return sb.toString();
    }

    public String bytesToString(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];

            sb.append(hexChars[i * 2]);
            sb.append(hexChars[i * 2 + 1]);
            sb.append(' ');
        }
        return sb.toString();
    }


    private void getSendBuf() {
        sendIndex = 0;

        sendDataLen = sendBuf.length;
    }

    private void onSendBtnClicked() {
        if (sendDataLen > 20) {
            sendBytes += 20;
            final byte[] buf = new byte[20];
            // System.arraycopy(buffer, 0, tmpBuf, 0, writeLength);
            for (int i = 0; i < 20; i++) {
                buf[i] = sendBuf[sendIndex + i];
            }
            sendIndex += 20;
            mBluetoothLeService.writeData(buf);
            sendDataLen -= 20;
        } else {
            sendBytes += sendDataLen;
            final byte[] buf = new byte[sendDataLen];
            for (int i = 0; i < sendDataLen; i++) {
                buf[i] = sendBuf[sendIndex + i];
            }
            mBluetoothLeService.writeData(buf);
            sendDataLen = 0;
            sendIndex = 0;
        }
    }

    private void displayData(byte[] buf) {

        LogUtil.e("displayData read buf = " + Arrays.toString(buf));

        recvBytes += buf.length;
        recv_cnt += buf.length;

        if (recv_cnt >= 1024) {
            recv_cnt = 0;
            mData.delete(0, mData.length() / 2); //UI界面只保留512个字节，免得APP卡顿
        }

        String s = bytesToString(buf);
        mData.append(s);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.bt_fully_on:  // 全开
                Toast.makeText(FunctionActivity.this, "全开", Toast.LENGTH_SHORT).show();
                // EE 00 00 1E 00 64 00 00 00 00 00 00 00 00 00 00 00 FF 95 75 EF

                sendStartLamp(100);

                break;

            case R.id.bt_fully_off: // 全关
                // EE 00 00 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 FF B1 FA EF
                Toast.makeText(FunctionActivity.this, "全关", Toast.LENGTH_SHORT).show();
                sendStartLamp(0);

                break;
            case R.id.bt_read_electric_parameter: // 读取电参
                // EE 00 00 42 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C8 E6 EF
                sendReadElectricParameter();

                break;

            case R.id.bt_read_calibration: // 读标定
                // EE 00 00 58 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C3 7C EF
                sendReadOrWriteCalibration(0, null, null, null);

                break;
            case R.id.bt_write_calibration: // 写标定
                // EE 00 00 58 00 01 00 00 00 00 00 00 00 00 00 00 00 00 42 7E EF


                new AlertDialog.Builder(FunctionActivity.this).setTitle("温馨提示")//设置对话框标题

                        .setMessage("写入标定吗？")//设置显示的内容

                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮


                            @Override

                            public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                                String voltageStr = tv_calibration_voltage.getText().toString();
                                String electricityStr = et_calibration_electricity.getText().toString();
                                if (TextUtils.isEmpty(voltageStr) || TextUtils.isEmpty(electricityStr)) {

                                    Message msg = Message.obtain();
                                    msg.obj = "电压电流参数不能为空";
                                    msg.what = SHOW_TOAST;
                                    upHandler.sendMessage(msg);
                                    return;
                                }
                                double voltage = Double.parseDouble(voltageStr);
                                double electricity = Double.parseDouble(electricityStr);
                                double power = (double) (voltage * electricity) * 10;
                                sendReadOrWriteCalibration(1, int2bytes((int) (voltage * 100)), int2bytes((int) (electricity * 1000)), int2bytes((int) power));


                            }

                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加返回按钮


                    @Override

                    public void onClick(DialogInterface dialog, int which) {//响应事件

                        // TODO Auto-generated method stub

                        Log.i("alertdialog", " 请保存数据！");

                    }

                }).show();//在按键响应事件中显示此对话框


                break;
            case R.id.bt_calibration_measure: // 计量
                // EE 00 00 59 00 01 00 00 00 00 00 00 00 00 00 00 00 00 42 BF EF
                sendMeasureOrDemarcates(1);
                break;
            case R.id.bt_calibration_demarcates: // 标定
                // EE 00 00 59 00 02 00 00 00 00 00 00 00 00 00 00 00 00 81 BA EF
                sendMeasureOrDemarcates(2);
                break;
            case R.id.bt_read_versions_firmware: // 读取版本固件
                // EE 00 00 A2 00 00 00 00 00 00 00 00 00 00 00 00 00 00 80 06 EF
                sendReadVersionsFirmware();

                break;
            case R.id.bt_timing: // 校时
                // EE 00 00 06 00 11 06 1E 0D 31 08 05 00 00 00 00 00 00 07 6A EF
                // EE 00 00 3A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 EA 9E EF

                sendTiming();
                // 读取当前时间
                // sendReadTime();

                break;
            case R.id.bt_restore_factory_settings: // 恢复出厂设置
                // EE 00 00 2E 00 A1 AA 00 00 00 00 00 00 00 00 00 00 00 1C 93 EF

                new AlertDialog.Builder(FunctionActivity.this).setTitle("温馨提示")//设置对话框标题

                        .setMessage("需要恢复原厂设置吗？")//设置显示的内容

                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮


                            @Override

                            public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                                sendRestoreFactorySettings();
                            }

                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加返回按钮


                    @Override

                    public void onClick(DialogInterface dialog, int which) {//响应事件



                    }

                }).show();//在按键响应事件中显示此对话框


                break;
            case R.id.bt_QR_code: // 二维码
                // 清空结果
                tv_qr_code_result.setText("NULL");

                Intent intent = new Intent();
                intent.setClass(this, MipcaActivityCapture.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);


                break;
            case R.id.bt_eliminate_warning: // 清除报警
                upHandler.sendEmptyMessage(SHOW_PROGRESS);
                sendWarningSetting((byte) 0, (byte) 0);


                break;
            case R.id.bt_stop_warning: // 关闭报警
                upHandler.sendEmptyMessage(SHOW_PROGRESS);
                sendWarningSetting((byte) 85, (byte) -86);


                break;
            case R.id.bt_start_warning: // 开启报警
                upHandler.sendEmptyMessage(SHOW_PROGRESS);
                sendWarningSetting((byte) -86, (byte) 85);

                break;


        }
    }


    /**
     * 报警设置
     *
     * @param byte1
     * @param byte2
     */
    private void sendWarningSetting(final byte byte1, final byte byte2) {

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    final byte[] buf = new byte[20];

                    buf[0] = -18;
                    buf[1] = 0;
                    buf[2] = 0;
                    buf[3] = 90; // 指令
                    buf[4] = 0;
                    buf[5] = byte1;
                    buf[6] = byte2;

                    buf[17] = 0;
                    buf[18] = 0;  // CRC
                    buf[19] = 0;
                    //    buf[20] = -17;   // 帧尾


                    // 截取数组做CRC校验
                    final byte[] buf2 = new byte[18];
                    LogUtil.e(Arrays.toString(buf2));

                    System.arraycopy(buf, 0, buf2, 0, 18);
                    // 获取CRC
                    byte[] crc = checkCRC.crc(buf2);
                    // 添加CRC
                    System.arraycopy(crc, 0, buf, 18, 2);

                    mBluetoothLeService.writeData(buf);
                    sleep(200);
                    mBluetoothLeService.writeData(new byte[]{-17});

                    sleep(2000);
                    upHandler.sendEmptyMessage(STOP_PROGRESS);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    upHandler.sendEmptyMessage(STOP_PROGRESS);
                }
            }
        }.start();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {

                    Bundle bundle = data.getExtras();
                    //显示扫描到的内容
                    String chipID = bundle.getString("result");
                    et_qr_code_san.setText(chipID);

                    sendWriteChipID(chipID);

                    //显示
                    //	mImageView.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));
                }
                break;
        }
    }


    /**
     * 写芯片
     *
     * @param chipID 芯片id
     */
    private void sendWriteChipID(final String chipID) {
        // EE 00 00 82 45 15 61 56 15 56 04 00 00 00 00 00 00 00 97 F5 EF

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    final byte[] buf = new byte[20];

                    buf[0] = -18;
                    buf[1] = 0;
                    buf[2] = 0;
                    buf[3] = -126; // 指令
                    buf[4] = 0;   // 芯片号
                    buf[5] = 0;

                    buf[6] = -86;
                    buf[7] = 0;
                    buf[8] = 0;
                    buf[9] = 0;
                    buf[10] = 0;
                    buf[11] = 0;
                    buf[12] = 0;
                    buf[13] = 0;
                    buf[14] = 0;
                    buf[15] = 0;
                    buf[16] = 0;

                    buf[17] = 0;
                    buf[18] = 0;  // CRC
                    buf[19] = 0;
                    //    buf[20] = -17;   // 帧尾


                    // 分割读取的芯片Id
                    splitId = splitString(chipID);
                    // 拼接芯片id
                    System.arraycopy(splitId, 0, buf, 4, splitId.length);


                    // 截取数组做CRC校验
                    final byte[] buf2 = new byte[18];
                    LogUtil.e(Arrays.toString(buf2));

                    System.arraycopy(buf, 0, buf2, 0, 18);
                    // 获取CRC
                    byte[] crc = checkCRC.crc(buf2);
                    // 添加CRC
                    System.arraycopy(crc, 0, buf, 18, 2);

                    mBluetoothLeService.writeData(buf);
                    sleep(200);
                    mBluetoothLeService.writeData(new byte[]{-17});
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private byte[] splitString(String chipID) {

        byte[] splitId = new byte[12];
        for (int i = 0; i < chipID.length() / 2; i++) {
            String id = chipID.substring(i * 2, (i + 1) * 2);
            splitId[i] = (byte) Integer.parseInt(id, 16);
        }
        return splitId;
    }


    /**
     * 恢复恢复出厂设置
     */
    private void sendRestoreFactorySettings() {

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {

                    final byte[] buf = new byte[20];

                    buf[0] = -18;
                    buf[1] = 0;
                    buf[2] = 0;
                    buf[3] = 46;
                    buf[4] = 0;
                    buf[5] = -95;

                    buf[6] = -86;
                    buf[7] = 0;
                    buf[8] = 0;
                    buf[9] = 0;
                    buf[10] = 0;
                    buf[11] = 0;
                    buf[12] = 0;
                    buf[13] = 0;
                    buf[14] = 0;
                    buf[15] = 0;
                    buf[16] = 0;

                    buf[17] = 0;
                    buf[18] = 0;  // CRC
                    buf[19] = 0;
                    //    buf[20] = -17;   // 帧尾


                    // 截取数组做CRC校验
                    final byte[] buf2 = new byte[18];
                    System.arraycopy(buf, 0, buf2, 0, 18);
                    // 获取CRC
                    byte[] crc = checkCRC.crc(buf2);
                    // 添加CRC
                    System.arraycopy(crc, 0, buf, 18, 2);

                    mBluetoothLeService.writeData(buf);
                    sleep(200);
                    mBluetoothLeService.writeData(new byte[]{-17});
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    /**
     * 校时
     */
    private void sendTiming() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {

                    Calendar cal = Calendar.getInstance();
                    //当前年
                    int year = cal.get(Calendar.YEAR);
                    //当前月
                    int month = (cal.get(Calendar.MONTH)) + 1;
                    //当前月的第几天：即当前日
                    int day_of_month = cal.get(Calendar.DAY_OF_MONTH);
                    //当前时：HOUR_OF_DAY-24小时制；HOUR-12小时制
                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    //当前分
                    int minute = cal.get(Calendar.MINUTE);
                    //当前秒
                    int second = cal.get(Calendar.SECOND);
                    //0-上午；1-下午
                    int ampm = cal.get(Calendar.AM_PM);
                    //当前年的第几天
                    int day_of_year = cal.get(Calendar.DAY_OF_YEAR);
                    // 获取当前星期
                    int weekData = cal.get(Calendar.DAY_OF_WEEK) - 1;

                    //     LogUtil.e("year = " + year + "month " + month + "月" + day_of_month + "日" + "上下午 ： " +hour +"   "+ minute + " weekData = " + weekData);
                    final byte[] buf = new byte[20];

                    buf[0] = -18;
                    buf[1] = 0;
                    buf[2] = 0;
                    buf[3] = 6;
                    buf[4] = 0;
                    buf[5] = (byte) Integer.parseInt(String.valueOf(year).substring(2));

                    buf[6] = (byte) month;
                    buf[7] = (byte) day_of_month;
                    buf[8] = (byte) hour;
                    buf[9] = (byte) minute;
                    buf[10] = (byte) second;
                    buf[11] = (byte) weekData;
                    buf[12] = 0;
                    buf[13] = 0;
                    buf[14] = 0;
                    buf[15] = 0;
                    buf[16] = 0;

                    buf[17] = 0;
                    buf[18] = 0;  // CRC
                    buf[19] = 0;
                    //    buf[20] = -17;   // 帧尾


                    // 截取数组做CRC校验
                    final byte[] buf2 = new byte[18];
                    System.arraycopy(buf, 0, buf2, 0, 18);
                    // 获取CRC
                    byte[] crc = checkCRC.crc(buf2);
                    // 添加CRC
                    System.arraycopy(crc, 0, buf, 18, 2);

                    mBluetoothLeService.writeData(buf);
                    sleep(200);
                    mBluetoothLeService.writeData(new byte[]{-17});
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    /**
     * 读取固件版本
     */
    private void sendReadVersionsFirmware() {

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    final byte[] buf = new byte[20];

                    buf[0] = -18;
                    buf[1] = 0;
                    buf[2] = 0;
                    buf[3] = -94;
                    buf[4] = 0;
                    buf[5] = 0;

                    buf[6] = 0;
                    buf[7] = 0;
                    buf[8] = 0;
                    buf[9] = 0;
                    buf[10] = 0;
                    buf[11] = 0;
                    buf[12] = 0;
                    buf[13] = 0;
                    buf[14] = 0;
                    buf[15] = 0;
                    buf[16] = 0;

                    buf[17] = 0;
                    buf[18] = 0;  // CRC
                    buf[19] = 0;
                    //    buf[20] = -17;   // 帧尾


                    // 截取数组做CRC校验
                    final byte[] buf2 = new byte[18];
                    System.arraycopy(buf, 0, buf2, 0, 18);
                    // 获取CRC
                    byte[] crc = checkCRC.crc(buf2);
                    // 添加CRC
                    System.arraycopy(crc, 0, buf, 18, 2);

                    mBluetoothLeService.writeData(buf);
                    sleep(200);
                    mBluetoothLeService.writeData(new byte[]{-17});
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    /**
     * 测量或标定
     *
     * @param md 1-测量、2-标定
     */
    private void sendMeasureOrDemarcates(final int md) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    final byte[] buf = new byte[20];

                    buf[0] = -18;
                    buf[1] = 0;
                    buf[2] = 0;
                    buf[3] = 89;
                    buf[4] = 0;
                    buf[5] = (byte) md;

                    buf[6] = 0;
                    buf[7] = 0;
                    buf[8] = 0;
                    buf[9] = 0;
                    buf[10] = 0;
                    buf[11] = 0;
                    buf[12] = 0;
                    buf[13] = 0;
                    buf[14] = 0;
                    buf[15] = 0;
                    buf[16] = 0;

                    buf[17] = 0;
                    buf[18] = 0;  // CRC
                    buf[19] = 0;
                    //    buf[20] = -17;   // 帧尾


                    // 截取数组做CRC校验
                    final byte[] buf2 = new byte[18];
                    System.arraycopy(buf, 0, buf2, 0, 18);
                    // 获取CRC
                    byte[] crc = checkCRC.crc(buf2);
                    // 添加CRC
                    System.arraycopy(crc, 0, buf, 18, 2);

                    mBluetoothLeService.writeData(buf);
                    sleep(200);
                    mBluetoothLeService.writeData(new byte[]{-17});
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }


    /**
     * 读写标定
     *
     * @param rw 0 读、1写
     */
    private void sendReadOrWriteCalibration(final int rw, final byte[] voltageByte, final byte[] electricityByte, final byte[] powerByte) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    final byte[] buf = new byte[20];

                    buf[0] = -18;
                    buf[1] = 0;
                    buf[2] = 0;
                    buf[3] = 88;
                    buf[4] = 0;
                    buf[5] = (byte) rw;

                    if (rw == 1) {
                        buf[6] = voltageByte[0];
                        buf[7] = voltageByte[1];
                        buf[8] = electricityByte[0];
                        buf[9] = electricityByte[1];
                        buf[10] = powerByte[0];
                        buf[11] = powerByte[1];
                    }

                    buf[12] = 0;
                    buf[13] = 0;
                    buf[14] = 0;
                    buf[15] = 0;
                    buf[16] = 0;

                    buf[17] = 0;
                    buf[18] = 0;  // CRC
                    buf[19] = 0;
                    //    buf[20] = -17;   // 帧尾


                    // 截取数组做CRC校验
                    final byte[] buf2 = new byte[18];
                    System.arraycopy(buf, 0, buf2, 0, 18);
                    // 获取CRC
                    byte[] crc = checkCRC.crc(buf2);
                    // 添加CRC
                    System.arraycopy(crc, 0, buf, 18, 2);

                    mBluetoothLeService.writeData(buf);
                    sleep(200);
                    mBluetoothLeService.writeData(new byte[]{-17});
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }


    /**
     * 读取电参
     */
    private void sendReadElectricParameter() {

        //   EE 00 00 42 00 00 00 00 00 00 00 00 00 00 00 00 00 00 C8 E6 EF
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {

                    final byte[] buf = new byte[20];

                    buf[0] = -18;
                    buf[1] = 0;
                    buf[2] = 0;
                    buf[3] = 66;  // 指令码
                    buf[4] = 0;
                    buf[5] = 0;

                    buf[6] = 0;
                    buf[7] = 0;
                    buf[8] = 0;
                    buf[9] = 0;
                    buf[10] = 0;
                    buf[11] = 0;
                    buf[12] = 0;
                    buf[13] = 0;
                    buf[14] = 0;
                    buf[15] = 0;
                    buf[16] = 0;

                    buf[17] = 0;
                    buf[18] = 0;  // CRC
                    buf[19] = 0;
                    //    buf[20] = -17;   // 帧尾


                    // 截取数组做CRC校验
                    final byte[] buf2 = new byte[18];
                    System.arraycopy(buf, 0, buf2, 0, 18);
                    // 获取CRC
                    byte[] crc = checkCRC.crc(buf2);
                    // 添加CRC
                    System.arraycopy(crc, 0, buf, 18, 2);

                    mBluetoothLeService.writeData(buf);
                    sleep(200);
                    mBluetoothLeService.writeData(new byte[]{-17});
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }


    private class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (seekBar.getId()) {
                case R.id.sb_brightness1:
                    tv_progress1.setText(progress + " %");
                    break;
                case R.id.sb_brightness2:
                    tv_progress2.setText(progress + " %");
                    break;
                case R.id.sb_brightness3:
                    tv_progress3.setText(progress + " %");
                    break;
                case R.id.sb_brightness4:
                    tv_progress4.setText(progress + " %");
                    break;

            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Toast.makeText(FunctionActivity.this, "seebar = " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
            // 80 F8 00 00 80 00 00 80 80 00 00 00 00 00 00 00 00 00 00 00 00 F8 80 00 78 F8 F8

            int progress = seekBar.getProgress();
            switch (seekBar.getId()) {

                case R.id.sb_brightness1:
                    tv_progress1.setText(progress + " %");
                    sendSeekBarLuminance(progress, 1);
                    break;
                case R.id.sb_brightness2:
                    tv_progress2.setText(progress + " %");
                    sendSeekBarLuminance(progress, 2);
                    break;
                case R.id.sb_brightness3:
                    tv_progress3.setText(progress + " %");
                    sendSeekBarLuminance(progress, 4);
                    break;
                case R.id.sb_brightness4:
                    // EE 00 00 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 08 37 BB EF
                    tv_progress4.setText(progress + " %");
                    sendSeekBarLuminance(progress, 8);
                    break;


            }

        }
    }


    /**
     * 曲线进度条实时调光
     *
     * @param progress 进度
     * @param location 灯具位
     */
    private void sendSeekBarLuminance(final int progress, final int location) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {

                    final byte[] buf = new byte[20];

                    buf[0] = -18;
                    buf[1] = 0;
                    buf[2] = 0;
                    buf[3] = 30;
                    buf[4] = 0;
                    buf[5] = (byte) progress;  // 亮度

                    buf[6] = 0;
                    buf[7] = 0;
                    buf[8] = 0;
                    buf[9] = 0;
                    buf[10] = 0;
                    buf[11] = 0;
                    buf[12] = 0;
                    buf[13] = 0;
                    buf[14] = 0;
                    buf[15] = 0;
                    buf[16] = 0;

                    buf[17] = (byte) location;  // 灯具号
                    buf[18] = 0;  // CRC
                    buf[19] = 0;
                    //    buf[20] = -17;   // 帧尾


                    // 截取数组做CRC校验
                    final byte[] buf2 = new byte[18];
                    System.arraycopy(buf, 0, buf2, 0, 18);
                    // 获取CRC
                    byte[] crc = checkCRC.crc(buf2);
                    // 添加CRC
                    System.arraycopy(crc, 0, buf, 18, 2);

                    mBluetoothLeService.writeData(buf);
                    sleep(200);
                    mBluetoothLeService.writeData(new byte[]{-17});
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }


    /**
     * 曲线实时调光全开、全关
     *
     * @param luminance
     */
    private void sendStartLamp(final int luminance) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {

                    final byte[] buf = new byte[20];
                    //     EE 00 00 1E 00 64 00 00 00 00 00 00 00 00 00 00 00 FF 95 75 EF 全开
                    // EE 00 00 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 FF B1 FA EF  全关

                    buf[0] = -18;
                    buf[1] = 0;
                    buf[2] = 0;
                    buf[3] = 30;
                    buf[4] = 0;
                    buf[5] = (byte) luminance;  // 亮度

                    buf[6] = 0;
                    buf[7] = 0;
                    buf[8] = 0;
                    buf[9] = 0;
                    buf[10] = 0;
                    buf[11] = 0;
                    buf[12] = 0;
                    buf[13] = 0;
                    buf[14] = 0;
                    buf[15] = 0;
                    buf[16] = 0;

                    buf[17] = -1;  // 灯具号
                    buf[18] = 0;  // CRC
                    buf[19] = 0;
                    //    buf[20] = -17;   // 帧尾


                    // 截取数组做CRC校验
                    final byte[] buf2 = new byte[18];
                    System.arraycopy(buf, 0, buf2, 0, 18);
                    // 获取CRC
                    byte[] crc = checkCRC.crc(buf2);
                    // 添加CRC
                    System.arraycopy(crc, 0, buf, 18, 2);

                    mBluetoothLeService.writeData(buf);
                    sleep(200);
                    mBluetoothLeService.writeData(new byte[]{-17});
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    /**
     * 读取时间
     */
    private void sendReadTime() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {

                    final byte[] buf = new byte[20];
                    //     EE 00 00 1E 00 64 00 00 00 00 00 00 00 00 00 00 00 FF 95 75 EF 全开
                    // EE 00 00 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 FF B1 FA EF  全关

                    buf[0] = -18;
                    buf[1] = 0;
                    buf[2] = 0;
                    buf[3] = 58;
                    buf[4] = 0;
                    buf[5] = 0;

                    buf[6] = 0;
                    buf[7] = 0;
                    buf[8] = 0;
                    buf[9] = 0;
                    buf[10] = 0;
                    buf[11] = 0;
                    buf[12] = 0;
                    buf[13] = 0;
                    buf[14] = 0;
                    buf[15] = 0;
                    buf[16] = 0;

                    buf[17] = -1;  // 灯具号
                    buf[18] = 0;  // CRC
                    buf[19] = 0;
                    //    buf[20] = -17;   // 帧尾


                    // 截取数组做CRC校验
                    final byte[] buf2 = new byte[18];
                    System.arraycopy(buf, 0, buf2, 0, 18);
                    // 获取CRC
                    byte[] crc = checkCRC.crc(buf2);
                    // 添加CRC
                    System.arraycopy(crc, 0, buf, 18, 2);

                    mBluetoothLeService.writeData(buf);
                    sleep(200);
                    mBluetoothLeService.writeData(new byte[]{-17});
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void showProgress() {
        mProgress = ProgressDialog.show(this, "执行中...", "");
    }

    private void stopProgress() {
        if (mProgress != null) {
            mProgress.cancel();
        }

    }


}
