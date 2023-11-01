package com.example.projectfoot.model;

import com.example.projectfoot.config.FirebaseConfig;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class Contract {

    private String idOwner;
    private String idCourt;
    private String courtOfTheContract;
    private List<String> images;

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public void save(){
        DatabaseReference database = FirebaseConfig.getFirebaseDatabase();
        DatabaseReference conversaRef = database.child("contracts");
        conversaRef.child(this.getIdOwner()).child(this.getIdCourt())
                .setValue(this);
    }

    public String getCourtOfTheContract() {
        return courtOfTheContract;
    }

    public void setCourtOfTheContract(String courtOfTheContract) {
        this.courtOfTheContract = courtOfTheContract;
    }

    public String getIdOwner() {
        return idOwner;
    }

    public void setIdOwner(String idOwner) {
        this.idOwner = idOwner;
    }

    public String getIdCourt() {
        return idCourt;
    }

    public void setIdCourt(String idCourt) {
        this.idCourt = idCourt;
    }
}
