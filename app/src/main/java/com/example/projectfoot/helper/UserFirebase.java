package com.example.projectfoot.helper;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.projectfoot.config.FirebaseConfig;
import com.example.projectfoot.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UserFirebase {

    public static String GetUserEmail(){
        FirebaseAuth user = FirebaseConfig.getFirebaseAuth();
        String email = user.getCurrentUser().getEmail();
        String userEmail = Base64Custom.encodeBase64(email);

        return userEmail;
    }

    public static com.google.firebase.auth.FirebaseUser getActualUser(){
        FirebaseAuth user = FirebaseConfig.getFirebaseAuth();
        return user.getCurrentUser();
    }

    public static Boolean updateUserName(String name){

        try{
            com.google.firebase.auth.FirebaseUser user = getActualUser();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name).build();
            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){
                        Log.d("Profile", "Error trying to update user name");
                    }
                }
            });
            return true;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

    public static User GetLoggedUserData(){
        com.google.firebase.auth.FirebaseUser firebaseUser = getActualUser();
        User user = new User();
        user.setEmail(firebaseUser.getEmail());
        user.setName(firebaseUser.getDisplayName());

        if(firebaseUser.getPhotoUrl() == null){
            user.setPhoto("");
        }else {
            user.setPhoto(firebaseUser.getPhotoUrl().toString());
        }

        return user;

    }

    public static Boolean updateUserPhoto(Uri url){

        try{
            com.google.firebase.auth.FirebaseUser user = getActualUser();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(url).build();
            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){
                        Log.d("Profile", "Error trying to update user photo");
                    }
                }
            });
            return true;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

}
