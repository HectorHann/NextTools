package com.han.nexttools.log;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.han.nexttools.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogFragment extends Fragment implements LogContract.View {
    // TODO: Rename parameter arguments, choose names that match
    private static final String LOG_DIR = "/sdcard/NextTrucking/log";

    private File mLogFile;

    private TextView mTV;
    private EditText mET;

    private LogContract.Presenter mPresenter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File dir = new File(LOG_DIR);
        File[] files = dir.listFiles();
        if (files != null && files.length > 0)
            mLogFile = files[0];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        mTV = (TextView) view.findViewById(R.id.log_tv);
        mET = (EditText) view.findViewById(R.id.search);

        mET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH && !TextUtils.isEmpty(v.getText())) {
                    String searchStr = v.getText().toString();
                    mPresenter.searchContent(mTV.getText().toString(), searchStr);
                }
                return false;
            }
        });

        view.findViewById(R.id.clear_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FileWriter fileWriter = new FileWriter(mLogFile, false);
                    fileWriter.write("");
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mPresenter.readLastNLine(mLogFile);
                }

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.readLastNLine(mLogFile);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void setPresenter(LogContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showLog(SpannableString log) {
        mTV.setText(log);
    }

    @Override
    public void showLog(String log) {
        mTV.setText(log);
    }

    @Override
    public void showToast(String toast) {
        Toast.makeText(getContext(), toast, Toast.LENGTH_LONG).show();
    }
}
