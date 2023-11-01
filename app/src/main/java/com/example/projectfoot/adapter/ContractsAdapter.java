package com.example.projectfoot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectfoot.R;
import com.example.projectfoot.model.Contract;

import java.util.List;

public class ContractsAdapter extends RecyclerView.Adapter<ContractsAdapter.MyViewHolder> {
    private List<Contract> contracts;
    private Context context;

    public ContractsAdapter(List<Contract> contractsList, Context c) {
        this.contracts = contractsList;
        this.context = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_courts, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Contract contract = contracts.get(position);
        holder.courOfTheContractName.setText(contract.getCourtOfTheContract());

    }

    @Override
    public int getItemCount() {
        return contracts.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView courOfTheContractName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            courOfTheContractName = itemView.findViewById(R.id.textDetailsCourtName);
        }
    }
}
