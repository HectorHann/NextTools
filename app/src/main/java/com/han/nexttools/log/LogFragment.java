package com.han.nexttools.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.han.nexttools.MainActivity;
import com.han.nexttools.R;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LogFragment extends Fragment implements LogContract.View {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String LOG_DIR = "/sdcard/NextTrucking/log";
    private static final int READ_FILE = 0;
    private static final int SEARCH_FILE = 1;
    private static final int JSON_PERCENT = 2;
    // TODO: Rename and change types of parameters

    private OnFragmentInteractionListener mListener;

    private File mLogFile;

    private TextView mTV;
    private EditText mET;

    private ArrayList<String> contentList = new ArrayList<>();
    private String contentJson;

    private LogContract.Presenter mPresenter;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case READ_FILE:
                    ((MainActivity) getActivity()).dismissProgress();
                    mTV.setText(contentJson);
                    break;
                case JSON_PERCENT:
                    ((MainActivity) getActivity()).updateProgress(msg.arg1);
                    break;
            }
        }
    };

    @Override
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
                    SpannableString s = new SpannableString(contentJson);

                    Pattern p = Pattern.compile(searchStr, Pattern.CASE_INSENSITIVE);

                    Matcher m = p.matcher(s);

                    if (!m.find()) {
                        Toast.makeText(getActivity(), "Not Find!", Toast.LENGTH_LONG).show();
                        return false;
                    }

                    m.reset();

                    while (m.find()) {
                        int start = m.start();
                        int end = m.end();
                        s.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    mTV.setText(s);
                }
                return false;
            }
        });


        return view;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        mListener = null;
    }

    @Override
    public void setPresenter(LogContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showlog(String log) {
        mTV.setText(log);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
