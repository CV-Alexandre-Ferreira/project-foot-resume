package com.example.projectfoot.model;

import com.example.projectfoot.config.FirebaseConfig;
import com.example.projectfoot.helper.UserFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {

    private String id;
    private String name;
    private String email;
    private String password;
    private String photo;
    private int userType;

    public User() {
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void save(){
        DatabaseReference firebaseRef = FirebaseConfig.getFirebaseDatabase();
        DatabaseReference user = firebaseRef.child("usuarios").child(getId());

        user.setValue(this);
    }

    public void update(){
        String userEmail = UserFirebase.GetUserEmail();
        DatabaseReference database = FirebaseConfig.getFirebaseDatabase();

        DatabaseReference userRef = database.child("usuarios")
                .child(userEmail);

        Map<String, Object> userValues = convertToMap();
        userRef.updateChildren(userValues);
    }
    @Exclude
    public Map<String, Object> convertToMap(){
        HashMap<String,Object> userMap = new HashMap<>();

        userMap.put("email", getEmail());
        userMap.put("name", getName());
        userMap.put("photo", getPhoto());

        return userMap;
    }
    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
