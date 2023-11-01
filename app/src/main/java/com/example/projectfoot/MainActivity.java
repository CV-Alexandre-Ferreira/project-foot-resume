package com.example.projectfoot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.projectfoot.config.FirebaseConfig;
import com.example.projectfoot.fragment.FeedFragment;
import com.example.projectfoot.fragment.MapFragment;
import com.example.projectfoot.fragment.MyPlacesFragment;
import com.example.projectfoot.fragment.ProfileFragment;
import com.example.projectfoot.helper.UserFirebase;
import com.example.projectfoot.helper.VariablesForUsage;
import com.example.projectfoot.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class MainActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private ValueEventListener valueEventListenerUsers;
    private BottomNavigationViewEx bottomNavigationViewEx;
    public static final int COURT_USER = 1;
    private int userType = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usersRef = FirebaseConfig.getFirebaseDatabase().child("usuarios");
        currentUser = UserFirebase.getActualUser();

        bottomNavigationViewEx = findViewById(R.id.bottomNavigation);

        showButtonIfUserOwnsCourt();

        enableNavigation(bottomNavigationViewEx);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.viewPager, new FeedFragment()).commit();
    }

    @Override
    public void onStop() {
        super.onStop();
        usersRef.removeEventListener(valueEventListenerUsers);
    }

    public void showButtonIfUserOwnsCourt(){
        valueEventListenerUsers = usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                for(DataSnapshot dados: snapshot.getChildren()){

                    User user = dados.getValue(User.class);

                    String currentUserEmail = currentUser.getEmail();
                    if(currentUserEmail.equals(user.getEmail())){

                        if(user.getUserType() == 1){
                            userType = COURT_USER;
                            VariablesForUsage.userTypeVariable = COURT_USER;
                            bottomNavigationViewEx.getMenu().findItem(R.id.ic_addCourt).setVisible(true);
                        }

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void enableNavigation(BottomNavigationViewEx viewEx){
        viewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                   if(item.getItemId() == R.id.ic_home){
                        fragmentTransaction.replace(R.id.viewPager, new FeedFragment()).commit();
                        return true;
                   }

                if(item.getItemId() == R.id.ic_profile) {
                        fragmentTransaction.replace(R.id.viewPager, new ProfileFragment()).commit();
                        return true;
                }
                if(item.getItemId() == R.id.ic_addCourt) {
                    fragmentTransaction.replace(R.id.viewPager, new MyPlacesFragment()).commit();
                    return true;
                }
                if(item.getItemId() == R.id.ic_map) {
                    fragmentTransaction.replace(R.id.viewPager, new MapFragment()).commit();
                    return true;
                }

                return false;
            }
        });
    }
}