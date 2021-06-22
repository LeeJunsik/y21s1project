package com.example.myapplicationw03;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {

        super(context, name, factory, version);

    }

    @Override

    public void onCreate(SQLiteDatabase db) {

        String sql = "create table if not exists student("
                + "_id integer primary key autoincrement, "
                + "name text, "
                + "age integer, "
                + "address text);";

        db.execSQL(sql);

    }

    @Override

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        String sql = "drop table if exists trans";
        db.execSQL(sql);

        onCreate(db);

    }

    public void onCreateString(SQLiteDatabase db, String sql) {

        db.execSQL(sql);

    }

    public ArrayList<String> getTableData(SQLiteDatabase db, String table) {

        String sql ="SELECT * FROM " + table;

        ArrayList<String> strList = new ArrayList<String>();

        String str = null;

        Cursor mCur = db.rawQuery(sql, null);
        if(mCur != null) {

            while(mCur.moveToNext()) {

                str = mCur.getString(1);
                strList.add(str);

            }

        }

        return strList;
    }

    public void setTableData(SQLiteDatabase db, transactionWrapper tw) {

        ContentValues values = new ContentValues();

        values.put("market", tw.market);
        values.put("side", tw.side);
        values.put("volume", tw.volume);
        values.put("price", tw.price);
        values.put("vp", tw.vp);
        values.put("fee", tw.fee);
        values.put("text", tw.vpf);

        db.insert("trans", null, values);
    }

}
