package com.han.nexttools.log;

import android.text.SpannableString;

import com.han.nexttools.BasePresenter;
import com.han.nexttools.BaseView;

import java.io.File;

/**
 * Created by Han on 2017/8/14.
 */

public interface LogContract {

    interface Presenter extends BasePresenter {
        void readLastNLine(File file);

        String formatLog2Json(String log);

        void searchContent(String content, String search);

    }

    interface View extends BaseView<Presenter> {
        void showLog(SpannableString log);

        void showLog(String log);

        void showToast(String toast);

    }
}
