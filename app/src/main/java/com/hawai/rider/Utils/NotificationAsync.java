package com.hawai.rider.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by AliAh on 01/03/2018.
 */

public class NotificationAsync extends AsyncTask<String, String, String> {

    String output = "";

    public static String status = "";
    Context context;

    NotificationObserver observer;
    String msgId;

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public final static String AUTH_KEY_FCM_LIVE = "AAAAh6pZ8YQ:APA91bGs39_Sd3GjQaoEEazl4oOnEq8GezQq6NiYAihPc9KIWZWFsmQ6m-rD85J7HXBud9Jw9FLAsiDM3F2UcrBtuTuv_UISEKLF3dtPm6w2nLIgNIBnfPMa4d2KUPo_r5o63wGlMyAV";
    public final static String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";

    public NotificationAsync(Context context) {
        this.context = context;
        observer=(NotificationObserver) context;

    }

    @Override
    protected String doInBackground(String... params) {
        URL url;
        String param1 = params[0];
        String sendTo = params[1];
        String Title = params[2];
        String Message = params[3];
        String MessageType = params[4];
        String NotificationType = params[5];
        String Id = params[6];
        String ChannelId = params[7];
        String PictureUrl = params[8];



        try {
            url = new URL(API_URL_FCM);


            HttpURLConnection conn = (HttpURLConnection) url.openConnection();


            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(60000);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "key=" + AUTH_KEY_FCM_LIVE);
            conn.setRequestProperty("Content-Type", "application/json");


            JSONObject json = new JSONObject();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Title", Title);
            jsonObject.put("Message", Message);
            jsonObject.put("Type", NotificationType);
            jsonObject.put("Id",Id);
            jsonObject.put("ChannelId",ChannelId);
            jsonObject.put("PictureUrl",PictureUrl);


            json.put("data", jsonObject);
            json.put("to", sendTo);
            json.put("priority", "high");


            Log.d("json", "" + json);


            OutputStreamWriter wr = new OutputStreamWriter(
                    conn.getOutputStream());
            wr.write(json.toString());
            wr.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));


            while ((output = br.readLine()) != null) {
//            Toast.makeText(context, ""+output, Toast.LENGTH_SHORT).show();
                Log.d("output", output);
                JSONObject jsonObject1= new JSONObject(output);
                String abc=jsonObject1.getString("success");
                if(abc.equals("1")){
                    observer.onSuccess(msgId);
                }

            }

        } catch (Exception e) {
//        Toast.makeText(context, "erroor "+e, Toast.LENGTH_SHORT).show();
            Log.d("exception", "" + e);

        }

        return null;
    }
}
