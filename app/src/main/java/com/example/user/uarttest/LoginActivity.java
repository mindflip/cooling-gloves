package com.example.user.uarttest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    EditText inputId, inputPw;
    HttpPost httpPost;
    HttpResponse response;
    HttpClient httpClient;
    List<NameValuePair> nameValuePairs;
    ProgressDialog dialog = null;
    TextView tv;

    private Button btnLogin, btnSignup;

    final static String url = ServerIP.getServerIP();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        inputId = (EditText)  findViewById(R.id.etEmail);
        inputPw = (EditText)  findViewById(R.id.etPassword);
        tv = (TextView) findViewById(R.id.textView2);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnSignup = (Button) findViewById(R.id.btnSignup);

        btnLogin.setOnClickListener(onClickListener);
        btnSignup.setOnClickListener(onClickListener);
    }

    Button.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnLogin:
                    dialog = ProgressDialog.show(LoginActivity.this,""
                            ,"Validating user...",true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String id = inputId.getText().toString();
                            String pw = inputPw.getText().toString();

                            login(id, pw);
                        }
                    }).start();
                    break;

                case R.id.btnSignup:
                    Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    void login(String id, String pw) {
        try {
            httpClient = new DefaultHttpClient();
            httpPost = new HttpPost(url+"login.php");
            nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("username",id));
            nameValuePairs.add(new BasicNameValuePair("password",pw));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpClient.execute(httpPost);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpClient.execute(httpPost, responseHandler);

//            System.out.println("Response : " + response);
            Log.i("Response", response);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv.setText("Response from PHP : " + response);
                    dialog.dismiss();
                }
            });

            if (response.equalsIgnoreCase("User Found")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "Login Success",Toast.LENGTH_SHORT).show();
                    }
                });
                Intent data = new Intent();
                data.putExtra("ID",id);
                setResult(Activity.RESULT_OK,data);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Login Fail",Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e) {
            dialog.dismiss();
            System.out.println("Exception :" + e.getMessage());
        }
    }
}
