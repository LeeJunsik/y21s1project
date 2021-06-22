package com.example.myapplicationw03;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.util.EntityUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import weka.classifiers.lazy.IBk;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class transactionWrapper {
    String market = "";
    String side = "";
    String volume = "";
    String price = "";
    String vp = "";
    String fee = "";
    String vpf = "";
}

public class MainActivity extends AppCompatActivity {

    public static String accessKey = "weuK5yTGPsurw4k6QXxXv2qPIv5928lq7uOq1vtb";
    public static String secretKey = "OW52qLEtAatnehfNnEuzsoL6Sqk7m8NCD9xpBSxv";
    public static String serverUrl = "https://api.upbit.com";

    public boolean trigx = true;

    Button update_btn;
    Button s_start;
    Button s_stop;
    TextView text2;
    TextView text3;
    DBHelper helper;
    SQLiteDatabase db;

    ArrayList<String> sCurrency = new ArrayList<>();//화폐종류
    ArrayList<String> sBalance = new ArrayList<>();//주문가능 금액/수량
    ArrayList<String> sLocked = new ArrayList<>();//주문 중 묶여있는 금액
    ArrayList<String> sAvg_buy_price = new ArrayList<>();//매수 평균가

    ArrayList<String> trans = new ArrayList<>();

    private static final String TAG = "Main";
    String MyAccountStr = "";

    String code = "BTC";

    String aa = "gdgdasdfasdf";
    int bb = 1234;
    String cc = "zvzvasdfasdfasdf";

    private IAutoInterface binder;

    private boolean running = true;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = IAutoInterface.Stub.asInterface(service);
            text3.setText("Service Running");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            text3.setText("Service Stopped");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        update_btn = (Button)findViewById(R.id.update_btn);
        s_start = (Button)findViewById(R.id.s_start);
        s_stop = (Button)findViewById(R.id.s_stop);
        text2 = (TextView) findViewById(R.id.text2);
        text3 = (TextView) findViewById(R.id.text3);
        helper = new DBHelper(MainActivity.this, "coin.db", null, 1);
        db = helper.getWritableDatabase();
        helper.onCreateString(db,"create table if not exists trans(_id integer primary key autoincrement, market text, side text, volume text, price text, vp text, fee text, vpf text);");
        helper.onCreateString(db,"create table if not exists coin(_id integer primary key autoincrement, name text);");

        selTable();




        s_start.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, AutoService.class);
                //startService(intent);
                bindService(intent, connection, BIND_AUTO_CREATE);
                running = true;
                new Thread(new getAutoThread()).start();
                text3.setText("Service Running");

            }

        });


        s_stop.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {

                //Intent intent = new Intent(MainActivity.this, AutoService.class);
                //stopService(intent);
                try{

                    unbindService(connection);
                    running = false;
                    text3.setText("Service Stopped");

                } catch(Exception e) {

                    //do nothing

                }

            }

        });


        update_btn.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {

                /*ContentValues new_values = new ContentValues();
                new_values.put("age", 200);

                db.update("student", new_values, "name=?", new String[]{"홍길동"});*/

                Intent intent = new Intent(MainActivity.this, CoinRegister.class);
                startActivity(intent);

            }

        });

    }

    public Instances getDataSet(String fileName) throws IOException {
        /**
         * we can set the file i.e., loader.setFile("finename") to load the data
         */
        int classIdx = 1;
        /** the arffloader to load the arff file */
        ArffLoader loader = new ArffLoader();
        /** load the traing data */
        AssetManager assetManager = getResources().getAssets();
        InputStream inputStream= assetManager.open(fileName);
        loader.setSource(inputStream);
        /**
         * we can also set the file like loader3.setFile(new
         * File("test-confused.arff"));
         */
        //loader.setFile(new File(fileName));
        Instances dataSet = loader.getDataSet();
        /** set the index based on the data given in the arff files */
        dataSet.setClassIndex(classIdx);
        return dataSet;
    }

    private void selTable() {

        Cursor c = db.query("trans", null, null, null, null, null,  "_id" + " DESC", null);

        SimpleCursorAdapter adapter = null;
        adapter = new SimpleCursorAdapter(MainActivity.this,
                R.layout.lv_layout, c,
                new String[] {"market", "side", "volume", "price", "vp", "fee", "vpf"},
                new int[] {R.id.text4, R.id.text5, R.id.text6, R.id.text7, R.id.text8, R.id.text9, R.id.texta}, 0);

        ListView list = (ListView)findViewById(R.id.list);
        list.setAdapter(adapter);

    }

    private class getAutoThread implements Runnable {

        private Handler handler = new Handler();

        @Override
        public void run() {
            while(running) {

                if(binder == null) continue;

                handler.post(new Runnable() {

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void run() {
                        try{
                            if(binder.getSendable()) {
                                text2.setText(binder.getSomething() + "");
                                binder.setSendable();
                            }
                            if(binder.getExeg()) {

                                ArrayList<Double> asdfasdf = (ArrayList<Double>) AutoService.arrArray.clone();
                                AutoService.exeg = false;

                                ArrayList<String> strList = helper.getTableData(db, "coin");

                                getAccount();

                                for (int i = 0; i < asdfasdf.size(); ++i) {

                                    if(asdfasdf.get(i) >= 0.5) {
                                        trans.add(makeOrder("KRW-" + strList.get(i), "bid", "5000.0", "1.0"));
                                    } else {
                                        for(int j = 0; j < sCurrency.size(); ++j) {
                                            if(sCurrency.get(j).equals(strList.get(i))) {
                                                trans.add(makeOrder("KRW-" + strList.get(i), "ask", "1.0", sBalance.get(j)));
                                            }
                                        }
                                    }

                                }

                            }

                            transactionWrapper tw = new transactionWrapper();

                            if(trans.size() != 0) tw = getOrderHistory("KRW-" + trans.get(0));

                            if(!tw.market.equals("")) {

                                helper.setTableData(db, tw);

                                Cursor c = db.query("trans", null, null, null, null, null,  "_id" + " DESC", null);

                                SimpleCursorAdapter adapter = null;
                                adapter = new SimpleCursorAdapter(MainActivity.this,
                                        R.layout.lv_layout, c,
                                        new String[] {"market", "side", "volume", "price", "vp", "fee", "vpf"},
                                        new int[] {R.id.text4, R.id.text5, R.id.text6, R.id.text7, R.id.text8, R.id.text9, R.id.texta}, 0);

                                ListView list = (ListView)findViewById(R.id.list);
                                list.setAdapter(adapter);

                                trans.remove(0);

                            }
                        } catch (Exception e) {

                            e.printStackTrace();

                        }


                    }

                });

                try {
                    Thread.sleep(500);
                } catch(Exception e) {

                    e.printStackTrace();

                }

            }
        }

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private String makeOrder(String market, String side, String price, String volume){

        String sUUID = "";

        try{

        //UUIDD return용 선언부//UUID 반환

        HashMap<String, String> params = new HashMap<>();
        params.put("market", market);
        params.put("side", side);
        params.put("volume", volume);
        params.put("price", price);

        String ot;

        if(side.equals("ask")) {
            ot = "market";
        } else {
            ot = "price";
        }

        params.put("ord_type", ot);

        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, String> entity : params.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }

        String queryString = String.join("&", queryElements.toArray(new String[0]));

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes("UTF-8"));

        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));
        System.out.println(queryHash);

        String jwtToken = Jwts.builder()
                .claim("access_key", accessKey)
                .claim("nonce", UUID.randomUUID().toString())
                .claim("query_hash", queryHash)
                .claim("query_hash_alg", "SHA512")
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();


        String authenticationToken = "Bearer " + jwtToken;
            HttpClient client = HttpClientBuilder.create().build();
            //HttpPost request = new HttpPost(serverUrl + "/v1/orders");
            //request.setHeader("Content-Type","application/json");
            //request.addHeader("Authorization",authenticationToken);
            //request.setEntity(new StringEntity(new Gson().toJson(params)));

            AsyncTask<String, Void, HttpResponse> asyncTask = new AsyncTask<String, Void, HttpResponse>() {
                @Override
                protected HttpResponse doInBackground(String... url) {
                    HttpPost request = new HttpPost(serverUrl + "/v1/orders");
                    request.setHeader("Content-Type","application/json");
                    request.addHeader("Authorization",authenticationToken);
                    try {
                        request.setEntity(new StringEntity(new Gson().toJson(params)));
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    HttpResponse response = null;
                    try {
                        response = client.execute(request);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                }
            };

            HttpResponse response = asyncTask.execute(serverUrl+"/v1/orders").get();

            AsyncTask<String, Void, String> asyncTask2 = new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... url) {
                    HttpEntity entity = null;
                    String data = "";
                    try {
                        entity = response.getEntity();
                        data = EntityUtils.toString(entity, "UTF-8");
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    return data;
                }
            };

            //HttpResponse response = client.execute(request);
            //HttpEntity entity = response.getEntity();
            //HttpEntity entity = asyncTask2.execute("").get();

            //1. 데이터 담기
            String data = asyncTask2.execute("").get();

            String sMarket ="";//시장이름
            String sSide ="";//매수 or 매도
            String sCreated_at ="";//주문 날짜
            String sPrice ="";//거래단가
            String sVolume ="";//거래량
            String sOrderType ="";//거래 방식

            MyAccountStr = "<주문 내역>\n";

            System.out.println(data);

            //3.배열에 있는 오브젝트를 오브젝트에 담기
            JSONObject jsonObject = new JSONObject(data);

            //4.오브젝트에 있는 데이터를 key값으로 불러오기
            sMarket = jsonObject.get("market").toString();
            sSide = jsonObject.get("side").toString();
            sOrderType = jsonObject.get("ord_type").toString();
            sPrice = jsonObject.get("price").toString();
            sVolume = jsonObject.get("volume").toString();
            sCreated_at = jsonObject.get("created_at").toString();
            sUUID = jsonObject.get("uuid").toString();

            Log.d(TAG, "마켓:"+sMarket);
            Log.d(TAG, "매수/매도:"+sSide);
            Log.d(TAG, "거래단가:"+sPrice);
            Log.d(TAG, "거래량:"+sVolume);

            MyAccountStr += "마켓:"+sMarket+"\r\n";
            MyAccountStr += "(매수/매도):";
            if(sSide.equals("bid")){
                MyAccountStr += "매수"+"\r\n";
            }else if(sSide.equals("ask")){
                MyAccountStr += "매도"+"\r\n";
            }
            MyAccountStr += "주문방식:"+sOrderType+"\r\n";
            MyAccountStr += "거래단가:"+sPrice+"\r\n";
            MyAccountStr += "거래량:"+sVolume+"\r\n";
            MyAccountStr += "거래 일자:"+sCreated_at+"\r\n";
            MyAccountStr += "UUID:"+sUUID+"\r\n";
            MyAccountStr += "\n";

        }catch (Exception e){
            e.printStackTrace();
        }

        return sUUID;
    }

    private void getAccount() {

        try{

        String jwtToken = Jwts.builder()
                .claim("access_key", accessKey)
                .claim("nonce", UUID.randomUUID().toString())
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();

        System.out.println(jwtToken);

        String authenticationToken = "Bearer " + jwtToken;
            HttpClient client = HttpClientBuilder.create().build();
            //HttpGet request = new HttpGet(serverUrl+"/v1/accounts");
            //request.setHeader("Content-Type","application/json");
            //request.addHeader("Authorization",authenticationToken);

            AsyncTask<String, Void, HttpResponse> asyncTask = new AsyncTask<String, Void, HttpResponse>() {
                @Override
                protected HttpResponse doInBackground(String... url) {
                    HttpGet request = new HttpGet(url[0]);
                    request.setHeader("Content-Type","application/json");
                    request.addHeader("Authorization",authenticationToken);
                    HttpResponse response = null;
                    try {
                        response = client.execute(request);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                }
            };

            HttpResponse response = asyncTask.execute(serverUrl+"/v1/accounts").get();
            //HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            //1. 데이터 담기
            String data = EntityUtils.toString(entity, "UTF-8");

            if(sCurrency != null) sCurrency.clear();//화폐종류
            sBalance.clear();//주문가능 금액/수량
            sLocked.clear();//주문 중 묶여있는 금액
            sAvg_buy_price.clear();//매수 평균가

            //2. 데이터를 배열에 담기
            JSONArray jsonArray = new JSONArray(data);

            for(int i=0;i<jsonArray.length();i++){

                //3.배열에 있는 오브젝트를 오브젝트에 담기
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                //4.오브젝트에 있는 데이터를 key값으로 불러오기
                sCurrency.add(jsonObject.get("currency").toString());
                sBalance.add(jsonObject.get("balance").toString());
                sLocked.add(jsonObject.get("locked").toString());
                sAvg_buy_price.add(jsonObject.get("avg_buy_price").toString());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private transactionWrapper getOrderHistory(String str_uuid){

        transactionWrapper tw = new transactionWrapper();

        try{

        HashMap<String, String> params = new HashMap<>();
        params.put("uuid", str_uuid);

        ArrayList<String> queryElements = new ArrayList<>();
        for(Map.Entry<String, String> entity : params.entrySet()) {
            queryElements.add(entity.getKey() + "=" + entity.getValue());
        }

        String queryString = String.join("&", queryElements.toArray(new String[0]));

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes("UTF-8"));

        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));
        System.out.println(queryHash);

        String jwtToken = Jwts.builder()
                .claim("access_key", accessKey)
                .claim("nonce", UUID.randomUUID().toString())
                .claim("query_hash", queryHash)
                .claim("query_hash_alg", "SHA512")
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();


        String authenticationToken = "Bearer " + jwtToken;
            HttpClient client = HttpClientBuilder.create().build();
            //HttpGet request = new HttpGet(serverUrl+"/v1/order?"+queryString);
            //request.setHeader("Content-Type","application/json");
            //request.addHeader("Authorization",authenticationToken);

            AsyncTask<String, Void, HttpResponse> asyncTask = new AsyncTask<String, Void, HttpResponse>() {
                @Override
                protected HttpResponse doInBackground(String... url) {
                    HttpGet request = new HttpGet(url[0]);
                    request.setHeader("Content-Type","application/json");
                    request.addHeader("Authorization",authenticationToken);
                    HttpResponse response = null;
                    try {
                        response = client.execute(request);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                }
            };

            HttpResponse response = asyncTask.execute(serverUrl+"/v1/order?"+queryString).get();

            AsyncTask<String, Void, String> asyncTask2 = new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... url) {
                    HttpEntity entity = null;
                    String data = "";
                    try {
                        entity = response.getEntity();
                        data = EntityUtils.toString(entity, "UTF-8");
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    return data;
                }
            };

            //HttpResponse response = client.execute(request);
            //HttpEntity entity = response.getEntity();
            //HttpEntity entity = asyncTask2.execute("").get();

            //1. 데이터 담기
            String data = asyncTask2.execute("").get();

            String sMarket ="";//시장이름
            String sSide ="";//매수 or 매도
            String sCreated_at ="";//주문 날짜
            String sPrice ="";//거래단가
            String sVolume ="";//거래량
            String sAmount ="";//거래금액
            String sPaid_fee ="";//수수료
            String sOrderType ="";//거래 방식
            String amount_minus_p_f ="";

            //3.배열에 있는 오브젝트를 오브젝트에 담기
            JSONObject jsonObject = new JSONObject(data);

            //4.오브젝트에 있는 데이터를 key값으로 불러오기
            sMarket = jsonObject.get("market").toString();
            sSide = jsonObject.get("side").toString();
            sOrderType = jsonObject.get("ord_type").toString();
            sPrice = jsonObject.get("price").toString();
            sVolume = jsonObject.get("volume").toString();
            sAmount = Double.toString(Double.valueOf(sPrice)*Double.valueOf(sVolume));
            sPaid_fee = jsonObject.get("paid_fee").toString();
            sCreated_at = jsonObject.get("created_at").toString();
            double amount = Double.valueOf(sAmount);
            double paid_fee = Double.valueOf(sPaid_fee);
            amount_minus_p_f = Double.toString(amount - paid_fee);

            if(jsonObject.get("state").toString().equals("done")) {

                tw.market = sMarket;
                tw.side = sSide;
                tw.volume = sVolume;
                tw.price = sPrice;
                tw.vp = sAmount;
                tw.fee = sPaid_fee;
                tw.vpf = amount_minus_p_f;

            }



        }catch (Exception e){
            e.printStackTrace();
        }

        return tw;
    }
}