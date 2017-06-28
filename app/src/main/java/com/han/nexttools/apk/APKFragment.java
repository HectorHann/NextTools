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
import com.han.nexttools.ftp.FTP;

import org.apache.commons.net.ftp.FTPFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class APKFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final int APK_LIST = 0;
    private static final int APK_VERSION = 1;
    private static final int APK_INSTALL = 2;
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private static final String DOWNLOAD_DIR = "/sdcard/NextAPK/";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    private TextView mVersionTV;
    private String mVersionContent;

    private File mCurDownAPK;

    public APKFragment() {
    }

    private List<FTPFile> mAPKFileList = new ArrayList<>();
    private APKRecyclerViewAdapter mAdapter;

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

        new File(DOWNLOAD_DIR).mkdirs();

        Log.d("JPush", JPushInterface.getRegistrationID(getContext()));

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apk_list, container, false);

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
                ((MainActivity) getActivity()).showProgress();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            new FTP().downloadSingleFile(item.getName(), "/sdcard/", item.getName(), new FTP.DownLoadProgressListener() {
                                @Override
                                public void onDownLoadProgress(String currentStep, long downProcess, File file) {
                                    ((MainActivity) getActivity()).updateProgress((int) downProcess);
                                    if (currentStep.equals(FTP.FTP_DOWN_SUCCESS)) {
                                        ((MainActivity) getActivity()).dismissProgress();
                                        mCurDownAPK = file;
                                        mHandler.sendEmptyMessage(APK_INSTALL);

                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();

        getReadMeTxt();
        getApkList();
    }

    private void getApkList() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    mAPKFileList.clear();
                    mAPKFileList.addAll(new FTP().getAPKFileList());
                    mHandler.sendEmptyMessage(APK_LIST);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void getReadMeTxt() {
        ((MainActivity) getActivity()).showProgress();
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {

                    new FTP().downloadSingleFile("readme.txt", "/sdcard/", "readme.txt", new FTP.DownLoadProgressListener() {
                        @Override
                        public void onDownLoadProgress(String currentStep, long downProcess, File file) {
                            Log.d("Han", currentStep + "|-" + downProcess);
                            ((MainActivity) getActivity()).updateProgress((int) downProcess);
                            if (currentStep.equals(FTP.FTP_DOWN_SUCCESS)) {
                                ((MainActivity) getActivity()).dismissProgress();
                                try {
                                    FileInputStream inputStream = new FileInputStream(file);
                                    byte[] bytes = new byte[inputStream.available()];
                                    ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

                                    while (inputStream.read(bytes) != -1) {
                                        arrayOutputStream.write(bytes, 0, bytes.length);
                                    }

                                    inputStream.close();
                                    arrayOutputStream.close();
                                    mVersionContent = new String(arrayOutputStream.toByteArray());
                                    Log.d("Han", mVersionContent);

                                    mHandler.sendEmptyMessage(APK_VERSION);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
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
    public void onDetach() {
        super.onDetach();
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
