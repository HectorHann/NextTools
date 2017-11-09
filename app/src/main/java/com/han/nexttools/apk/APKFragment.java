package com.han.nexttools.apk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.TabStopSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.han.nexttools.MainActivity;
import com.han.nexttools.R;
import com.han.nexttools.RecyclerViewDivider;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class APKFragment extends Fragment implements APKContract.View {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final int APK_LIST = 0;
    private static final int APK_VERSION = 1;
    private static final int APK_INSTALL = 2;
    private static final String DOWNLOAD_DIR = "/sdcard/NextAPK/";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    private TextView mVersionTV;
    private String mVersionContent;

    private File mCurDownAPK;
    private List<FTPFile> mAPKFileList = new ArrayList<>();
    private APKRecyclerViewAdapter mAdapter;
    private APKContract.Presenter mPresenter;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case APK_LIST:
                    mAdapter.notifyDataSetChanged();
                    break;
                case APK_VERSION:
                    SpannableString s = new SpannableString(mVersionContent);
                    s.setSpan(new TabStopSpan() {
                        @Override
                        public int getTabStop() {
                            return 100;
                        }
                    }, 5, 30, 0);

                    mVersionTV.setText(s, TextView.BufferType.SPANNABLE);
                    break;
                case APK_INSTALL:
                    Log.d("Han", "Install " + mCurDownAPK.toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(Uri.fromFile(mCurDownAPK), "application/vnd.android.package-archive");
                    getActivity().startActivity(intent);
                    break;
            }
        }
    };

    public APKFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static APKFragment newInstance(int columnCount) {
        APKFragment fragment = new APKFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        mPresenter.createDir(DOWNLOAD_DIR);
        Log.d("JPush", JPushInterface.getRegistrationID(getContext()));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apk_list, container, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mVersionTV = (TextView) view.findViewById(R.id.version_content);
        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.apkList);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        mAdapter = new APKRecyclerViewAdapter(mAPKFileList, mListener);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new RecyclerViewDivider(context, LinearLayoutManager.HORIZONTAL));
        return view;
    }


    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        mListener = new OnListFragmentInteractionListener() {
            @Override
            public void onListFragmentInteraction(final FTPFile item) {
                Log.d("Han", item.toString());
                mPresenter.downloadAPKFile(getContext(), item, "/sdcard/", item.getName());
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("APK", "onResume");
        mPresenter.getAPKList();
        mPresenter.downloadReadMeFile("/sdcard/", "readme.txt");
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void setPresenter(APKContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showProgress() {
        ((MainActivity) getActivity()).showProgress();
    }

    @Override
    public void dismissProgress() {
        ((MainActivity) getActivity()).dismissProgress();
    }

    @Override
    public void setProgress(int progress) {
        ((MainActivity) getActivity()).updateProgress(progress);
    }

    @Override
    public void showAPKList(List<FTPFile> apkList) {
        mAPKFileList.clear();
        mAPKFileList.addAll(apkList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showReadMe(String content) {
        SpannableString s = new SpannableString(content);
        s.setSpan(new TabStopSpan() {
            @Override
            public int getTabStop() {
                return 100;
            }
        }, 5, 30, 0);
        mVersionTV.setText(s, TextView.BufferType.SPANNABLE);
    }

    @Override
    public void installAPK(String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
        getActivity().startActivity(intent);
    }

    public List<FTPFile> getAPKFileList() {
        return mAPKFileList == null ? new ArrayList<>() : mAPKFileList;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(FTPFile item);
    }
}
