package com.example.user.uarttest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by user on 2018-03-19.
 */

public class CoolingControl extends AppCompatActivity {

    public final static String ACTION_LEVEL_CONTROL = "CoolingControl.ACTION_LEVEL_CONTROL";
    public final static String ACTION_MODE_CHANGE = "CoolingControl.ACTION_MODE_CHANGE";
    public final static String STRING_EXTRA_DATA = "CoolingControl.STRING_EXTRA_DATA";
    public final static String INT_EXTRA_DATA = "CoolingControl.INT_EXTRA_DATA";
    public final static String HEAT = "y";
    public final static String COOL = "x";

    private SeekBar sbCoolingController;
    private Button btnModeChange, btnCoolingState, btnRegisterTemp, btnUnregisterTemp;
    private EditText etTargetTemp;
    private TextView tvTargetTemp;
    private String[] controllerInput;
    private String showInput, txInput, savedTargetTemp;
    private String modeState = COOL;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cooling_control);

        sbCoolingController = (SeekBar) findViewById(R.id.sbCoolingController);
        btnModeChange = (Button) findViewById(R.id.btnModeChange);
        btnCoolingState = (Button) findViewById(R.id.btnCoolingState);
        etTargetTemp = (EditText) findViewById(R.id.etTargetTemp);
        tvTargetTemp = (TextView) findViewById(R.id.tvTargetTemp);
        btnRegisterTemp = (Button) findViewById(R.id.btnRegisterTemp);
        btnUnregisterTemp = (Button) findViewById(R.id.btnUnregisterTemp);

        controllerInput = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};

        Intent savedStateIntent = getIntent();

        txInput = savedStateIntent.getStringExtra("savedCoolingStateTx");
        int savedStateValue = savedStateIntent.getIntExtra("savedCoolingStateVal", 1);
        modeState = savedStateIntent.getStringExtra("savedModeState");

        if(savedStateIntent.getStringExtra("userId") != null){
            userId = savedStateIntent.getStringExtra("userId");
        }


        if(userId != null && !"".equals(userId)){
            savedTargetTemp = savedStateIntent.getStringExtra("targetTemp");

            if ("".equals(savedTargetTemp)){

            } else {
                tvTargetTemp.setText( "목표 온도 : " + savedTargetTemp +"℃");
            }
        }

        if(userId == null || "".equals(userId)){
            btnRegisterTemp.setEnabled(false);
            btnUnregisterTemp.setEnabled(false);
        }

        Log.i("initial modestate check", modeState);

        sbCoolingController.setProgress(savedStateValue);
        btnCoolingState.setText(String.valueOf(savedStateValue));
        showInput = String.valueOf(savedStateValue);

        if (modeState.equals(COOL)){
            btnModeChange.setText("COOLING");
        }else if (modeState.equals(HEAT)){
            btnModeChange.setText("HEATING");
        }

        sbCoolingController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                showInput = String.valueOf(i+1);
                btnCoolingState.setText(showInput);

                if(i > 9){
                    txInput = controllerInput[i-10];
                } else {
                    txInput = String.valueOf(i);
                }

                Intent intent = new Intent(ACTION_LEVEL_CONTROL);
                intent.putExtra("CoolingStateTx",txInput);
                intent.putExtra("CoolingStateVal",showInput);
//                setResult(RESULT_OK, intent);
                LocalBroadcastManager.getInstance(CoolingControl.this).sendBroadcast(intent);

                Log.d("State test", txInput);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnModeChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (modeState.equals(COOL)){
                    Intent intent = new Intent(ACTION_MODE_CHANGE);
                    intent.putExtra(STRING_EXTRA_DATA, HEAT);
                    txInput = "0";
                    intent.putExtra("CoolingStateTx",txInput);
                    LocalBroadcastManager.getInstance(CoolingControl.this).sendBroadcast(intent);

//                    tvMode.setText("HEAT MODE");
                    btnModeChange.setText("HEATING");
                    modeState = HEAT;
                    sbCoolingController.setProgress(0);
                    btnCoolingState.setText("1");
                    Log.i("Mode Change", "//////////////"+modeState);
                } else if (modeState.equals(HEAT)) {
                    Intent intent = new Intent(ACTION_MODE_CHANGE);
                    intent.putExtra(STRING_EXTRA_DATA, COOL);
                    txInput = "0";
                    intent.putExtra("CoolingStateTx",txInput);
                    LocalBroadcastManager.getInstance(CoolingControl.this).sendBroadcast(intent);

//                    tvMode.setText("COOL MODE");
                    btnModeChange.setText("COOLING");
                    modeState = COOL;
                    sbCoolingController.setProgress(0);
                    btnCoolingState.setText("1");
                    Log.i("Mode Change", "//////////////"+modeState);
                }

            }
        });

        btnRegisterTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("".equals(userId)){
                    Toast.makeText(CoolingControl.this, "", Toast.LENGTH_SHORT).show();
                }
                String targetTemp = etTargetTemp.getText().toString();

                if ( 20 <= Double.parseDouble(targetTemp) && Double.parseDouble(targetTemp) <= 40 ) {
                    Networking networking = new Networking();
                    networking.sendTargetTemp(targetTemp, userId);
                    Toast.makeText(CoolingControl.this , "목표 온도를 " + targetTemp + "℃로 설정하였습니다.", Toast.LENGTH_SHORT).show();
                    tvTargetTemp.setText(targetTemp + "℃");
                } else {
                    Toast.makeText(CoolingControl.this, "20 과 40 사이의 수를 입력하십시오.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnUnregisterTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Networking networking = new Networking();
                networking.unregisterTargetTemp(userId);
                Toast.makeText(CoolingControl.this, "목표 온도를 해제하였습니다.", Toast.LENGTH_SHORT).show();
                tvTargetTemp.setText("목표 온도");
            }
        });
    }
}