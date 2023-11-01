package com.example.projectfoot.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.projectfoot.R;
import com.example.projectfoot.adapter.CourtsAdapter;
import com.example.projectfoot.config.FirebaseConfig;
import com.example.projectfoot.helper.RecyclerItemClickListener;
import com.example.projectfoot.helper.UserFirebase;
import com.example.projectfoot.model.Court;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView recyclerViewCourtList;
    private DatabaseReference courtsRef;
    private FirebaseUser currentUser;
    private CourtsAdapter adapter;
    private ValueEventListener valueEventListenerCourts;

    private ArrayList<Court> courtsList = new ArrayList<>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeedFragment newInstance(String param1, String param2) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        //Initial configs
        recyclerViewCourtList = view.findViewById(R.id.recyclerCourtsList);
        courtsRef = FirebaseConfig.getFirebaseDatabase().child("courts");
        currentUser = UserFirebase.getActualUser();

        //Adapter config
        adapter = new CourtsAdapter(courtsList, getActivity());

        //recyclerView config
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewCourtList.setLayoutManager(layoutManager);
        recyclerViewCourtList.setHasFixedSize(true);
        recyclerViewCourtList.setAdapter(adapter);

        recyclerViewCourtList.addOnItemTouchListener(new RecyclerItemClickListener(
                getContext(), recyclerViewCourtList,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Court selectedCourt = courtsList.get(position);

                        Bundle bundle = new Bundle();
                        bundle.putString("courtName",selectedCourt.getName());
                        bundle.putString("courtAddress",selectedCourt.getAddress());


                        CourtDetailsFragment fragmentCourtDetails = new CourtDetailsFragment();
                        fragmentCourtDetails.setArguments(bundle);

                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.viewPager, fragmentCourtDetails).commit();

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getCourtsFromFirebase();
    }
    @Override
    public void onStop() {
        super.onStop();
        courtsRef.removeEventListener(valueEventListenerCourts);
    }

    public void getCourtsFromFirebase(){
        valueEventListenerCourts = courtsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                cleanCourtsList();

                for(DataSnapshot data: snapshot.getChildren()){

                    Court court = data.getValue(Court.class);
                    courtsList.add(court);

                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void cleanCourtsList(){
        courtsList.clear();
    }
}