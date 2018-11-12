package com.example.user.uarttest;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
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

/**
 * Created by user on 2018-03-22.
 */

public class Networking {

    final static String url = ServerIP.getServerIP();

    String userId;
    String bpm;
    HttpPost httpPost;
    StringBuffer buffer;
    HttpResponse response;
    HttpClient httpClient;
    List<NameValuePair> nameValuePairs;

    public Networking() {

    }

    public void signUp(String id, String pw, String nn, final Context context) {
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;
            @Override
            protected String doInBackground(String... params) {
                try {
                    String id = (String) params[0];
                    String pw = (String) params[1];
                    String nn = (String) params[2];

                    Log.i("Networking_1", id);

                    String link = url+ "signup.php";
                    String data = URLEncoder.encode("Id","UTF-8") + "=" + URLEncoder.encode(id,"UTF-8");
                    data += "&" + URLEncoder.encode("Pw","UTF-8") + "=" + URLEncoder.encode(pw, "UTF-8");
                    data += "&" + URLEncoder.encode("Nn", "UTF-8") + "=" + URLEncoder.encode(nn, "UTF-8");
                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    Log.i("Networking_2", id);

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    Log.i("Networking_3", id);

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    Log.i("Networking_4", id);

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
                loading = ProgressDialog.show(context, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(context,s, Toast.LENGTH_LONG).show();
            }
        }
        InsertData task = new InsertData();
        task.execute(id, pw, nn);
    }

    void sendHealthData(String heartRate, String bodyTemp, String peltierTemp, String userId) { // 체온, 심박수 디비에추가
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;
            @Override
            protected String doInBackground(String... params) {
                try {
                    String heartRate = (String) params[0];
                    String bodyTemp = (String) params[1];
                    String peltierTemp = (String) params[2];
                    String userId = (String) params[3];

                    Log.i("SENDHEALTHDATA", "/////////////" + userId);

                    String link = url + "health_info_insert.php";
                    String data = URLEncoder.encode("UID", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8");
                    data += "&" + URLEncoder.encode("HeartRate","UTF-8") + "=" + URLEncoder.encode(heartRate,"UTF-8");
                    data += "&" + URLEncoder.encode("BodyTemp","UTF-8") + "=" + URLEncoder.encode(bodyTemp, "UTF-8");
                    data += "&" + URLEncoder.encode("PeltierTemp","UTF-8") + "=" + URLEncoder.encode(peltierTemp, "UTF-8");
                    data += "&" + URLEncoder.encode("classification","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8");

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
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }
        }
        InsertData task = new InsertData();
        task.execute(heartRate, bodyTemp, peltierTemp, userId);
    }

    void sendTargetTemp(String targetTemp, String userId) { // 목표 온도 디비에추가
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;
            @Override
            protected String doInBackground(String... params) {
                try {
                    String targetTemp = (String) params[0];
                    String userId = (String) params[1];

                    String link = url + "health_info_insert.php";
                    String data = URLEncoder.encode("UID", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8");
                    data += "&" + URLEncoder.encode("TargetTemp","UTF-8") + "=" + URLEncoder.encode(targetTemp, "UTF-8");
                    data += "&" + URLEncoder.encode("classification","UTF-8") + "=" + URLEncoder.encode("2", "UTF-8");

                    Log.i("sendTargetTemp", data);

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
//                    Toast.makeText(CoolingControl.class , "목표 온도를 " + targetTemp + "℃로 설정하였습니다.", Toast.LENGTH_SHORT).show();
                    return sb.toString();
                } catch(Exception e) {
                    return new String("Exception: " +e.getMessage());
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }
        }
        InsertData task = new InsertData();
        task.execute(targetTemp, userId);
    }

    void unregisterTargetTemp(String userId) { // 목표 온도 디비에추가
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;
            @Override
            protected String doInBackground(String... params) {
                try {
                    String userId = (String) params[0];

                    String link = url + "unregister_target_temp.php";
                    String data = URLEncoder.encode("UID", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8");

                    Log.i("sendTargetTemp", data);

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
//                    Toast.makeText(CoolingControl.class , "목표 온도를 " + targetTemp + "℃로 설정하였습니다.", Toast.LENGTH_SHORT).show();
                    return sb.toString();
                } catch(Exception e) {
                    return new String("Exception: " +e.getMessage());
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }
        }
        InsertData task = new InsertData();
        task.execute(userId);
    }

    public void registerExInfo(String id, String exType, String exTime, final Context context) {
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;
            @Override
            protected String doInBackground(String... params) {
                try {
                    String id = (String) params[0];
                    String exType = (String) params[1];
                    String exTime = (String) params[2];

                    String link = url+ "register_exercise.php";
                    String data = URLEncoder.encode("Id","UTF-8") + "=" + URLEncoder.encode(id,"UTF-8");
                    data += "&" + URLEncoder.encode("exType","UTF-8") + "=" + URLEncoder.encode(exType, "UTF-8");
                    data += "&" + URLEncoder.encode("exTime", "UTF-8") + "=" + URLEncoder.encode(exTime, "UTF-8");
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
                loading = ProgressDialog.show(context, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(context,s, Toast.LENGTH_LONG).show();
            }
        }
        InsertData task = new InsertData();
        task.execute(id, exType, exTime);
    }

    String loadingTargetTemp(String userId) {
        try {
            httpClient = new DefaultHttpClient();
            httpPost = new HttpPost(url +"load_target_temp.php");
            nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("u_id",userId));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpClient.execute(httpPost);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String tempValue = httpClient.execute(httpPost, responseHandler);
            Log.d("TargetTemp", tempValue);
            System.out.println("Response : " + tempValue);
            return tempValue;
//            final String[] healthData = response.split(" ");
//            for(int i=0; i< healthData.length; i++) {
//
//
//            }

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    void deleteUser(String userId) {
        try {
            httpClient = new DefaultHttpClient();
            httpPost = new HttpPost(url+"delete_user.php");
            nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("u_id",userId));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpClient.execute(httpPost);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpClient.execute(httpPost, responseHandler);
            System.out.println("Response : " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
