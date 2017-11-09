package com.han.nexttools.apk;

import android.content.Context;

import com.han.nexttools.BasePresenter;
import com.han.nexttools.BaseView;

import org.apache.commons.net.ftp.FTPFile;

import java.util.List;

/**
 * Created by Han on 2017/8/10.
 */

public interface APKContract {

    interface Presenter extends BasePresenter {
        void getAPKList();

        void downloadAPKFile(Context context, FTPFile file, String fileDir, String fileName);

        void downloadReadMeFile(String fileDir, String fileName);

        void getReadMeContent(String path);

        void createDir(String dir);
    }


    interface View extends BaseView<Presenter> {
        void showProgress();

        void dismissProgress();

        void setProgress(int progress);

        void showAPKList(List<FTPFile> apkList);

        void showReadMe(String content);

        void installAPK(String path);
    }
}
