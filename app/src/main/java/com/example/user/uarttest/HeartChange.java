package com.example.user.uarttest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.audiofx.AudioEffect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.EventLogTags;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

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

public class HeartChange extends AppCompatActivity {

    String userId;
    String bpm;
    HttpPost httpPost;
    StringBuffer buffer;
    HttpResponse response;
    HttpClient httpClient;
    List<NameValuePair> nameValuePairs;
    ProgressDialog dialog = null;

    ArrayList<String> labels = new ArrayList<String>();
    ArrayList<Entry> entries = new ArrayList<>();

    TextView tv;

    final static String url = ServerIP.getServerIP();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_change);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        bpm = intent.getStringExtra("bpm");

        // 스레드화해서 실시간으로 받아올수 있게 할예정
        loadingData();
        //그래프 추가

        tv = findViewById(R.id.tvChartBpm);
        tv.setText(bpm);
        LineChart lineChart = (LineChart) findViewById(R.id.chart_heart_rate);

        LineDataSet lineDataSet = new LineDataSet(entries, "");

        lineDataSet.setColor(ColorTemplate.VORDIPLOM_COLORS[2]);
        lineDataSet.setDrawCubic(true);
        lineDataSet.setDrawFilled(true); //선아래로 색상표시
        lineDataSet.setDrawValues(true);
        lineDataSet.setFillColor(ColorTemplate.VORDIPLOM_COLORS[2]);
        lineDataSet.setValueTextSize(10);
        lineDataSet.setValueTextColor(ColorTemplate.JOYFUL_COLORS[4]);

        LineData lineData = new LineData(labels, lineDataSet);
        lineChart.setData(lineData);

        //MarkerView mv = new MarkerView(this,);

        // lineChart.setMarkerView(mv);

        //lineChart.setDrawMarkerViews(true);

        //YAxis y = lineChart.getAxisLeft();
        //y.setTextColor(Color.WHITE);
        lineChart.getAxisLeft().setEnabled(false); //왼쪽 축 숨김

        XAxis x = lineChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setTextColor(Color.BLACK);
        x.setDrawGridLines(false);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getAxisRight().setDrawGridLines(false);

        lineChart.setDescription(null);
        lineChart.setGridBackgroundColor(ColorTemplate.COLOR_NONE);

        Legend legend = lineChart.getLegend();

        legend.setTextColor(Color.WHITE);
        lineChart.getLegend().setEnabled(false); // Hide the legend
        lineChart.animateXY(2000, 2000); //애니메이션 기능 활성화
        lineChart.invalidate();

        //그래프 추가 끝
    }

    void loadingData() {
        try {
            httpClient = new DefaultHttpClient();
            httpPost = new HttpPost(url +"load_health_hr.php");
            nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("u_id",userId));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpClient.execute(httpPost);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpClient.execute(httpPost, responseHandler);
            //Log.d("################",""+response);
            System.out.println("Response : " + response);
            final String[] healthData = response.split(" ");
            for(int i=0; i< healthData.length; i++) {
                labels.add(healthData[i*2]);
                entries.add(new Entry(Float.valueOf(healthData[i*2+1]),i));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
