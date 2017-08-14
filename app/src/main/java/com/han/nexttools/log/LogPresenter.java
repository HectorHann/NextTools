package com.han.nexttools.log;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        mView.showLog(formatLog2Json(sb.toString()));
    }

    @Override
    public String formatLog2Json(String log) {
        return JsonFormatTool.formatJson(log);
    }

    @Override
    public void searchContent(String content, String search) {
        SpannableString s = new SpannableString(content);

        Pattern p = Pattern.compile(search, Pattern.CASE_INSENSITIVE);

        Matcher m = p.matcher(s);


        if (!m.find()) {
            mView.showToast("Not Find!");
            return;
        }

        m.reset();

        while (m.find()) {
            int start = m.start();
            int end = m.end();
            s.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mView.showLog(s);
    }
}
