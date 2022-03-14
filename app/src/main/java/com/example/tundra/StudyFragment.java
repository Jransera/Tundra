package com.example.tundra;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link StudyFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class StudyFragment extends Fragment {
    userData u_data;

    //text views
    TextView rank;
    TextView total;
    TextView latest;
    TextView avg;
    TextView succ;
    TextView num;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StudyFragment() {
        // Required empty public constructor
    }

//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment StudyFragment.
//     */
    // TODO: Rename and change types and number of parameters
//    public static StudyFragment newInstance(String param1, String param2) {
//        StudyFragment fragment = new StudyFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            u_data = (userData) getArguments().get("user_info");
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }


     }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getArguments() != null) {
            u_data = (userData) getArguments().get("user_info");
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        // Inflate the layout for this fragment
        FrameLayout fl = (FrameLayout) inflater.inflate(R.layout.fragment_study, container, false);

        fl.setVisibility(View.VISIBLE);

        rank = (TextView) fl.findViewById(R.id.rank_tv);
        total = (TextView)fl.findViewById(R.id.total_time_tv);
        avg = (TextView)fl.findViewById(R.id.average_tv);
        latest = (TextView)fl.findViewById(R.id.latest_tv);
        succ = (TextView)fl.findViewById(R.id.succ_rate_tv);
        num = (TextView)fl.findViewById(R.id.num_sessions_tv);


        //set values
        Log.d("MyActivty","starting");

        if(rank == null)
        {
            Log.d("MyActivty","null");
        }

        rank.setText(Integer.toString(u_data.getRank()+1));

        Log.d("MyActivty","rank");


        total.setText(Long.toString(u_data.getTotalTime()));

        Log.d("MyActivty","total");

        avg.setText(Long.toString(u_data.getAvg()));
        Log.d("MyActivty","avg");

        latest.setText(Long.toString(u_data.getLatest()));

        Log.d("MyActivty","latest");

        succ.setText(Float.toString(u_data.getSuccRate()));

        Log.d("MyActivty","succ");

        num.setText(Integer.toString(u_data.getNumSessions()));

        Log.d("MyActivty","num");



        return fl;
    }
}