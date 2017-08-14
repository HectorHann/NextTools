package com.han.nexttools.apk;


import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.han.nexttools.ftp.FTP;

import org.apache.commons.net.ftp.FTPFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Han on 2017/8/10.
 */

public class APKPresenter implements APKContract.Presenter {

    private static int MSG_WHAT_READEME_FILE_PATH = 0x1;
    private static int MSG_WHAT_APK_FILE_PATH = 0x2;
    private static int MSG_WHAT_APK_LIST = 0x3;
    private static int MSG_WHAT_PROGRESS = 0x4;
    private APKContract.View mView;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("APK", "Message what = " + String.valueOf(msg.what) + "  arg1 = " + msg.arg1);
            if (msg.what == MSG_WHAT_READEME_FILE_PATH) {
                mView.dismissProgress();
                getReadMeContent((String) msg.obj);
            } else if (msg.what == MSG_WHAT_APK_FILE_PATH) {
                mView.dismissProgress();
                mView.installAPK((String) msg.obj);
                mView.dismissProgress();
            } else if (msg.what == MSG_WHAT_APK_LIST) {
                mView.dismissProgress();
                mView.showAPKList((List<FTPFile>) msg.obj);
            } else if (msg.what == MSG_WHAT_PROGRESS) {
                mView.setProgress(msg.arg1);
            }
        }
    };

    public APKPresenter(@NonNull APKContract.View view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void getAPKList() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Log.d("APK", "start get apk list");
                List<FTPFile> fileList = new FTP().getAPKFileList();
                Message message = handler.obtainMessage(MSG_WHAT_APK_LIST);
                message.obj = fileList;
                handler.sendMessage(message);
            }
        }.start();

    }

    @Override
    public void downloadAPKFile(final FTPFile file, final String fileDir, final String fileName) {
        mView.showProgress();
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    new FTP().downloadSingleFile(file.getName(), fileDir, fileName, new FTP.DownLoadProgressListener() {
                        @Override
                        public void onDownLoadProgress(String currentStep, int downProcess, File file) {
                            if (currentStep.equals(FTP.FTP_DOWN_SUCCESS)) {
                                Message message = handler.obtainMessage(MSG_WHAT_APK_FILE_PATH);
                                message.obj = fileDir + fileName;
                                handler.sendMessage(message);
                            } else if (currentStep.equals(FTP.FTP_DOWN_LOADING)) {
                                Message message = handler.obtainMessage(MSG_WHAT_PROGRESS);
                                message.arg1 = downProcess;
                                handler.sendMessage(message);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();

    }

    @Override
    public void downloadReadMeFile(final String fileDir, final String fileName) {
        Log.d("APK", "get readme.txt");
        mView.showProgress();
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    new FTP().downloadSingleFile("readme.txt", fileDir, fileName, new FTP.DownLoadProgressListener() {
                        @Override
                        public void onDownLoadProgress(String currentStep, int downProcess, File file) {
                            if (currentStep.equals(FTP.FTP_DOWN_SUCCESS)) {
                                Message message = handler.obtainMessage(MSG_WHAT_READEME_FILE_PATH);
                                message.obj = fileDir + fileName;
                                handler.sendMessage(message);
                            } else if (currentStep.equals(FTP.FTP_DOWN_LOADING)) {
                                Message message = handler.obtainMessage(MSG_WHAT_PROGRESS);
                                message.arg1 = downProcess;
                                handler.sendMessage(message);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void getReadMeContent(String path) {
        String content = null;
        try {
            FileInputStream inputStream = new FileInputStream(new File(path));
            byte[] bytes = new byte[inputStream.available()];
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

            while (inputStream.read(bytes) != -1) {
                arrayOutputStream.write(bytes, 0, bytes.length);
            }

            inputStream.close();
            arrayOutputStream.close();
            content = new String(arrayOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mView.showReadMe(content);
    }


    @Override
    public void createDir(String dir) {
        new File(dir).mkdirs();
    }

}
