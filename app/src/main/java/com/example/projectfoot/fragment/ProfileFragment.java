package com.example.projectfoot.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.projectfoot.LoginActivity;
import com.example.projectfoot.R;
import com.example.projectfoot.config.FirebaseConfig;
import com.example.projectfoot.helper.Base64Custom;
import com.example.projectfoot.helper.UserFirebase;
import com.example.projectfoot.model.Contract;
import com.example.projectfoot.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button buttonLogout;
    private static FirebaseAuth auth;
    private TextView textViewName, textViewEmail;
    private ValueEventListener valueEventListenerUser;
    private DatabaseReference usersRef;
    private FirebaseUser currentUser;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        auth = FirebaseConfig.getFirebaseAuth();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        usersRef = FirebaseConfig.getFirebaseDatabase().child("usuarios");
        currentUser = UserFirebase.getActualUser();
        getUserDataFromFirebase();

        textViewName = view.findViewById(R.id.tvName);
        textViewEmail = view.findViewById(R.id.tvEmail);
        buttonLogout = view.findViewById(R.id.buttonLogOut);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                auth.signOut();
                Intent i = new Intent(getActivity(), LoginActivity.class);
                startActivity(i);
                getActivity().finish();
            }
        });

        return view;
    }

    public void getUserDataFromFirebase(){
        valueEventListenerUser = usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                for(DataSnapshot data: snapshot.getChildren()){
                    String emailEncoded = Base64Custom.encodeBase64(Objects.requireNonNull(currentUser.getEmail()));

                    if(emailEncoded.equals(data.getKey())) {

                            User user = data.getValue(User.class);
                            textViewName.setText(Objects.requireNonNull(user).getName());
                            textViewEmail.setText(user.getEmail());

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}