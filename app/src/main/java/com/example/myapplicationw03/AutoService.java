package com.example.myapplicationw03;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import weka.classifiers.lazy.IBk;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.SerializationHelper;

public class AutoService extends Service {
    public AutoService() {
    }

    DBHelper helper;
    SQLiteDatabase db;

    public static boolean exeg;

    private boolean isStop;
    private static int count = 0;
    private boolean trig1;

    public static ArrayList<Double> arrArray = new ArrayList<>();

    private boolean isSendable = false;

    private RequestQueue requestQueue;
    private static final String TAG = "Main";

    double[][][] candleArray = new double[120][3][3];

    private ArrayList<String> as = new ArrayList<>();

    IAutoInterface.Stub binder = new IAutoInterface.Stub() {
        @Override
        public int getSomething() throws RemoteException {
            return count;
        }
        @Override
        public boolean getExeg() throws RemoteException {
            return exeg;
        }
        @Override
        public boolean getSendable() throws RemoteException {
            return isSendable;
        }
        @Override
        public void setSendable() throws RemoteException {
            isSendable = false;
        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        helper = new DBHelper(AutoService.this, "coin.db", null, 1);
        db = helper.getWritableDatabase();
        helper.onCreateString(db,"create table if not exists coin(_id integer primary key autoincrement, name text);");

        if (requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        isSendable = false;
        trig1 = true;
        candleArray[119][0][0] = 5;

        exeg = false;

        new Thread(new autoBuySell()).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStop = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isStop = true;
        return super.onUnbind(intent);
    }

    private class autoBuySell implements Runnable {

        private Handler handler = new Handler();

        @Override
        public void run() {

            while(true) {

                if(isStop) {

                    break;

                }

                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        int tm = Integer.parseInt(new SimpleDateFormat("mm").format(new Date()));

                        arrArray.clear();

                        ArrayList<String> strList = helper.getTableData(db, "coin");

                        String asdf = "";

                        getCandle3("BTC", 119);
                        int tm2 = (int) Math.round(candleArray[119][0][0]);

                        for(int i = 0; i < strList.size(); ++i) {
                            try{
                                getCandle3(strList.get(i), i);

                                try {
                                    Thread.sleep(200);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                AssetManager assetManager = getResources().getAssets();
                                InputStream inputStream= assetManager.open("KRW-" + strList.get(i) + ".model");
                                IBk ibk = (IBk) SerializationHelper.read(inputStream);

                                Instance qwer = new DenseInstance(7);
                                qwer.setValue(0, candleArray[i][1][0]);
                                qwer.setValue(1, candleArray[i][1][1]);
                                qwer.setValue(2, candleArray[i][1][2]);
                                qwer.setValue(3, candleArray[i][2][0]);
                                qwer.setValue(4, candleArray[i][2][1]);
                                qwer.setValue(5, candleArray[i][2][2]);
                                asdf = asdf + Double.toString(candleArray[i][0][0]) + " ";

                                double[] sdfsdf = ibk.distributionForInstance(qwer);
                                System.out.println(sdfsdf[0]);
                                System.out.println(candleArray[0][1][0]);
                                System.out.println(candleArray[1][1][0]);
                                asdf = asdf + Double.toString(sdfsdf[0]) + " ";
                                arrArray.add(sdfsdf[0]);

                            } catch(Exception e) {
                                e.printStackTrace();
                            }

                        }

                        boolean trigg = true;

                        for(int i = 0; i < strList.size(); ++i) {

                            if(candleArray[i][0][0] != candleArray[0][0][0]) trigg = false;

                        }

                        if(trig1 && trigg && tm == tm2 && tm2 == candleArray[0][0][0]) {
                            Toast.makeText(getApplicationContext(), asdf, Toast.LENGTH_SHORT).show();
                            System.out.println(asdf);
                            trig1 = false;
                            exeg = true;
                        }

                        if(tm % 10 != 0) trig1 = true;



                        //Toast.makeText(getApplicationContext(), "aaaa", Toast.LENGTH_SHORT).show();
                        count += 1;
                        isSendable = true;

                    }

                });

                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "exit", Toast.LENGTH_SHORT).show();
                }

            });

        }

    }

    private void getCandle3(String coinNm, int q){

        String coinName = coinNm;

        String url = "https://api.upbit.com/v1/candles/minutes/10?market=KRW-"+coinName+"&count=3";
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try{
                            JSONArray jsonArray = new JSONArray(response);

                            for(int i=0; i<jsonArray.length();i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                String sOpening_price = jsonObject.get("opening_price").toString();//시가
                                String sHigh_price = jsonObject.get("high_price").toString();//고가
                                String sLow_price = jsonObject.get("low_price").toString();//저가
                                String sTrade_price = jsonObject.get("trade_price").toString();//종가
                                String sTime = jsonObject.get("candle_date_time_kst").toString().substring(14,16);//종가

                                double o_p_candle = Double.valueOf(sOpening_price);
                                double h_p_candle = Double.valueOf(sHigh_price);
                                double l_p_candle = Double.valueOf(sLow_price);
                                double t_p_candle = Double.valueOf(sTrade_price);

                                System.out.println(jsonObject.get("market").toString());

                                if(i==0){
                                    candleArray[q][i][0] = Double.parseDouble(sTime);
                                    candleArray[q][i][1] = 0;
                                    candleArray[q][i][2] = 0;
                                    continue;
                                }
                                candleArray[q][i][0] = t_p_candle/o_p_candle;//종가/시가
                                candleArray[q][i][1] = h_p_candle/o_p_candle;//고가/시가
                                candleArray[q][i][2] = l_p_candle/o_p_candle;//저가/시가
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse"+error.getMessage());
                    }
                }){
        };
        request.setShouldCache(false);
        requestQueue.add(request);
        requestQueue.start();
    }
}