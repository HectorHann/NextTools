package com.han.nexttools.crash;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Han on 2017/8/14.
 */

public class CrashPresenter implements CrashContract.Presenter {
    private CrashContract.View mView;

    public CrashPresenter(CrashContract.View view) {
        mView = view;
    }

    @Override
    public void loadCrashFile(String path) {
        ArrayList<File> list = new ArrayList<>();
        File dir = new File(path);
        File[] files = dir.listFiles();
        if (files != null) {
            list.addAll(Arrays.asList(files));
            Collections.sort(list);
            Collections.reverse(list);
        }

        mView.showFileList(list);
    }
}
