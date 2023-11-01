package com.example.projectfoot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
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
import com.example.projectfoot.model.AdditionalFeature;
import com.example.projectfoot.model.Contract;
import com.example.projectfoot.model.Court;
import com.example.projectfoot.model.OpeningHours;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class UpdateCourtActivity extends AppCompatActivity {
    private DatabaseReference courtsRef;
    private ValueEventListener valueEventListenerCourts;
    private EditText editTextCourtName, editTextCourtAddress, editTextCourtContactPhone;
    private CheckBox checkBox_wifi_update, checkBox_restroom_update, checkBox_bar_update;
    private ImageView imageCourt1update;
    private List<String> defaultOpening = Arrays.asList("00:00", "00:00");
    private CheckBox checkbox_monday_update, checkbox_tuesday_update, checkbox_wednesday_update, checkbox_thursday_update,
            checkbox_friday_update, checkbox_saturday_update, checkbox_sunday_update;
    private Spinner spinnerOpeningMondayUpdate, spinnerOpeningTuesdayUpdate, spinnerOpeningWednesdayUpdate, spinnerOpeningThursdayUpdate,
            spinnerOpeningFridayUpdate, spinnerOpeningSaturdayUpdate, spinnerOpeningSundayUpdate;
    private Spinner spinnerEndingMondayUpdate, spinnerEndingTuesdayUpdate, spinnerEndingWednesdayUpdate, spinnerEndingThursdayUpdate,
            spinnerEndingFridayUpdate, spinnerEndingSaturdayUpdate, spinnerEndingSundayUpdate;
    StorageReference storageRef;
    private Button updateButton;
    private Court court;
    private List<AdditionalFeature> additionalFeatures = new ArrayList<>();
    private OpeningHours openingHours = new OpeningHours();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_court);

        court = new Court();

        editTextCourtName = findViewById(R.id.editTextCourtNameUpdate);
        editTextCourtAddress = findViewById(R.id.editTextCourtAddressUpdate);
        editTextCourtContactPhone = findViewById(R.id.editTextPhoneUpdate);
        checkBox_bar_update = findViewById(R.id.checkbox_bar_update);
        checkBox_restroom_update = findViewById(R.id.checkbox_restroom_update);
        checkBox_wifi_update = findViewById(R.id.checkbox_wifi_update);
        imageCourt1update = findViewById(R.id.imageCourt1update);

        checkbox_monday_update = findViewById(R.id.checkbox_monday_update);
        checkbox_tuesday_update = findViewById(R.id.checkbox_tuesday_update);
        checkbox_wednesday_update = findViewById(R.id.checkbox_wednesday_update);
        checkbox_thursday_update = findViewById(R.id.checkbox_thursday_update);
        checkbox_friday_update = findViewById(R.id.checkbox_friday_update);
        checkbox_saturday_update = findViewById(R.id.checkbox_saturday_update);
        checkbox_sunday_update = findViewById(R.id.checkbox_sunday_update);

        spinnerOpeningMondayUpdate = findViewById(R.id.spinnerOpeningMondayUpdate);
        spinnerOpeningTuesdayUpdate = findViewById(R.id.spinnerOpeningTuesdayUpdate);
        spinnerOpeningWednesdayUpdate = findViewById(R.id.spinnerOpeningWednesdayUpdate);
        spinnerOpeningThursdayUpdate = findViewById(R.id.spinnerOpeningThursdayUpdate);
        spinnerOpeningFridayUpdate = findViewById(R.id.spinnerOpeningFridayUpdate);
        spinnerOpeningSaturdayUpdate = findViewById(R.id.spinnerOpeningSaturdayUpdate);
        spinnerOpeningSundayUpdate = findViewById(R.id.spinnerOpeningSundayUpdate);

        spinnerEndingMondayUpdate = findViewById(R.id.spinnerEndingMondayUpdate);
        spinnerEndingTuesdayUpdate = findViewById(R.id.spinnerEndingTuesdayUpdate);
        spinnerEndingWednesdayUpdate = findViewById(R.id.spinnerEndingWednesdayUpdate);
        spinnerEndingThursdayUpdate = findViewById(R.id.spinnerEndingThursdayUpdate);
        spinnerEndingFridayUpdate = findViewById(R.id.spinnerEndingFridayUpdate);
        spinnerEndingSaturdayUpdate = findViewById(R.id.spinnerEndingSaturdayUpdate);
        spinnerEndingSundayUpdate = findViewById(R.id.spinnerEndingSundayUpdate);
        updateButton = findViewById(R.id.buttonUpdateCourt);

        loadSpinnerData();

        courtsRef = FirebaseConfig.getFirebaseDatabase().child("courts");
        storageRef = FirebaseConfig.getFirebaseStorage();

        getCourtsFromFirebase();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                court.setName(editTextCourtName.getText().toString());
                court.setAddress(editTextCourtAddress.getText().toString());
                court.setContact_phone(editTextCourtContactPhone.getText().toString());

                if(checkBox_wifi_update.isChecked()) additionalFeatures.add(AdditionalFeature.WIFI);
                if(checkBox_restroom_update.isChecked()) additionalFeatures.add(AdditionalFeature.RESTROOM);
                if(checkBox_bar_update.isChecked()) additionalFeatures.add(AdditionalFeature.BAR);

                court.setAdditionalFeatures(additionalFeatures);

                /**
                 * Geocoding -> address/description TO lag long
                 * Reverse Geocoding -> lag/long TO address
                 **/
                String courtAddress = editTextCourtAddress.getText().toString();
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

                if(checkbox_monday_update.isChecked()) {
                    List<String> dayOH = Arrays.asList(spinnerOpeningMondayUpdate.getSelectedItem().toString(), spinnerEndingMondayUpdate.getSelectedItem().toString());
                    openingHours.setMonday(dayOH);
                }else openingHours.setMonday(defaultOpening);
                if(checkbox_tuesday_update.isChecked()) {
                    List<String> dayOH = Arrays.asList(spinnerOpeningTuesdayUpdate.getSelectedItem().toString(), spinnerEndingTuesdayUpdate.getSelectedItem().toString());
                    openingHours.setTuesday(dayOH);
                }else openingHours.setTuesday(defaultOpening);

                if(checkbox_wednesday_update.isChecked()) {
                    List<String> dayOH = Arrays.asList(spinnerOpeningWednesdayUpdate.getSelectedItem().toString(), spinnerEndingWednesdayUpdate.getSelectedItem().toString());
                    openingHours.setWednesday(dayOH);
                }else openingHours.setWednesday(defaultOpening);

                if(checkbox_thursday_update.isChecked()) {
                    List<String> dayOH = Arrays.asList(spinnerOpeningThursdayUpdate.getSelectedItem().toString(), spinnerEndingThursdayUpdate.getSelectedItem().toString());
                    openingHours.setThursday(dayOH);
                }else openingHours.setThursday(defaultOpening);

                if(checkbox_friday_update.isChecked()) {
                    List<String> dayOH = Arrays.asList(spinnerOpeningFridayUpdate.getSelectedItem().toString(), spinnerEndingFridayUpdate.getSelectedItem().toString());
                    openingHours.setFriday(dayOH);
                }else openingHours.setFriday(defaultOpening);

                if(checkbox_saturday_update.isChecked()) {
                    List<String> dayOH = Arrays.asList(spinnerOpeningSaturdayUpdate.getSelectedItem().toString(), spinnerEndingSaturdayUpdate.getSelectedItem().toString());
                    openingHours.setSaturday(dayOH);
                }else openingHours.setSaturday(defaultOpening);

                if(checkbox_sunday_update.isChecked()) {
                    List<String> dayOH = Arrays.asList(spinnerOpeningSundayUpdate.getSelectedItem().toString(), spinnerEndingSundayUpdate.getSelectedItem().toString());
                    openingHours.setSunday(dayOH);
                }else openingHours.setSunday(defaultOpening);

                court.setOpeningHours(openingHours);
                court.update();

                finish();
            }
        });
    }

    public void getCourtsFromFirebase(){

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String courtName = extras.getString("myCourtName");
            valueEventListenerCourts = courtsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for(DataSnapshot data: snapshot.getChildren()) {

                        court = data.getValue(Court.class);
                        assert court != null;
                        if(court.getName() != null){
                        if (court.getName().equals(courtName)) {
                            editTextCourtName.setText(court.getName());
                            editTextCourtAddress.setText(court.getAddress());
                            editTextCourtContactPhone.setText(court.getContact_phone());

                            if (court.getAdditionalFeatures() != null) {
                                for (int position = 0; position < court.getAdditionalFeatures().size(); position++) {
                                    if (court.getAdditionalFeatures().get(position).equals(AdditionalFeature.WIFI))
                                        checkBox_wifi_update.setChecked(true);
                                    else if (court.getAdditionalFeatures().get(position).equals(AdditionalFeature.RESTROOM))
                                        checkBox_restroom_update.setChecked(true);
                                    else if (court.getAdditionalFeatures().get(position).equals(AdditionalFeature.BAR))
                                        checkBox_bar_update.setChecked(true);
                                }
                            }
                            String courtId = Base64Custom.encodeBase64(courtName) + "/";
                            StorageReference imageRef = storageRef.child("imagens/courts/" + courtId + "image0");

                            try {
                                final File localFile = File.createTempFile("courtImage", "jpg");
                                imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                        imageCourt1update.setImageBitmap(bitmap);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(UpdateCourtActivity.this, R.string.error_on_image_upload, Toast.LENGTH_LONG).show();

                                    }
                                });

                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            if (!court.getOpeningHours().getMonday().equals(defaultOpening)) {
                                checkbox_monday_update.setChecked(true);
                                spinnerOpeningMondayUpdate.setVisibility(View.VISIBLE);
                                spinnerEndingMondayUpdate.setVisibility(View.VISIBLE);

                                String spinnerValueOpening = court.getOpeningHours().getMonday().get(0); //the value you want the position for
                                String spinnerValueEnding = court.getOpeningHours().getMonday().get(1);
                                ArrayAdapter myAdapterOpening = (ArrayAdapter) spinnerOpeningMondayUpdate.getAdapter(); //cast to an ArrayAdapter
                                ArrayAdapter myAdapterEnding = (ArrayAdapter) spinnerEndingMondayUpdate.getAdapter();
                                int spinnerPositionOpening = myAdapterOpening.getPosition(spinnerValueOpening);
                                int spinnerPositionEnding = myAdapterEnding.getPosition(spinnerValueEnding);


                                spinnerOpeningMondayUpdate.setSelection(spinnerPositionOpening);
                                spinnerEndingMondayUpdate.setSelection(spinnerPositionEnding);
                            }

                            if (!court.getOpeningHours().getTuesday().equals(defaultOpening)) {
                                checkbox_tuesday_update.setChecked(true);
                                spinnerOpeningTuesdayUpdate.setVisibility(View.VISIBLE);
                                spinnerEndingTuesdayUpdate.setVisibility(View.VISIBLE);

                                String spinnerValueOpening = court.getOpeningHours().getTuesday().get(0); //the value you want the position for
                                String spinnerValueEnding = court.getOpeningHours().getTuesday().get(1);
                                ArrayAdapter myAdapterOpening = (ArrayAdapter) spinnerOpeningTuesdayUpdate.getAdapter(); //cast to an ArrayAdapter
                                ArrayAdapter myAdapterEnding = (ArrayAdapter) spinnerEndingTuesdayUpdate.getAdapter();
                                int spinnerPositionOpening = myAdapterOpening.getPosition(spinnerValueOpening);
                                int spinnerPositionEnding = myAdapterEnding.getPosition(spinnerValueEnding);


                                spinnerOpeningTuesdayUpdate.setSelection(spinnerPositionOpening);
                                spinnerEndingTuesdayUpdate.setSelection(spinnerPositionEnding);
                            }

                            if (!court.getOpeningHours().getWednesday().equals(defaultOpening)) {

                                Log.d("myWednesday", court.getOpeningHours().getWednesday().toString());
                                Log.d("myWednesdayDefault", defaultOpening.toString());
                                checkbox_wednesday_update.setChecked(true);
                                spinnerOpeningWednesdayUpdate.setVisibility(View.VISIBLE);
                                spinnerEndingWednesdayUpdate.setVisibility(View.VISIBLE);

                                String spinnerValueOpening = court.getOpeningHours().getWednesday().get(0); //the value you want the position for
                                String spinnerValueEnding = court.getOpeningHours().getWednesday().get(1);
                                ArrayAdapter myAdapterOpening = (ArrayAdapter) spinnerOpeningWednesdayUpdate.getAdapter(); //cast to an ArrayAdapter
                                ArrayAdapter myAdapterEnding = (ArrayAdapter) spinnerEndingWednesdayUpdate.getAdapter();
                                int spinnerPositionOpening = myAdapterOpening.getPosition(spinnerValueOpening);
                                int spinnerPositionEnding = myAdapterEnding.getPosition(spinnerValueEnding);


                                spinnerOpeningWednesdayUpdate.setSelection(spinnerPositionOpening);
                                spinnerEndingWednesdayUpdate.setSelection(spinnerPositionEnding);
                            }

                            if (!court.getOpeningHours().getThursday().equals(defaultOpening)) {
                                checkbox_thursday_update.setChecked(true);
                                spinnerOpeningThursdayUpdate.setVisibility(View.VISIBLE);
                                spinnerEndingThursdayUpdate.setVisibility(View.VISIBLE);

                                String spinnerValueOpening = court.getOpeningHours().getThursday().get(0); //the value you want the position for
                                String spinnerValueEnding = court.getOpeningHours().getThursday().get(1);
                                ArrayAdapter myAdapterOpening = (ArrayAdapter) spinnerOpeningThursdayUpdate.getAdapter(); //cast to an ArrayAdapter
                                ArrayAdapter myAdapterEnding = (ArrayAdapter) spinnerEndingThursdayUpdate.getAdapter();
                                int spinnerPositionOpening = myAdapterOpening.getPosition(spinnerValueOpening);
                                int spinnerPositionEnding = myAdapterEnding.getPosition(spinnerValueEnding);


                                spinnerOpeningThursdayUpdate.setSelection(spinnerPositionOpening);
                                spinnerEndingThursdayUpdate.setSelection(spinnerPositionEnding);
                            }

                            if (!court.getOpeningHours().getFriday().equals(defaultOpening)) {
                                checkbox_friday_update.setChecked(true);
                                spinnerOpeningFridayUpdate.setVisibility(View.VISIBLE);
                                spinnerEndingFridayUpdate.setVisibility(View.VISIBLE);

                                String spinnerValueOpening = court.getOpeningHours().getFriday().get(0); //the value you want the position for
                                String spinnerValueEnding = court.getOpeningHours().getFriday().get(1);
                                ArrayAdapter myAdapterOpening = (ArrayAdapter) spinnerOpeningFridayUpdate.getAdapter(); //cast to an ArrayAdapter
                                ArrayAdapter myAdapterEnding = (ArrayAdapter) spinnerEndingFridayUpdate.getAdapter();
                                int spinnerPositionOpening = myAdapterOpening.getPosition(spinnerValueOpening);
                                int spinnerPositionEnding = myAdapterEnding.getPosition(spinnerValueEnding);


                                spinnerOpeningFridayUpdate.setSelection(spinnerPositionOpening);
                                spinnerEndingFridayUpdate.setSelection(spinnerPositionEnding);
                            }

                            if (!court.getOpeningHours().getSaturday().equals(defaultOpening)) {
                                checkbox_saturday_update.setChecked(true);
                                spinnerOpeningSaturdayUpdate.setVisibility(View.VISIBLE);
                                spinnerEndingSaturdayUpdate.setVisibility(View.VISIBLE);

                                String spinnerValueOpening = court.getOpeningHours().getSaturday().get(0); //the value you want the position for
                                String spinnerValueEnding = court.getOpeningHours().getSaturday().get(1);
                                ArrayAdapter myAdapterOpening = (ArrayAdapter) spinnerOpeningSaturdayUpdate.getAdapter(); //cast to an ArrayAdapter
                                ArrayAdapter myAdapterEnding = (ArrayAdapter) spinnerEndingSaturdayUpdate.getAdapter();
                                int spinnerPositionOpening = myAdapterOpening.getPosition(spinnerValueOpening);
                                int spinnerPositionEnding = myAdapterEnding.getPosition(spinnerValueEnding);


                                spinnerOpeningSaturdayUpdate.setSelection(spinnerPositionOpening);
                                spinnerEndingSaturdayUpdate.setSelection(spinnerPositionEnding);
                            }

                            if (!court.getOpeningHours().getSunday().equals(defaultOpening)) {
                                checkbox_sunday_update.setChecked(true);
                                spinnerOpeningSundayUpdate.setVisibility(View.VISIBLE);
                                spinnerEndingSundayUpdate.setVisibility(View.VISIBLE);

                                String spinnerValueOpening = court.getOpeningHours().getSunday().get(0); //the value you want the position for
                                String spinnerValueEnding = court.getOpeningHours().getSunday().get(1);
                                ArrayAdapter myAdapterOpening = (ArrayAdapter) spinnerOpeningSundayUpdate.getAdapter(); //cast to an ArrayAdapter
                                ArrayAdapter myAdapterEnding = (ArrayAdapter) spinnerEndingSundayUpdate.getAdapter();
                                int spinnerPositionOpening = myAdapterOpening.getPosition(spinnerValueOpening);
                                int spinnerPositionEnding = myAdapterEnding.getPosition(spinnerValueEnding);


                                spinnerOpeningSundayUpdate.setSelection(spinnerPositionOpening);
                                spinnerEndingSundayUpdate.setSelection(spinnerPositionEnding);
                            }

                        }
                    }

                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }

    private void loadSpinnerData(){

        String[] times = getResources().getStringArray(R.array.opening_closing);

        ArrayAdapter<String > adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, times
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOpeningMondayUpdate.setAdapter(adapter);
        spinnerEndingMondayUpdate.setAdapter(adapter);
        spinnerOpeningTuesdayUpdate.setAdapter(adapter);
        spinnerEndingTuesdayUpdate.setAdapter(adapter);
        spinnerOpeningWednesdayUpdate.setAdapter(adapter);
        spinnerEndingWednesdayUpdate.setAdapter(adapter);
        spinnerOpeningThursdayUpdate.setAdapter(adapter);
        spinnerEndingThursdayUpdate.setAdapter(adapter);
        spinnerOpeningFridayUpdate.setAdapter(adapter);
        spinnerEndingFridayUpdate.setAdapter(adapter);
        spinnerOpeningSaturdayUpdate.setAdapter(adapter);
        spinnerEndingSaturdayUpdate.setAdapter(adapter);
        spinnerOpeningSundayUpdate.setAdapter(adapter);
        spinnerEndingSundayUpdate.setAdapter(adapter);

    }

    @Override
    public void onStop() {
        super.onStop();
        courtsRef.removeEventListener(valueEventListenerCourts);
    }

}