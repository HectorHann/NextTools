package com.han.nexttools.log;

import java.io.File;
import java.util.List;

/**
 * Created by Han on 2017/8/14.
 */

public class LogPresenter implements LogContract.Presenter {

    private LogContract.View mView;

    public LogPresenter(LogContract.View view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void readLastNLine(File file) {
        List<String> contentList = ReadFile.readLastNLine(file, 60);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < contentList.size(); i++) {
            String str = contentList.get(i);
            if (str.contains("NextHttp Request") || str.contains("NextHttp Response")) {
                sb.append("\n");
                sb.append("\n");
            }

            sb.append(str);
            sb.append("\n");
        }

        mView.showlog(formatLog2Json(sb.toString()));
    }

    @Override
    public String formatLog2Json(String log) {
        return JsonFormatTool.formatJson(log);
    }
}
