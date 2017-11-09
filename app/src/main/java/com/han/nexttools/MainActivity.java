package com.han.nexttools;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.han.nexttools.apk.APKContract;
import com.han.nexttools.apk.APKFragment;
import com.han.nexttools.apk.APKPresenter;
import com.han.nexttools.crash.CrashContract;
import com.han.nexttools.crash.CrashFragment;
import com.han.nexttools.crash.CrashPresenter;
import com.han.nexttools.log.LogContract;
import com.han.nexttools.log.LogFragment;
import com.han.nexttools.log.LogPresenter;
import com.han.nexttools.push.PushReceiver;

import org.apache.commons.net.ftp.FTPFile;
import org.json.JSONObject;

import java.util.List;

import cn.jpush.android.api.JPushInterface;

public class MainActivity extends FragmentActivity {

    private FrameLayout mFrameLayout;
    private FragmentManager mFragmentManager;
    private Dialog dialog;
    private ProgressBar progressBar;
    private BottomNavigationView navigation;
    private String mPushContent;

    private APKPresenter mAPKPresenter;
    private LogPresenter mLogPresenter;
    private CrashPresenter mCrashPresenter;

    private boolean mForceUpdate;
    private String mUpdateAPK = "";
    private boolean mInstall;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mInstall) {
                mInstall = false;
                @SuppressLint("RestrictedApi") List<FTPFile> ftpFileList = ((APKFragment) mFragmentManager.getFragments().get(0)).getAPKFileList();
                for (FTPFile item : ftpFileList) {
                    if (item.getName().toLowerCase().contains(mUpdateAPK.toLowerCase())) {
                        Log.d("APK", "Download APK " + item.getName());
                        mAPKPresenter.downloadAPKFile(MainActivity.this, item, "/sdcard/", item.getName());
                    }
                }
            }
        }
    };
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_apk:
                    fragment = new APKFragment();
                    mAPKPresenter = new APKPresenter((APKContract.View) fragment);
                    break;
                case R.id.navigation_log:
                    fragment = new LogFragment();
                    mLogPresenter = new LogPresenter((LogContract.View) fragment);
                    break;
                case R.id.navigation_crash:
                    fragment = new CrashFragment();
                    mCrashPresenter = new CrashPresenter((CrashContract.View) fragment);
                    break;

            }
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.replace(R.id.content, fragment);
            transaction.commit();
            return true;
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgress();
    }

    private void showPushDialog() {

        if (mInstall) {
            return;
        }
        new AlertDialog.Builder(MainActivity.this).setTitle("更新提示")//设置对话框标题
                .setMessage(mPushContent)//设置显示的内容
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {//添加确定按钮
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mForceUpdate) {
                            List<FTPFile> ftpFileList = ((APKFragment) mFragmentManager.getFragments().get(0)).getAPKFileList();
                            for (FTPFile item : ftpFileList) {
                                if (item.getName().toLowerCase().contains(mUpdateAPK.toLowerCase())) {
                                    Log.d("APK", "Download APK " + item.getName());
                                    mAPKPresenter.downloadAPKFile(MainActivity.this, item, "/sdcard/", item.getName());
                                }
                            }
                        }
                    }
                }).setCancelable(false).
                show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("JPush", JPushInterface.getRegistrationID(this));
        mFragmentManager = getSupportFragmentManager();
        mFrameLayout = (FrameLayout) findViewById(R.id.content);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.setSelectedItemId(R.id.navigation_apk);

        mPushContent = getIntent().getStringExtra(JPushInterface.EXTRA_ALERT);

        try {
            JSONObject json = new JSONObject(getIntent().getExtras().getString(JPushInterface.EXTRA_EXTRA));
            mForceUpdate = json.getBoolean("force");
            mUpdateAPK = json.getString("apk");
            mInstall = json.getBoolean("install");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("JPush", "ForceUPdate = " + mForceUpdate + "|APK = " + mUpdateAPK);
        if (!TextUtils.isEmpty(mPushContent)) {
            Log.d("JPush", mPushContent);
            showPushDialog();
        }

        mHandler.sendEmptyMessageDelayed(1, 2000);
    }


    public void showProgress() {
        try {
            if (dialog == null) {
                LayoutInflater inflater = LayoutInflater.from(this);
                View v = inflater.inflate(R.layout.progress, null);// 得到加载view
                progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
                progressBar.setProgress(0);
                dialog = new Dialog(this, R.style.loading_dialog);
                dialog.setContentView(v, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                dialog.setCancelable(false);
            }
            if (!dialog.isShowing()) {
                dialog.show();
            }

        } catch (Exception e) {

        }

    }

    public void dismissProgress() {
        try {
            if (dialog != null) {
                dialog.dismiss();
                progressBar.setProgress(0);
            }
        } catch (Exception e) {

        }
    }

    public void updateProgress(int progress) {
        progressBar.setProgress(progress);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PushReceiver.clearlAllNotification(this);
    }
}
