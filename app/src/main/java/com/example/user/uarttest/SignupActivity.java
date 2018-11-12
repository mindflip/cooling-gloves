package com.example.user.uarttest;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignupActivity extends AppCompatActivity {

    private EditText etId;
    private EditText etPw;
    private EditText etPw_Chk;
    private EditText etNn;
    private Button btnDone, btnCancel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etId = (EditText) findViewById(R.id.etEmail);
        etPw = (EditText) findViewById(R.id.etPassword);
        etPw_Chk=(EditText) findViewById(R.id.etPasswordConfirm);
        etNn = (EditText) findViewById(R.id.etNickname);
        btnDone = (Button) findViewById(R.id.btnDone);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        btnDone.setOnClickListener(onClickListener);
        btnCancel.setOnClickListener(onClickListener);
    }

    Button.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnDone:
                    String Id = etId.getText().toString();
                    String Pw = etPw.getText().toString();
                    String Pw_Chk = etPw_Chk.getText().toString();
                    String Nn = etNn.getText().toString();
                    if (!Pw_Chk.equals(Pw)) {
                        Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_LONG).show();
                    } else if (Id.equals("")) {
                        Toast.makeText(getApplicationContext(), "아이디는 필수정보입니다.", Toast.LENGTH_LONG).show();
                    } else if (Pw.equals("") || Pw_Chk.equals("")) {
                        Toast.makeText(getApplicationContext(), "비밀번호는 필수정보입니다.", Toast.LENGTH_LONG).show();
                    } else if (Nn.equals("")) {
                        Toast.makeText(getApplicationContext(), "닉네임은 필수정보입니다.", Toast.LENGTH_LONG).show();
                    } else {
                        //insertToDatabase(Id, Pw, Nn);
                        Networking networking = new Networking();
                        networking.signUp(Id, Pw, Nn, SignupActivity.this);
                        finish();
                    }
                    break;

                case R.id.btnCancel:
                    finish();
                    break;
            }
        }
    };
}
