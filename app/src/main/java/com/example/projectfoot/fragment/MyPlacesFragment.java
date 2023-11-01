package com.example.projectfoot.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.projectfoot.AddCourtActivity;
import com.example.projectfoot.R;
import com.example.projectfoot.UpdateCourtActivity;
import com.example.projectfoot.adapter.CourtsAdapter;
import com.example.projectfoot.config.FirebaseConfig;
import com.example.projectfoot.helper.Base64Custom;
import com.example.projectfoot.helper.RecyclerItemClickListener;
import com.example.projectfoot.helper.UserFirebase;
import com.example.projectfoot.model.Contract;
import com.example.projectfoot.model.Court;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyPlacesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyPlacesFragment extends Fragment {

    private RecyclerView recyclerViewMyCourtsList;
    private DatabaseReference contractsRef;
    private FirebaseUser currentUser;
    private CourtsAdapter adapter;
    private ValueEventListener valueEventListenerMyCourts;
    private ArrayList<Contract> myContractsList = new ArrayList<>();
    private DatabaseReference courtsRef;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button buttonAddCourt;
    public MyPlacesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyPlacesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyPlacesFragment newInstance(String param1, String param2) {
        MyPlacesFragment fragment = new MyPlacesFragment();
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

        View view = inflater.inflate(R.layout.fragment_my_places, container, false);

        courtsRef = FirebaseConfig.getFirebaseDatabase();

        buttonAddCourt = view.findViewById(R.id.buttonAddCourt);
        buttonAddCourt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity() , AddCourtActivity.class);
                startActivity(i);

            }
        });

        //Initial configs
        recyclerViewMyCourtsList = view.findViewById(R.id.recyclerMyCourtsList);

        currentUser = UserFirebase.getActualUser();
        contractsRef = FirebaseConfig.getFirebaseDatabase().child("contracts");
        getMyContractsFromFirebase();

        //Adapter config
        adapter = new CourtsAdapter(myContractsList, getActivity(),0);

        //recyclerView config
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewMyCourtsList.setLayoutManager(layoutManager);
        recyclerViewMyCourtsList.setHasFixedSize(true);
        recyclerViewMyCourtsList.setAdapter(adapter);

        recyclerViewMyCourtsList.addOnItemTouchListener(new RecyclerItemClickListener(
                getContext(), recyclerViewMyCourtsList,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Contract selectedContract = myContractsList.get(position);

                        //String selectedCourtDecoded = Base64Custom.decodeBase64(selectedContract.getCourtOfTheContract());

                        Intent i = new Intent(MyPlacesFragment.this.getContext(), UpdateCourtActivity.class);
                        i.putExtra("myCourtName", selectedContract.getCourtOfTheContract());
                        startActivity(i);

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));

        swipe();

        return view;
    }

    public void getMyContractsFromFirebase(){
        valueEventListenerMyCourts = contractsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                cleanCourtsList();

                for(DataSnapshot data: snapshot.getChildren()){
                    String emailEncoded = Base64Custom.encodeBase64(Objects.requireNonNull(currentUser.getEmail()));

                    if(emailEncoded.equals(data.getKey())) {

                        for (DataSnapshot data2 : data.getChildren()) {
                            Contract contract = data2.getValue(Contract.class);
                            myContractsList.add(contract);
                        }

                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void swipe(){
        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

                int dragFlags = ItemTouchHelper.ACTION_STATE_IDLE; //bota o evento drag inativo(idle)
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                deleteTransference(viewHolder);
            }
        };

        new ItemTouchHelper(itemTouch).attachToRecyclerView(recyclerViewMyCourtsList);

    }

    public void deleteTransference(RecyclerView.ViewHolder viewHolder){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext());
        alertDialog.setTitle(R.string.delete_court);
        alertDialog.setMessage(R.string.delete_court_confirmation);
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                int position = viewHolder.getAdapterPosition();
                Contract contract;
                contract = myContractsList.get(position);
                //TODO delete contracts
                String courtName = contract.getCourtOfTheContract();
                String contractOwnerId = contract.getIdOwner();
                String contractCourtId = contract.getIdCourt();

                DatabaseReference courtToDelete = courtsRef.child("courts").child(courtName);
                courtToDelete.removeValue();
                DatabaseReference contractToDelete = contractsRef.child(contractOwnerId).child(contractCourtId);

                courtToDelete.removeValue();
                contractToDelete.removeValue();

                myContractsList.remove(position);
                adapter.notifyItemRemoved(position);

            }
        });

        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getContext(), R.string.canceled, Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();

    }

    public void cleanCourtsList(){
        myContractsList.clear();
    }
}