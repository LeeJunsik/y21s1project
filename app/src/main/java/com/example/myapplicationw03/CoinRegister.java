package com.example.myapplicationw03;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class CoinRegister extends AppCompatActivity {

    Button register;
    Button delete;
    EditText ed;
    DBHelper helper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_register);
        register = (Button)findViewById(R.id.cregister);
        delete = (Button)findViewById(R.id.cdelete);
        ed = (EditText)findViewById(R.id.ed);



        helper = new DBHelper(CoinRegister.this, "coin.db", null, 1);
        db = helper.getWritableDatabase();
        helper.onCreateString(db,"create table if not exists coin(_id integer primary key autoincrement, name text);");

        selTable();

        register.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {

                ContentValues values = new ContentValues();

                values.put("name", ed.getText().toString());

                db.insert("coin", null, values);
                selTable();

            }

        });

        delete.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {

                db.delete("coin", "name=?", new String[]{ed.getText().toString()});
                selTable();

            }

        });
    }

    private void selTable() {

        Cursor c = db.query("coin", null, null, null, null, null,  "name" + " ASC", null);

        SimpleCursorAdapter adapter = null;
        adapter = new SimpleCursorAdapter(CoinRegister.this,
                android.R.layout.simple_list_item_1, c,
                new String[] {"name"},
                new int[] {android.R.id.text1}, 0);

        ListView list = (ListView)findViewById(R.id.coinList);
        list.setAdapter(adapter);

    }
}