package com.han.nexttools.crash;

import com.han.nexttools.BasePresenter;
import com.han.nexttools.BaseView;

import java.io.File;
import java.util.List;

/**
 * Created by Han on 2017/8/14.
 */

public interface CrashContract {
    interface Presenter extends BasePresenter {
        void loadCrashFile(String path);
    }

    interface View extends BaseView<Presenter> {
        void showFileList(List<File> list);

        void startCrashViewActivity(File file);
    }
}
