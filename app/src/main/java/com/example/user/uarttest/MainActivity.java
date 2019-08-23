package com.example.user.uarttest;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularfillableloaders.CircularFillableLoaders;
import com.sdsmdg.harjot.crollerTest.Croller;
import com.sdsmdg.harjot.crollerTest.OnCrollerChangeListener;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener{

    public final static String ACTION_COMPARE_TEMP =
            "ACTION_COMPARE_TEMP";

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_COOLING_CONTROL = 3;
    private static final int REQUEST_LOGIN = 4;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "UART_TEST";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    private static final int STATE_LOGIN = 1;
    private static final int STATE_LOGOUT = 0;

    // display cooling state on CoolingControl activity
    private String coolingState = "1";
    private int savedCoolingState = 1;
    private String modeState = CoolingControl.COOL;

    private int userStatus;
    private String userId;

    private String temp1;
    private String temp2;
    private String bpm;

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
//    private ListView messageListView;
//    private ArrayAdapter<String> listAdapter;
//    private Button btnConnectDisconnect,btnSend;
//    private EditText edtMessage;

    private CircularFillableLoaders circularFillableLoaders;
    private Croller croller;
    private TextView tvCurrentTemp, tvBpm, tvMainMode, tvLevel, tvBattery, tvDisplayEx, tvOn;
    private ViewGroup llBpm;
    private Button btnModeChangeMain;
//    private Button btnCoolingStateMain;
    private ImageView imgHeart;
    private ProgressBar progressBar;

    private String[] controllerInput = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
//        messageListView = (ListView) findViewById(R.id.listMessage);
//        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
//        messageListView.setAdapter(listAdapter);
//        messageListView.setDivider(null);
//        btnConnectDisconnect=(Button) findViewById(R.id.btn_select);
//        btnSend=(Button) findViewById(R.id.sendButton);

        layoutImplement();

//        edtMessage = (EditText) findViewById(R.id.sendText);
        service_init();

        final Handler nHandler = new Handler();

        croller.setOnCrollerChangeListener(new OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(Croller croller, int progress) {
                if (mDevice != null){
                    // removeMessages(0)의 의미 : 모든 핸들러의 큐를 제거 (onStopTrackingTouch의 이벤트 제거)
                    nHandler.removeMessages(0);
                    circularFillableLoaders.setProgress(100 - (progress * 5));
                    circularFillableLoaders.setAmplitudeRatio((float) (0.03 + progress/100));
//                    btnCoolingStateMain.setText(progress + " LV");

                    savedCoolingState = progress;

                    if(progress > 10){
                        coolingState = controllerInput[progress - 11];
                        Log.i("COOLINGLEVEL", "************   " + coolingState);
                    } else {
                        coolingState = String.valueOf(progress - 1);
                        Log.i("COOLINGLEVEL", "************   " + coolingState);
                    }

                    Intent intent = new Intent(CoolingControl.ACTION_LEVEL_CONTROL);
                    intent.putExtra("CoolingStateTx",coolingState);
                    intent.putExtra("CoolingStateVal",String.valueOf(savedCoolingState));

                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                }
            }

            @Override
            public void onStartTrackingTouch(Croller croller) {
                if (mDevice == null){
                    Toast.makeText(MainActivity.this,"먼저 쿨링글러브와 블루투스 연결을 해주세요.",Toast.LENGTH_SHORT).show();
                    tvOn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onStopTrackingTouch(Croller croller) {
                if (mDevice != null){
                    // 별 다른 동작이 없을 시 TextView에 On이라는 글자 나타나도록 (cooling on)
                    nHandler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            tvOn.setVisibility(View.VISIBLE);
                        }
                    }, 300000);
                }
            }
        });

        btnModeChangeMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDevice == null) {
                    Toast.makeText(MainActivity.this, "먼저 쿨링글러브와 블루투스 연결을 해주세요.", Toast.LENGTH_SHORT).show();
                }else {
                    if (croller.getLabel().equals("COOLING")) {
                        tvCurrentTemp.setText("HEATING");
                        croller.setLabel("HEATING");
                        croller.setLabelColor(0xffff0000);
                        croller.setProgressPrimaryColor(0xffff0000);
                        croller.setIndicatorColor(0xffff0000);
                        croller.setProgress(1);
                        circularFillableLoaders.setColor(0xffff0000);
                        modeState = CoolingControl.HEAT;

                        Intent intent = new Intent(CoolingControl.ACTION_MODE_CHANGE);
                        intent.putExtra(CoolingControl.STRING_EXTRA_DATA, CoolingControl.HEAT);
                        intent.putExtra("CoolingStateTx", String.valueOf(0));
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                    } else {
                        tvCurrentTemp.setText("COOLING");
                        croller.setLabel("COOLING");
                        croller.setLabelColor(0xFF9BF2FF);
                        croller.setProgressPrimaryColor(0xFF9BF2FF);
                        croller.setIndicatorColor(0xFF9BF2FF);
                        croller.setProgress(1);
                        circularFillableLoaders.setColor(0xFF9BF2FF);
                        modeState = CoolingControl.COOL;

                        Intent intent = new Intent(CoolingControl.ACTION_MODE_CHANGE);
                        intent.putExtra(CoolingControl.STRING_EXTRA_DATA, CoolingControl.COOL);
                        intent.putExtra("CoolingStateTx", String.valueOf(0));
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                    }
                }
            }
        });

        imgHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDevice != null){
                    tvBpm.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    Random rand = new Random(System.currentTimeMillis());
                    final int temp = (Math.abs(rand.nextInt(11)) + 65);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            tvBpm.setVisibility(View.VISIBLE);
                            tvBpm.setText(String.valueOf(temp));
                        }
                    }, 10000);
                }
            }
        });

        Log.i(TAG, "onCreate Called");
        userStatus = STATE_LOGOUT;
    }

    public void layoutImplement(){
        circularFillableLoaders = (CircularFillableLoaders)findViewById(R.id.circularFillableLoaders);
        croller = (Croller) findViewById(R.id.croller);
        tvCurrentTemp = (TextView) findViewById(R.id.tvCurrentTemp);
        tvBpm = (TextView) findViewById(R.id.tvBpm);
//        tvBpm.setVisibility(View.GONE);
//        tvMainMode = (TextView) findViewById(R.id.tvMainMode);
//        tvLevel = (TextView) findViewById(R.id.tvLevel);
        tvBattery = (TextView) findViewById(R.id.tvBattery);
        llBpm = (ViewGroup) findViewById(R.id.llBpm);
//        tvDisplayEx = (TextView) findViewById(R.id.tvDisplayEx);
        btnModeChangeMain = (Button) findViewById(R.id.btnModeChangeMain);
//        btnCoolingStateMain = (Button) findViewById(R.id.btnCoolingStateMain);
        imgHeart = (ImageView) findViewById(R.id.imgHeart);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        tvOn = (TextView) findViewById(R.id.tvOn);
    }

    //for menu
    @Override
    //액티비티에서 메뉴를 구체화 시키는 부분
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu called");
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu){
        Log.i(TAG, "onPrepareOpntionsMenu called");
        if(userStatus == STATE_LOGOUT){
            menu.getItem(0).setVisible(true);
            menu.getItem(1).setVisible(false);
            menu.getItem(2).setVisible(false);
            menu.getItem(4).setVisible(false);
            menu.getItem(5).setVisible(false);
            menu.getItem(6).setVisible(false);

        } else if(userStatus == STATE_LOGIN){
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(true);
            menu.getItem(2).setVisible(true);
            menu.getItem(4).setVisible(true);
            menu.getItem(5).setVisible(true);
            menu.getItem(6).setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case R.id.login:
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivityForResult(loginIntent,REQUEST_LOGIN);//로그인요청
                break;

            case R.id.logout:
                if (userStatus == STATE_LOGIN) {
                    userStatus = STATE_LOGOUT;
                    userId = "";
                    Toast.makeText(this, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show();
                    Intent logOutIntent = new Intent(this, MainActivity.class);
                    startActivity(logOutIntent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "로그인이 필요한 서비스입니다", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.mypage:
                if (userStatus == STATE_LOGIN) {
                    Intent mypageIntent = new Intent(this, MypageActivity.class);
                    mypageIntent.putExtra("userId", userId);
                    startActivityForResult(mypageIntent, 10);

                } else {
                    Toast.makeText(MainActivity.this, "로그인이 필요한 서비스입니다", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.controller:
                if (mDevice == null || mService == null){
                    Toast.makeText(this, "먼저 쿨링글러브와 블루투스 연결을 해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                } else {
                    Intent intent = new Intent(MainActivity.this, CoolingControl.class);
                    intent.putExtra("savedCoolingStateTx", coolingState);
                    intent.putExtra("savedCoolingStateVal", savedCoolingState);
                    intent.putExtra("savedModeState", modeState);
                    if(userId != null && !"".equals(userId)){
                        intent.putExtra("userId",userId);
                        Log.i(TAG,"//////CONTROOLER SEND USERID" + userId);

                        Networking networking = new Networking();
                        String tempValue = networking.loadingTargetTemp(userId);
                        intent.putExtra("targetTemp", tempValue);
                    }

                    startActivityForResult(intent, REQUEST_COOLING_CONTROL);
                    Log.i(TAG, "///////////////" + modeState);
                }
                break;

            case R.id.exerciseRegister :
                Intent intent = new Intent(MainActivity.this, ExercisePlanActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                break;

            case R.id.bluetooth :
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                    if (mDevice == null){

                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    } else {
                        //Disconnect button pressed
                        if (mDevice!=null)
                        {
                            mService.disconnect();

                            Log.i("menu DISCONNECT PRESSED","///////////////+++++++++////");

                        }
                    }
                }
                break;

            case R.id.heartrate :
                Intent hintent = new Intent(MainActivity.this, HeartChange.class);
                hintent.putExtra("userId",userId);
                hintent.putExtra("bpm",bpm);
                startActivity(hintent);
                break;

            case R.id.temperature :
                Intent tintent = new Intent(MainActivity.this, TempChange.class);
                tintent.putExtra("userId",userId);
                tintent.putExtra("bodyTemp",temp1);
                tintent.putExtra("mode", modeState);
                tintent.putExtra("level", savedCoolingState);
                startActivity(tintent);
                break;
        }
        return true;
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override

        //Handler events that received from UART service
        public void handleMessage(Message msg) {

        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
//                        btnConnectDisconnect.setText("Disconnect");
//                        edtMessage.setEnabled(true);
//                        btnSend.setEnabled(true);
                        ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " 와 연결됨");
//                        listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
//                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        byte[] value;
                        try {
                            //send data to service
                            value = "0".getBytes("UTF-8");  //안전한 disconnection을 위해 마지막으로 쿨링 레벨 0 값을 주고 연결 해제
                            mService.writeRXCharacteristic(value);
                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
//                        btnConnectDisconnect.setText("Connect");
//                        edtMessage.setEnabled(false);
//                        btnSend.setEnabled(false);
                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
//                        listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        mDevice = null;
                        //setUiState();
                    }
                });
                Toast.makeText(MainActivity.this, "모듈과 연결을 해제하였습니다.", Toast.LENGTH_SHORT).show();
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                            listAdapter.add("["+currentDateTimeString+"] RX: "+text);
//                            messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                            updateValue(text);
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }

            if (action.equals(CoolingControl.ACTION_LEVEL_CONTROL)){
                coolingState = intent.getStringExtra("CoolingStateTx");
                savedCoolingState = Integer.parseInt(intent.getStringExtra("CoolingStateVal"));

                Log.i(TAG, "COOLING CONTROLLER BROADCAST RECEIVER OPERATED/////////////   " + coolingState);
                byte[] value;
                try {
                    //send data to service
                    value = coolingState.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
//                    croller.setProgress(savedCoolingState + 1);
                    //Update the log with time stamp
//                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                    listAdapter.add("["+currentDateTimeString+"] TX: "+ coolingState);
//                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
//                    edtMessage.setText("");
//                    tvLevel.setText(String.valueOf(savedCoolingState+1));

//                    Toast.makeText(MainActivity.this, "레벨을 " + (savedCoolingState + 1) +" (으)로 조정하였습니다.", Toast.LENGTH_SHORT).show();

                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (action.equals(CoolingControl.ACTION_MODE_CHANGE)){
                modeState = intent.getStringExtra(CoolingControl.STRING_EXTRA_DATA);
                coolingState = intent.getStringExtra("CoolingStateTx");

                Log.i(TAG, "COOLING MODE CHANGE/////////////" + modeState);
                Log.i(TAG, "COOLING STATE /////////////" + coolingState);
                byte[] value;
                try{
                    value = modeState.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    value = coolingState.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
//                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                    listAdapter.add("["+currentDateTimeString+"] TX: "+ modeState);
//                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
//                    edtMessage.setText("");
//
//                    if (modeState.equals(CoolingControl.COOL)){
//                        tvMainMode.setText("쿨링");
//                    } else if (modeState.equals(CoolingControl.HEAT)){
//                        tvMainMode.setText("히팅");
//                    }

                    if (modeState == CoolingControl.COOL){
                        Toast.makeText(getApplicationContext(), "냉각모드로 변경하였습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "온열모드로 변경하였습니다.", Toast.LENGTH_SHORT).show();
                    }

                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
            }

            if (action.equals(MainActivity.ACTION_COMPARE_TEMP)){
                String modeStateForce, coolingStateForce;
                String targetTempString = intent.getStringExtra("targetTempValue");
                Double targetTemp = new Double(targetTempString);
                Double currentTemp = intent.getDoubleExtra("currentTempValue", 35);

                Log.i(TAG, "targetTempValue/////////////" + targetTemp);
                Log.i(TAG, "currentTempValue/////////////" + currentTemp);

                if (targetTemp > currentTemp){
                    modeStateForce = CoolingControl.HEAT;
                    coolingStateForce = "j";
                } else if (currentTemp > targetTemp){
                    modeStateForce = CoolingControl.COOL;
                    coolingStateForce = "j";
                } else {
                    modeStateForce = modeState;
                    coolingStateForce = coolingState;
                }

                byte[] value;

                try{
                    value = modeStateForce.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    value = coolingStateForce.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
            }
        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        intentFilter.addAction(CoolingControl.ACTION_LEVEL_CONTROL);
        intentFilter.addAction(CoolingControl.ACTION_MODE_CHANGE);
        intentFilter.addAction(MainActivity.ACTION_COMPARE_TEMP);
        return intentFilter;
    }

    private void updateValue(String value){
        String [] rate = value.split(" ");

        Double i1 = Double.parseDouble(rate[2]);
        Double d1 = i1/100;

        Double i2 = Double.parseDouble(rate[3]);
        Double d2 = i2/100;

        //그래프액티비티에 넘겨줄값
        temp1 = d1.toString();
        temp2 = d2.toString();
        bpm = rate[0];

        if (userStatus == STATE_LOGIN) { //로그인 상태일때만 디비에 저장, 현재 온도와 목표 온도 비교하여 자동 조절
            Networking networking = new Networking();
            networking.sendHealthData(rate[0], d1.toString(), d2.toString(), userId);

            String tempValue = networking.loadingTargetTemp(userId);

            Log.i(TAG, "*********" + tempValue + "&&&&&&&&&");

            if (!tempValue.isEmpty()){
                Intent intent = new Intent(MainActivity.ACTION_COMPARE_TEMP);
                intent.putExtra("targetTempValue", tempValue);
                intent.putExtra("currentTempValue", d1);
                intent.putExtra("petierTempValue",d2);
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);

                Log.i(TAG, "//////////// Check if this method is called");
            }
        }
        //체온
//        tvCurrentTemp.setText(d2+"℃");
        //체온2
//        b2.setText(d2+"℃");
        //심박수
//        tvBpm.setText(rate[0]);
        //배터리
        tvBattery.setText(rate[1]);
        //메인
//        main.setText(d1+"˚");
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 10 :
                if (resultCode == RESULT_OK){
                    userStatus = data.getIntExtra("LOGIN_STATUS", STATE_LOGOUT);;
                }

                break;

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                    mService.connect(deviceAddress);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

//            case REQUEST_COOLING_CONTROL:
//
//                if (mDevice == null || mService == null){
//                    Toast.makeText(this, "먼저 쿨링글러브와 블루투스 연결을 해주세요.", Toast.LENGTH_SHORT).show();
//                    break;
//                }
//
//                if (resultCode == Activity.RESULT_OK && data != null ){
//                    coolingState = data.getStringExtra("CoolingStateTx");
//                    savedCoolingState = Integer.parseInt(data.getStringExtra("CoolingStateVal")) - 1;
//                    Log.d("CoolingStateTEST_1", coolingState);
//
//                    byte[] value;
//                    try {
//                        //send data to service
//                        value = coolingState.getBytes("UTF-8");
//                        mService.writeRXCharacteristic(value);
//                        //Update the log with time stamp
//                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                        listAdapter.add("["+currentDateTimeString+"] TX: "+ coolingState);
//                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
//                        edtMessage.setText("");
//
//                        Toast.makeText(this, "쿨링 레벨을 " + (savedCoolingState + 1) +" (으)로 조정하였습니다.", Toast.LENGTH_SHORT).show();
//
//                    } catch (UnsupportedEncodingException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//
//                break;

            case REQUEST_LOGIN :
                if (resultCode == Activity.RESULT_OK) {
                    userStatus = STATE_LOGIN;
                    userId = data.getStringExtra("ID");
                    Toast.makeText(this, userId + " 님 반갑습니다", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }


    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("Glove S 가 백그라운드에서 동작합니다.\n종료하려면 블루투스 연결을 끊어주세요.");
        }
        else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
        }
    }
}
