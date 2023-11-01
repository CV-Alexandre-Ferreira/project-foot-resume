package com.example.projectfoot.model;

import androidx.annotation.NonNull;

import com.example.projectfoot.config.FirebaseConfig;
import com.example.projectfoot.helper.UserFirebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Court {
    private String name;
    private String contact_phone;
    private String address;
    private List<String> images;
    private List<AdditionalFeature> additionalFeatures;
    private OpeningHours openingHours;

    public List<AdditionalFeature> getAdditionalFeatures() {
        return additionalFeatures;
    }

    public void setAdditionalFeatures(List<AdditionalFeature> additionalFeatures) {
        this.additionalFeatures = additionalFeatures;
    }

    public List<String> getImages() {
        return images;
    }
    public void setImages(List<String> images) {
        this.images = images;
    }

    public Court() {

    };

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    public String getContact_phone() {
        return contact_phone;
    }

    public void setContact_phone(String contact_phone) {
        this.contact_phone = contact_phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String nome) {
        this.name = nome;
    }

    public void save(){
        DatabaseReference database = FirebaseConfig.getFirebaseDatabase();
        DatabaseReference converseRef = database.child("courts");
        converseRef.child(this.name).setValue(this);
    }

    public void update(){
        String courtName = this.getName();
        DatabaseReference database = FirebaseConfig.getFirebaseDatabase();

        DatabaseReference courtsRef = database.child("courts")
                .child(courtName);

        Map<String, Object> courtValues = convertToMap();
        courtsRef.updateChildren(courtValues);
    }

    @Exclude
    public Map<String, Object> convertToMap(){
        HashMap<String,Object> userMap = new HashMap<>();

        //TODO
        //userMap.put("name", getName());
        userMap.put("address", getAddress());
        userMap.put("contact_phone", getContact_phone());
        //TODO
        //userMap.put("images", getImages());
        userMap.put("openingHours", getOpeningHours());
        userMap.put("additionalFeatures", getAdditionalFeatures());

        return userMap;
    }

}
