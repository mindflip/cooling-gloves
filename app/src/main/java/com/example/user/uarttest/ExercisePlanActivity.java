package com.example.user.uarttest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
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
import java.util.ListIterator;

public class ExercisePlanActivity extends AppCompatActivity {

    private TextView tvExercise, tvExType, tvExTime;
    private EditText etExType, etExTime;
    private Button btnExRegister, btnDeleteEx;
    private String userId;
    private ListView lvExercise;

    ListAdapter listAdapter;
    ArrayList<ListItem> listItem;

    HttpPost httpPost;
    HttpResponse response;
    HttpClient httpClient;
    List<NameValuePair> nameValuePairs;
    ProgressDialog dialog = null;

    final static String url = ServerIP.getServerIP();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_plan);

        tvExercise = (TextView) findViewById(R.id.tvExercise);
        tvExType = (TextView) findViewById(R.id.tvExType);
        tvExTime = (TextView) findViewById(R.id.tvExTime);
        etExType = (EditText) findViewById(R.id.etExType);
        etExTime = (EditText) findViewById(R.id.etExTime);
        btnExRegister = (Button) findViewById(R.id.btnExRegister);
        lvExercise = (ListView) findViewById(R.id.lvExercise);

        btnExRegister.setOnClickListener(onClickListener);

        listItem = new ArrayList<ListItem>();
        listAdapter = new ListAdapter(this, listItem);
        lvExercise.setAdapter(listAdapter);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        Log.i("ExercisePlanActivity", userId);

        lvExercise.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(ExercisePlanActivity.this, listItem.get(i).getExType(), Toast.LENGTH_SHORT).show();
                Log.i("LISTITEMCLICK", "&&&&&&&&&&&& LISTCLICKED" );
            }
        });

        loadingData();
    }

    Button.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnExRegister :
                    String exType = etExType.getText().toString();
                    String exTime = etExTime.getText().toString();
                    if(exType.equals("")){
                        Toast.makeText(ExercisePlanActivity.this, "운동 종류를 입력하세요.", Toast.LENGTH_SHORT).show();
                    } else if(exTime.equals("")){
                        Toast.makeText(ExercisePlanActivity.this, "목표 운동 시간을 입력하세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        Networking networking = new Networking();
                        networking.registerExInfo(userId, exType, exTime, ExercisePlanActivity.this);

                        listItem.clear();
                        loadingData();
                        listAdapter.notifyDataSetChanged();
                    }
                    etExType.setText(null);
                    etExTime.setText(null);
                    break;
            }
        }
    };

    void loadingData() {
        try {
            httpClient = new DefaultHttpClient();
            httpPost = new HttpPost(url+"load_exercise.php");
            nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("u_id",userId));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpClient.execute(httpPost);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpClient.execute(httpPost, responseHandler);
//            Log.d("################",""+response);
            System.out.println("Response : " + response);
            final String[] healthData = response.split(" ");
            Log.d("###########", String.valueOf(healthData.length));
            for (int i = 0; i < healthData.length/3; i++){
                listItem.add(new ListItem(healthData[i*3], healthData[i*3+1], healthData[i*3+2]));
                Log.d("###########", healthData[i*3] + "  /  " + healthData[i*3+1] + "  /  " + healthData[i*3+2]);
            }

//            Log.i("LISTITEM CHECK", "///////////////////" + listItem);
//            for(int i=0; i< healthData.length; i++) {
//                labels.add(healthData[i*2]);
//                entries.add(new Entry(Float.valueOf(healthData[i*2+1]),i));
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void deleteData(String id) {
        try {
            httpClient = new DefaultHttpClient();
            httpPost = new HttpPost(url+"delete_exercise.php");
            nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("u_id",userId));
            nameValuePairs.add(new BasicNameValuePair("id", id));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpClient.execute(httpPost);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpClient.execute(httpPost, responseHandler);
            System.out.println("Response : " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ListAdapter extends BaseAdapter {

        TextView tvExType, tvExTime;

        Context context;
        ArrayList<ListItem> listItem;

        ListAdapter (Context context, ArrayList<ListItem> listItem){
            this.context = context;
            this.listItem = listItem;
        }

        @Override
        public int getCount() {
            Log.i("Size of list item", "/////////////////////" + this.listItem.size());
            return this.listItem.size();
        }

        @Override
        public Object getItem(int position) {
            return this.listItem.get(position);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final int position = i;
            if(view == null){
                view = LayoutInflater.from(context).inflate(R.layout.exercise_element,null);
                tvExType = (TextView) view.findViewById(R.id.tvExType);
                tvExTime = (TextView) view.findViewById(R.id.tvExTime);

                tvExType.setText(listItem.get(i).getExType());
                tvExTime.setText(listItem.get(i).getExTime());

                Log.i("GETVIEW", "**********" + i + "  /  " + listItem.get(i).getExType() + "  /  " + listItem.get(i).getExTime());
            }

            btnDeleteEx = (Button) view.findViewById(R.id.btnDeleteEx);
            btnDeleteEx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteData(listItem.get(position).getId());
                    listItem.remove(position);
                    listDelUpdate();
                }
            });
            return view;
        }
//
//        @Override
//        public void onClick(View view) {
//            int position = (Integer) view.getTag();
//            Log.i("POSITION", "//////////      " + position);
//            ListItem mListItem = listItem.get(position);
//
//            Intent intent = new Intent();
//            intent.putExtra("ExType", mListItem.getExType());
//        }
    }

    void listDelUpdate(){

        listAdapter.notifyDataSetChanged();

    }

    class ListItem {
        private String id;
        private String exType;
        private String exTime;

        public ListItem(String id, String exType, String exTime) {
            this.id = id;
            this.exType = exType;
            this.exTime = exTime;
        }

        public String getExType() {
            return exType;
        }

        public String getExTime() {
            return exTime;
        }

        public String getId() {
            return id;
        }
    }
}
