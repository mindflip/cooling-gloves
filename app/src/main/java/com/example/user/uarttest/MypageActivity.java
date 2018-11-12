package com.example.user.uarttest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MypageActivity extends AppCompatActivity {
    final static String url = ServerIP.getServerIP();
    String userId;
    HttpPost httpPost;
    StringBuffer buffer;
    HttpResponse response;
    HttpClient httpClient;
    List<NameValuePair> nameValuePairs;
    ProgressDialog dialog = null;
    TextView mypage_nick;
    RadioGroup rg;
    RadioButton rb;

    EditText etHeight;
    EditText etWeight;
    EditText etAge;

    Button btnDeleteUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        mypage_nick = (TextView) findViewById(R.id.mypage_usernick);
        etHeight = (EditText) findViewById(R.id.editText);
        etWeight = (EditText) findViewById(R.id.editText2);
        etAge = (EditText) findViewById(R.id.editText3);
        rg = (RadioGroup) findViewById(R.id.rg);
        btnDeleteUser = (Button) findViewById(R.id.btnDeleteUser);

        loadingData();

        btnDeleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Networking networking = new Networking();
                networking.deleteUser(userId);

                Intent intent = new Intent(MypageActivity.this, MainActivity.class);
                intent.putExtra("LOGIN_STATUS", 0);
                setResult(RESULT_OK, intent);

                Toast.makeText(MypageActivity.this, userId + "님의 회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show();

                finish();

            }
        });
    }

    public void savingDataonClick(View view) {
        String h = etHeight.getText().toString();
        String w = etWeight.getText().toString();
        String a = etAge.getText().toString();
        int radioId = rg.getCheckedRadioButtonId();
        rb = (RadioButton) findViewById(radioId);
        String s = rb.getText().toString();
        savingData(userId,h,w,a,s);
    }

    void savingData(String u_id, String height, String weight, String age, String sex) {
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;
            @Override
            protected String doInBackground(String... params) {
                try {
                    String uid = (String) params[0];
                    String h = (String) params[1];
                    String w = (String) params[2];
                    String a = (String) params[3];
                    String s = (String) params[4];

                    String link = url+"edit_mypage.php";
                    String data = URLEncoder.encode("UID", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8");
                    data += "&" + URLEncoder.encode("Height","UTF-8") + "=" + URLEncoder.encode(h,"UTF-8");
                    data += "&" + URLEncoder.encode("Weight","UTF-8") + "=" + URLEncoder.encode(w, "UTF-8");
                    data += "&" + URLEncoder.encode("Age", "UTF-8") + "=" + URLEncoder.encode(a, "UTF-8");
                    data += "&" + URLEncoder.encode("Sex", "UTF-8") + "=" + URLEncoder.encode(s, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    //Read Server Response
                    while ( ( line = reader.readLine() ) != null) {
                        sb.append(line);
                        break;
                    }
                    return sb.toString();
                } catch(Exception e) {
                    return new String("Exception: " +e.getMessage());
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MypageActivity.this, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getApplication(),s,Toast.LENGTH_LONG).show();
            }
        }
        InsertData task = new InsertData();
        task.execute(u_id, height, weight, age, sex);
    }

    void loadingData() {
        try {
            httpClient = new DefaultHttpClient();
            httpPost = new HttpPost(url+"mypage.php");
            nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("u_id",userId));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpClient.execute(httpPost);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpClient.execute(httpPost, responseHandler);
            //Log.d("################",""+response);
            System.out.println("Response : " + response);
            final String[] mypageData = response.split(" ");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //dialog.dismiss();
                    mypage_nick.setText(mypageData[0]+"님의 마이페이지입니다");
                    etHeight.setText(""+mypageData[1]);
                    etWeight.setText(""+mypageData[2]);
                    etAge.setText(""+mypageData[3]);
                    if (mypageData[4].equalsIgnoreCase("남자")) {
                        rb = findViewById(R.id.radio0);
                    } else {
                        rb = findViewById(R.id.radio1);
                    }
                    rb.setChecked(true);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
