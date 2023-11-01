package com.example.projectfoot.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectfoot.R;
import com.example.projectfoot.config.FirebaseConfig;
import com.example.projectfoot.helper.Base64Custom;
import com.example.projectfoot.helper.VariablesForUsage;
import com.example.projectfoot.model.AdditionalFeature;
import com.example.projectfoot.model.Court;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CourtDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CourtDetailsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String COURT_NAME = "court-name";
    public static final String COURT_ADDRESS = "court-address";

    // TODO: Rename and change types of parameters

    private String courtName;
    private String courtAddress;
    private CarouselView carouselView;
    int[] sampleImages = {R.drawable.defaultpic, R.drawable.defaultpic, R.drawable.defaultpic, R.drawable.defaultpic, R.drawable.defaultpic};
    List<Bitmap> courtImages = new ArrayList<>();
    private Bundle bundle;
    private ImageView imageViewBar, imageViewRestroom, imageViewWifi;
    private ValueEventListener valueEventListenerCourts;
    private DatabaseReference courtsRef;
    private StorageReference storageRef;
    private List<StorageReference> imageRefs = new ArrayList<>();
    private ImageView imageHolder;
    private int imageController = -1;
    private List<String> defaultOpening = Arrays.asList("00:00", "00:00");
    private Button buttonRentCourt;
    private TextView opening_monday_CourtDetails, opening_tuesday_CourtDetails, opening_wednesday_CourtDetails, opening_thursday_CourtDetails,
            opening_friday_CourtDetails, opening_saturday_CourtDetails, opening_sunday_CourtDetails;
    private TextView text_contact_phone, text_contact_phone_title;
    private TextView addressTextView;
    public CourtDetailsFragment() {
        // Required empty public constructor
    }

    public static CourtDetailsFragment newInstance(String courtName, String courtAddress) {
        CourtDetailsFragment fragment = new CourtDetailsFragment();
        Bundle args = new Bundle();
        args.putString(COURT_NAME, courtName);
        args.putString(COURT_ADDRESS, courtAddress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            courtName = getArguments().getString(COURT_NAME);
            courtAddress = getArguments().getString(COURT_ADDRESS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_court_details, container, false);

        //Bundle args = getArguments();


        bundle = this.getArguments();
        TextView nameTextView = view.findViewById(R.id.textDetailsCourtName);
        addressTextView = view.findViewById(R.id.textDetailsCourtAddress);

        imageViewBar = view.findViewById(R.id.imageBarCourtDetails);
        imageViewRestroom = view.findViewById(R.id.imageRestroomCourtDetails);
        imageViewWifi = view.findViewById(R.id.imageWifiCourtDetails);

        buttonRentCourt = view.findViewById(R.id.buttonRentCourt);

        opening_monday_CourtDetails = view.findViewById(R.id.opening_monday_CourtDetails);
        opening_tuesday_CourtDetails = view.findViewById(R.id.opening_tuesday_CourtDetails);
        opening_wednesday_CourtDetails = view.findViewById(R.id.opening_wednesday_CourtDetails);
        opening_thursday_CourtDetails = view.findViewById(R.id.opening_thursday_CourtDetails);
        opening_friday_CourtDetails = view.findViewById(R.id.opening_friday_CourtDetails);
        opening_saturday_CourtDetails = view.findViewById(R.id.opening_saturday_CourtDetails);
        opening_sunday_CourtDetails = view.findViewById(R.id.opening_sunday_CourtDetails);

        text_contact_phone = view.findViewById(R.id.contact_phone_CourtDetails);
        text_contact_phone_title = view.findViewById(R.id.contact_phone_title);
        imageHolder = view.findViewById(R.id.imageHolderCourtDetails);

        if(bundle != null){
            nameTextView.setText(bundle.getString("courtName"));
            addressTextView.setText(bundle.getString("courtAddress"));
        }

        carouselView = view.findViewById(R.id.carouselView);
        //carouselView.setPageCount(sampleImages.length);
        //carouselView.setImageListener(imageListener);

        courtsRef = FirebaseConfig.getFirebaseDatabase().child("courts");
        storageRef = FirebaseConfig.getFirebaseStorage();

        if(VariablesForUsage.userTypeVariable == 0) buttonRentCourt.setVisibility(View.VISIBLE);

        getCourtsFromFirebase();

        return view;
    }

    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            imageView.setImageBitmap(courtImages.get(position));
        }
    };

    public void getCourtsFromFirebase(){

            String courtName = bundle.getString("courtName");
            valueEventListenerCourts = courtsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for(DataSnapshot data: snapshot.getChildren()) {

                        Court court = data.getValue(Court.class);
                        assert court != null;
                        if(court.getName() != null) {
                        if (court.getName().equals(courtName)) {

                           addressTextView.setText(court.getAddress());

                            if (court.getAdditionalFeatures() != null) {

                                for (int position = 0; position < court.getAdditionalFeatures().size(); position++) {
                                    if (court.getAdditionalFeatures().get(position).equals(AdditionalFeature.WIFI))
                                        imageViewWifi.setVisibility(View.VISIBLE);
                                    else if (court.getAdditionalFeatures().get(position).equals(AdditionalFeature.RESTROOM))
                                        imageViewRestroom.setVisibility(View.VISIBLE);
                                    else if (court.getAdditionalFeatures().get(position).equals(AdditionalFeature.BAR))
                                        imageViewBar.setVisibility(View.VISIBLE);
                                }
                            }

                            if (!court.getOpeningHours().getSunday().equals(defaultOpening)) {
                                opening_sunday_CourtDetails.setVisibility(View.VISIBLE);
                                String openingHoursString = getString(R.string.sun) + " " +
                                        court.getOpeningHours().getSunday().get(0) + " " +
                                        getString(R.string.to) + " " +
                                        court.getOpeningHours().getSunday().get(1);
                                opening_sunday_CourtDetails.setText(openingHoursString);
                            }

                            if (!court.getOpeningHours().getMonday().equals(defaultOpening)) {
                                opening_monday_CourtDetails.setVisibility(View.VISIBLE);
                                String openingHoursString = getString(R.string.mon) + " " +
                                        court.getOpeningHours().getMonday().get(0) + " " +
                                        getString(R.string.to) + " " +
                                        court.getOpeningHours().getMonday().get(1);
                                opening_monday_CourtDetails.setText(openingHoursString);
                            }

                            if (!court.getOpeningHours().getTuesday().equals(defaultOpening)) {
                                opening_tuesday_CourtDetails.setVisibility(View.VISIBLE);
                                String openingHoursString = getString(R.string.tu) + " " +
                                        court.getOpeningHours().getTuesday().get(0) + " " +
                                        getString(R.string.to) + " " +
                                        court.getOpeningHours().getTuesday().get(1);
                                opening_tuesday_CourtDetails.setText(openingHoursString);
                            }

                            if (!court.getOpeningHours().getWednesday().equals(defaultOpening)) {
                                opening_wednesday_CourtDetails.setVisibility(View.VISIBLE);
                                String openingHoursString = getString(R.string.wed) + " " +
                                        court.getOpeningHours().getWednesday().get(0) + " " +
                                        getString(R.string.to) + " " +
                                        court.getOpeningHours().getWednesday().get(1);
                                opening_wednesday_CourtDetails.setText(openingHoursString);
                            }

                            if (!court.getOpeningHours().getThursday().equals(defaultOpening)) {
                                opening_thursday_CourtDetails.setVisibility(View.VISIBLE);
                                String openingHoursString = getString(R.string.th) + " " +
                                        court.getOpeningHours().getThursday().get(0) + " " +
                                        getString(R.string.to) + " " +
                                        court.getOpeningHours().getThursday().get(1);
                                opening_thursday_CourtDetails.setText(openingHoursString);
                            }

                            if (!court.getOpeningHours().getFriday().equals(defaultOpening)) {
                                opening_friday_CourtDetails.setVisibility(View.VISIBLE);
                                String openingHoursString = getString(R.string.fri) + " " +
                                        court.getOpeningHours().getFriday().get(0) + " " +
                                        getString(R.string.to) + " " +
                                        court.getOpeningHours().getFriday().get(1);
                                opening_friday_CourtDetails.setText(openingHoursString);
                            }

                            if (!court.getOpeningHours().getSaturday().equals(defaultOpening)) {
                                opening_saturday_CourtDetails.setVisibility(View.VISIBLE);
                                String openingHoursString = getString(R.string.sat) + " " +
                                        court.getOpeningHours().getSaturday().get(0) + " " +
                                        getString(R.string.to) + " " +
                                        court.getOpeningHours().getSaturday().get(1);
                                opening_saturday_CourtDetails.setText(openingHoursString);
                            }

                            if (court.getContact_phone() != null && !court.getContact_phone().equals("")) {
                                text_contact_phone.setVisibility(View.VISIBLE);
                                text_contact_phone_title.setVisibility(View.VISIBLE);
                                text_contact_phone.setText(court.getContact_phone());
                            }


                            String courtId = Base64Custom.encodeBase64(courtName) + "/";

                            for (int imageCounter = 0; imageCounter < court.getImages().size(); imageCounter++) {
                                imageRefs.add(storageRef.child("imagens/courts/" + courtId + "image" + imageCounter));
                            }

                            try {
                                for (int imageCounter = 0; imageCounter < court.getImages().size(); imageCounter++) {

                                    final File localFile = File.createTempFile("courtImage", "jpg");
                                    imageController++;
                                    imageRefs.get(imageCounter).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                            courtImages.add(bitmap);

                                            if (imageController == (court.getImages().size() - 1)) {

                                                carouselView.setImageListener(imageListener);
                                                carouselView.setPageCount(courtImages.size());
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getContext(), R.string.error_on_image_upload, Toast.LENGTH_LONG).show();

                                        }
                                    });

                                }

                            } catch (IOException e) {
                                throw new RuntimeException(e);
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

    @Override
    public void onStop() {
        super.onStop();
        courtsRef.removeEventListener(valueEventListenerCourts);
    }

}