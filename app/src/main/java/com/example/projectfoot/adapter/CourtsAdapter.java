package com.example.projectfoot.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectfoot.R;
import com.example.projectfoot.model.Contract;
import com.example.projectfoot.model.Court;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoProvider;

import java.util.List;

public class CourtsAdapter extends RecyclerView.Adapter<CourtsAdapter.MyViewHolder> {
    private List<Court> courtList;
    private List<Contract> contractList;
    private Context context;
    private int type = 1;

    public CourtsAdapter(List<Court> courtsList, Context c) {
        this.courtList = courtsList;
        this.context = c;
    }
    public CourtsAdapter(List<Contract> contractsList, Context c, int type) {
        this.contractList = contractsList;
        this.context = c;
        this.type = type;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_courts, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        if(type == 0) {

            Contract contract = contractList.get(position);
            holder.courtName.setText(contract.getCourtOfTheContract());
            holder.courtAddress.setVisibility(View.GONE);
            holder.image.setVisibility(View.GONE);
        }

        else {

            Court court = courtList.get(position);
            holder.courtName.setText(court.getName());
            holder.courtAddress.setText(court.getAddress());

            //get first image of the list
            List<String> urlImages = court.getImages();

            if(urlImages != null) {

                String urlFirstImage = urlImages.get(0);
                Picasso.get().load(urlFirstImage).into(holder.image);
            }

        }

    }


    @Override
    public int getItemCount() {
        if(type == 0) return contractList.size();
        else return courtList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView courtName;
        TextView courtAddress;
        ImageView image;
        LinearLayout linearLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            courtName = itemView.findViewById(R.id.textDetailsCourtName);
            courtAddress = itemView.findViewById(R.id.textCourtAddress);
            image = itemView.findViewById(R.id.imageCourt);
        }
    }
}
