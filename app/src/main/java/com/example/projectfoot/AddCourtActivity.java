package com.example.projectfoot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.projectfoot.config.FirebaseConfig;
import com.example.projectfoot.helper.Base64Custom;
import com.example.projectfoot.helper.Permissions;
import com.example.projectfoot.helper.UserFirebase;
import com.example.projectfoot.model.AdditionalFeature;
import com.example.projectfoot.model.Contract;
import com.example.projectfoot.model.Court;
import com.example.projectfoot.model.OpeningHours;
import com.example.projectfoot.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import dmax.dialog.SpotsDialog;

public class AddCourtActivity extends AppCompatActivity
        implements View.OnClickListener{

    private Button buttonAddCourtToFirebase;
    private EditText editTexCourtName, editTextPhone;
    private CheckBox checkBoxMonday, checkBoxTuesday, checkBoxWednesday, checkBoxThursday, checkBoxFriday, checkBoxSaturday, checkBoxSunday;
    private Spinner spinnerOpeningMonday, spinnerEndingMonday, spinnerOpeningTuesday, spinnerEndingTuesday, spinnerOpeningWednesday, spinnerEndingWednesday,
            spinnerOpeningThursday, spinnerEndingThursday, spinnerOpeningFriday, spinnerEndingFriday, spinnerOpeningSaturday, spinnerEndingSaturday,
            spinnerOpeningSunday, spinnerEndingSunday;
    private OpeningHours openingHours;
    private EditText editTextCourtAddress;
    private CheckBox checkboxWifi;
    private CheckBox checkboxRestroom;
    private CheckBox checkboxFood;
    private List<String> imagesList = new ArrayList<>();
    private ImageView image1, image2, image3;
    //private ImageView imageShower, imageFood, imageTelevision;
    private StorageReference storage;
    private Contract contract;
    private Court court;
    private List<String> listURLImages = new ArrayList<>();
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_court);

        Permissions.validatePermissions(permissions(),this, 1);

        setComponents();
        loadSpinnerData();

        image1.setOnClickListener(this);
        image2.setOnClickListener(this);
        image3.setOnClickListener(this);

        buttonAddCourtToFirebase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (imagesList.size() > 0 && checkIfHaveAtLeastOneOpeningHour() && checkIfHaveAddress()){

                    FirebaseUser currentUser = UserFirebase.getActualUser();
                String idOwner = Base64Custom.encodeBase64(currentUser.getEmail());

                court = new Court();
                contract = new Contract();

                String courtName = editTexCourtName.getText().toString();
                String courtAddress = editTextCourtAddress.getText().toString();
                String courtContactPhone = editTextPhone.getText().toString();
                court.setName(courtName);
                court.setAddress(courtAddress);
                court.setContact_phone(courtContactPhone);
                String idCourt = Base64Custom.encodeBase64(court.getName());

                contract.setIdOwner(idOwner);
                contract.setIdCourt(idCourt);
                contract.setCourtOfTheContract(courtName);

                dialog = new SpotsDialog.Builder()
                        .setContext(getApplicationContext())
                        .setMessage(R.string.saving_court)
                        .setCancelable(false)
                        .build();
                //dialog.show();

                for (int i = 0; i < imagesList.size(); i++) {
                    String urlImagem = imagesList.get(i);
                    int tamanhoLista = imagesList.size();
                    saveImageOnFirebaseStorage(urlImagem, tamanhoLista, i);
                }

                contract.save();

                /**
                 * Geocoding -> address/description TO lag long
                 * Reverse Geocoding -> lag/long TO address
                 **/

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                try {
                    //List<Address> listaEndereço = geocoder.getFromLocation(latitude, longitude, 1);
                    List<Address> addressList = geocoder.getFromLocationName(courtAddress, 1);
                    if (addressList != null && addressList.size() > 0) {
                        String addressLine = addressList.get(0).getAddressLine(0);

                        court.setAddress(addressLine);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


                List<AdditionalFeature> selectedFeatures = new ArrayList<>();
                if (checkboxWifi.isChecked()) {
                    selectedFeatures.add(AdditionalFeature.WIFI);
                }
                if (checkboxRestroom.isChecked()) {
                    selectedFeatures.add(AdditionalFeature.RESTROOM);
                }
                if (checkboxFood.isChecked()) {
                    selectedFeatures.add(AdditionalFeature.BAR);
                }

                court.setAdditionalFeatures(selectedFeatures);
                saveOpeningHours();
                court.setOpeningHours(openingHours);

                court.save();

                finish();
            }
                else if (!checkIfHaveAddress())
                    Toast.makeText(AddCourtActivity.this, R.string.put_valid_address, Toast.LENGTH_SHORT).show();
                else if (!checkIfHaveAtLeastOneOpeningHour()) Toast.makeText(AddCourtActivity.this, R.string.add_1_opening_hour, Toast.LENGTH_SHORT).show();
                else Toast.makeText(AddCourtActivity.this, R.string.add_1_image, Toast.LENGTH_SHORT).show();
            }
        });

        storage = FirebaseConfig.getFirebaseStorage();
    }

    public static String[] storage_permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] storage_permissions_33 = {
            Manifest.permission.READ_MEDIA_IMAGES
    };

    public static String[] permissions() {
        String[] p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            p = storage_permissions_33;
        } else {
            p = storage_permissions;
        }
        return p;
    }


    public void chooseImage(int requestCode){

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.imageCourt1) chooseImage(1);
        if(view.getId() == R.id.imageCourt2) chooseImage(2);
        if(view.getId() == R.id.imageCourt3) chooseImage(3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            //Recover image
            Uri selectedImage = data.getData();
            String imagePath = selectedImage.toString();

            //Set image on ImageView
            if(requestCode == 1) {
                image1.setImageURI(selectedImage);

            }else if(requestCode == 2){image2.setImageURI(selectedImage);

            }
            else if (requestCode == 3){image3.setImageURI(selectedImage);

            }

            imagesList.add(imagePath);

        }
    }

    private void saveImageOnFirebaseStorage(String urlString, int totalImages, int counter){

        //Criate node on storage
        //Change later to court.getId ...
        final StorageReference imageCourt = storage.child("imagens")
                .child("courts").child(contract.getIdCourt())
                .child("image"+counter);

        UploadTask uploadTask = imageCourt.putFile( Uri.parse(urlString) );
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imageCourt.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri firebaseUrl = task.getResult();
                        String urlConverted = firebaseUrl.toString();

                        listURLImages.add(urlConverted);

                        if(totalImages == listURLImages.size()){

                            contract.setImages(listURLImages);
                            court.setImages(listURLImages);
                            court.save();

                            dialog.dismiss();
                            finish();

                        }
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //exibirMensagemErro("Falha ao fazer upload");
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int permissaoResultado:grantResults){

            if(permissaoResultado == PackageManager.PERMISSION_DENIED){

                Permissions.validatePermissions(permissions,this, 1);

            }

        }
    }

    private void validatePermissionsAlert(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permissions_denied);
        builder.setMessage(R.string.accept_permissions);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onCheckBoxMondayClicked(View view){

        if(checkBoxMonday.isChecked()){

            spinnerOpeningMonday.setVisibility(View.VISIBLE);
            spinnerEndingMonday.setVisibility(View.VISIBLE);

        }
        else {
            spinnerOpeningMonday.setVisibility(View.GONE);
            spinnerEndingMonday.setVisibility(View.GONE);
        }

    }

    public void onCheckBoxTuesdayClicked(View view){

        if(checkBoxTuesday.isChecked()){
            spinnerOpeningTuesday.setVisibility(View.VISIBLE);
            spinnerEndingTuesday.setVisibility(View.VISIBLE);
        }
        else {
            spinnerOpeningTuesday.setVisibility(View.GONE);
            spinnerEndingTuesday.setVisibility(View.GONE);
        }

    }

    public void onCheckBoxWednesdayClicked(View view){

        if(checkBoxWednesday.isChecked()){
            spinnerOpeningWednesday.setVisibility(View.VISIBLE);
            spinnerEndingWednesday.setVisibility(View.VISIBLE);
        }
        else {
            spinnerOpeningWednesday.setVisibility(View.GONE);
            spinnerEndingWednesday.setVisibility(View.GONE);
        }

    }

    public void onCheckBoxThursdayClicked(View view){

        if(checkBoxThursday.isChecked()){
            spinnerOpeningThursday.setVisibility(View.VISIBLE);
            spinnerEndingThursday.setVisibility(View.VISIBLE);

        }
        else {
            spinnerOpeningThursday.setVisibility(View.GONE);
            spinnerEndingThursday.setVisibility(View.GONE);

        }
    }

    public void onCheckBoxFridayClicked(View view){

        if(checkBoxFriday.isChecked()){
            spinnerOpeningFriday.setVisibility(View.VISIBLE);
            spinnerEndingFriday.setVisibility(View.VISIBLE);
        }
        else {
            spinnerOpeningFriday.setVisibility(View.GONE);
            spinnerEndingFriday.setVisibility(View.GONE);
        }

    }

    public void onCheckBoxSaturdayClicked(View view){

        if(checkBoxSaturday.isChecked()){
            spinnerOpeningSaturday.setVisibility(View.VISIBLE);
            spinnerEndingSaturday.setVisibility(View.VISIBLE);
        }
        else {
            spinnerOpeningSaturday.setVisibility(View.GONE);
            spinnerEndingSaturday.setVisibility(View.GONE);
        }

    }

    public void onCheckBoxSundayClicked(View view){

        if(checkBoxSunday.isChecked()){
            spinnerOpeningSunday.setVisibility(View.VISIBLE);
            spinnerEndingSunday.setVisibility(View.VISIBLE);
        }
        else {
            spinnerOpeningSunday.setVisibility(View.GONE);
            spinnerEndingSunday.setVisibility(View.GONE);
        }

    }

    private void loadSpinnerData(){

        String[] times = getResources().getStringArray(R.array.opening_closing);

        ArrayAdapter<String > adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, times
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOpeningMonday.setAdapter(adapter);
        spinnerEndingMonday.setAdapter(adapter);
        spinnerOpeningTuesday.setAdapter(adapter);
        spinnerEndingTuesday.setAdapter(adapter);
        spinnerOpeningWednesday.setAdapter(adapter);
        spinnerEndingWednesday.setAdapter(adapter);
        spinnerOpeningThursday.setAdapter(adapter);
        spinnerEndingThursday.setAdapter(adapter);
        spinnerOpeningFriday.setAdapter(adapter);
        spinnerEndingFriday.setAdapter(adapter);
        spinnerOpeningSaturday.setAdapter(adapter);
        spinnerEndingSaturday.setAdapter(adapter);
        spinnerOpeningSunday.setAdapter(adapter);
        spinnerEndingSunday.setAdapter(adapter);

    }

    private void saveOpeningHours(){

        openingHours = new OpeningHours();

        List<String> m = Arrays.asList(spinnerOpeningMonday.getSelectedItem().toString(), spinnerEndingMonday.getSelectedItem().toString());
        List<String> t = Arrays.asList(spinnerOpeningTuesday.getSelectedItem().toString(), spinnerEndingTuesday.getSelectedItem().toString());
        List<String> w = Arrays.asList(spinnerOpeningWednesday.getSelectedItem().toString(), spinnerEndingWednesday.getSelectedItem().toString());
        List<String> th = Arrays.asList(spinnerOpeningThursday.getSelectedItem().toString(), spinnerEndingThursday.getSelectedItem().toString());
        List<String> f = Arrays.asList(spinnerOpeningFriday.getSelectedItem().toString(), spinnerEndingFriday.getSelectedItem().toString());
        List<String> s = Arrays.asList(spinnerOpeningSaturday.getSelectedItem().toString(), spinnerEndingSaturday.getSelectedItem().toString());
        List<String> su = Arrays.asList(spinnerOpeningSunday.getSelectedItem().toString(), spinnerEndingSunday.getSelectedItem().toString());
        openingHours.setMonday(m);
        openingHours.setTuesday(t);
        openingHours.setWednesday(w);
        openingHours.setThursday(th);
        openingHours.setFriday(f);
        openingHours.setSaturday(s);
        openingHours.setSunday(su);

    }

    public boolean checkIfHaveAtLeastOneOpeningHour(){

        return checkBoxSunday.isChecked() || checkBoxMonday.isChecked() || checkBoxTuesday.isChecked() || checkBoxWednesday.isChecked() || checkBoxThursday.isChecked()
                || checkBoxFriday.isChecked() || checkBoxSaturday.isChecked();

    }
    public boolean checkIfHaveAddress(){

        return (!editTextCourtAddress.getText().toString().equals("") &&
                editTextCourtAddress.length() > 5);

    }

    public void setComponents(){
        editTexCourtName = findViewById(R.id.editTextCourtName);
        editTextCourtAddress = findViewById(R.id.editTextCourtAddress);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonAddCourtToFirebase = findViewById(R.id.buttonAddCourtToFirebase);

        checkBoxMonday = findViewById(R.id.checkbox_monday);
        checkBoxTuesday = findViewById(R.id.checkbox_tuesday);
        checkBoxWednesday = findViewById(R.id.checkbox_wednesday);
        checkBoxThursday = findViewById(R.id.checkbox_thursday);
        checkBoxFriday = findViewById(R.id.checkbox_friday);
        checkBoxSaturday = findViewById(R.id.checkbox_saturday);
        checkBoxSunday = findViewById(R.id.checkbox_sunday);

        spinnerOpeningMonday = findViewById(R.id.spinnerOpeningMonday);
        spinnerEndingMonday = findViewById(R.id.spinnerEndingMonday);
        spinnerOpeningTuesday = findViewById(R.id.spinnerOpeningTuesday);
        spinnerEndingTuesday = findViewById(R.id.spinnerEndingTuesday);
        spinnerOpeningWednesday = findViewById(R.id.spinnerOpeningWednesday);
        spinnerEndingWednesday = findViewById(R.id.spinnerEndingWednesday);
        spinnerOpeningThursday = findViewById(R.id.spinnerOpeningThursday);
        spinnerEndingThursday = findViewById(R.id.spinnerEndingThursday);
        spinnerOpeningFriday = findViewById(R.id.spinnerOpeningFriday);
        spinnerEndingFriday = findViewById(R.id.spinnerEndingFriday);
        spinnerOpeningSaturday = findViewById(R.id.spinnerOpeningSaturday);
        spinnerEndingSaturday = findViewById(R.id.spinnerEndingSaturday);
        spinnerOpeningSunday = findViewById(R.id.spinnerOpeningSunday);
        spinnerEndingSunday = findViewById(R.id.spinnerEndingSunday);

        image1 = findViewById(R.id.imageCourt1);
        image2 = findViewById(R.id.imageCourt2);
        image3 = findViewById(R.id.imageCourt3);


        checkboxWifi = findViewById(R.id.checkbox_wifi);
        checkboxRestroom = findViewById(R.id.checkbox_restroom);
        checkboxFood = findViewById(R.id.checkbox_bar);
    }

}