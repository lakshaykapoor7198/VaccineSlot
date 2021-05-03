package com.time.vaccineslot.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.time.vaccineslot.R;
import com.time.vaccineslot.pojo.AvailableSlot;

import java.util.ArrayList;

public class SlotsAdapter extends RecyclerView.Adapter<SlotsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<AvailableSlot> availableSlots;

    public SlotsAdapter(Context context, ArrayList<AvailableSlot> availableSlots){
        this.context = context;
        this.availableSlots = availableSlots;
    }

    @NonNull
    @Override
    public SlotsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotsAdapter.ViewHolder holder, int position) {
        AvailableSlot availableSlot = availableSlots.get(position);
        holder.centerName.setText(availableSlot.getName());
        StringBuilder text = new StringBuilder();
        text.append(availableSlot.getAvailable_capacity());
        text.append(" dose");
        if (!availableSlot.getVaccine().equals("")){
            text.append(" of ");
            text.append(availableSlot.getVaccine());
        }
        holder.dose.setText(text.toString());
        holder.date.setText(availableSlot.getDate());
        holder.book.setOnClickListener((View view) -> {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("Redirecting to CoWin site")
                    .setPositiveButton("Ok", ((dialog, which) -> {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://selfregistration.cowin.gov.in/"));
                        context.startActivity(browserIntent);
                    }))
                    .setMessage( "Login, Add Member and Choose " + availableSlot.getName() + " as centre and " + availableSlot.getDate() + " as appointment date")
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return availableSlots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView centerName;
        private final TextView dose;
        private final TextView date;
        private final Button book;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            centerName = itemView.findViewById(R.id.centerName);
            dose = itemView.findViewById(R.id.Dose);
            date = itemView.findViewById(R.id.Date);
            book = itemView.findViewById(R.id.Book);
        }
    }

}
