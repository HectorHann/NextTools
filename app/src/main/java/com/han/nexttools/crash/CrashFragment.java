package com.han.nexttools.crash;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.han.nexttools.R;
import com.han.nexttools.RecyclerViewDivider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class CrashFragment extends Fragment implements CrashContract.View {

    public static final String CRASH_DIR = "/sdcard/NextTrucking/crash";
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private ArrayList<File> mCrashFileList = new ArrayList<>();
    private CrashRecyclerViewAdapter mAdapter;

    private CrashPresenter mPresent;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CrashFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static CrashFragment newInstance(int columnCount) {
        CrashFragment fragment = new CrashFragment();
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
    }


    @Override
    public void onResume() {
        super.onResume();
        mPresent.loadCrashFile(CRASH_DIR);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crash_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdapter = new CrashRecyclerViewAdapter(mCrashFileList, mListener);
            recyclerView.setAdapter(mAdapter);
            recyclerView.addItemDecoration(new RecyclerViewDivider(context, LinearLayoutManager.HORIZONTAL));
        }
        return view;
    }


    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        mListener = new OnListFragmentInteractionListener() {
            @Override
            public void onListFragmentInteraction(File item) {
                Log.d("Han", item.toString());
                startCrashViewActivity(item);
            }
        };
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void setPresenter(CrashContract.Presenter presenter) {
        mPresent = (CrashPresenter) presenter;
    }


    @Override
    public void showFileList(List<File> list) {
        mCrashFileList.clear();
        mCrashFileList.addAll(list);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void startCrashViewActivity(File file) {
        CrashViewActivity.start(getContext(), file);
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
        void onListFragmentInteraction(File item);
    }
}
