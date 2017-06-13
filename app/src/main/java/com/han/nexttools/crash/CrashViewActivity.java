package com.han.nexttools.crash;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.han.nexttools.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import cn.jpush.android.api.JPushInterface;

public class CrashViewActivity extends AppCompatActivity {
    public static void start(Context context, File file) {
        Intent intent = new Intent(context, CrashViewActivity.class);
        intent.putExtra("path", file.getPath());
        intent.putExtra("name", file.getName());
        context.startActivity(intent);
    }

    private TextView mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_view);
        Log.d("Han", JPushInterface.getRegistrationID(this));
        String filePath = getIntent().getStringExtra("path");
        String fileName = getIntent().getStringExtra("name");
        getSupportActionBar().setTitle(fileName);
        try {
            FileInputStream inputStream = new FileInputStream(filePath);
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            while (inputStream.read(bytes) != -1) {
                arrayOutputStream.write(bytes, 0, bytes.length);
            }
            inputStream.close();
            arrayOutputStream.close();
            String content = new String(arrayOutputStream.toByteArray());
            mTv = ((TextView) findViewById(R.id.crash_tv));
            mTv.setMovementMethod(ScrollingMovementMethod.getInstance());
            mTv.setText(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
