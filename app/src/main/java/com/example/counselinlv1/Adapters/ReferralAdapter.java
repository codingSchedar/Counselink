package com.example.counselinlv1.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.counselinlv1.Models.Referral;
import com.example.counselinlv1.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReferralAdapter extends RecyclerView.Adapter<ReferralAdapter.ReferralViewHolder> {

    private List<Referral> referralList;
    private Context context;
    private OnReferralClickListener listener;

    public interface OnReferralClickListener {
        void onReferralClick(Referral referral);
    }

    public ReferralAdapter(List<Referral> referralList, Context context, OnReferralClickListener listener) {
        this.referralList = referralList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReferralViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_referral, parent, false);
        return new ReferralViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReferralViewHolder holder, int position) {
        try {
            Referral referral = referralList.get(position);

            // Bind data to views with null checks
            holder.referredName.setText(referral.getStudentName() != null ? referral.getStudentName() : "Unknown Student");
            holder.referredBy.setText("Referred by: " + (referral.getReferrerID() != null ? referral.getReferrerID() : "Unknown"));
            holder.referralDate.setText("Referral Date: " + (referral.getCreatedAt() > 0 ? formatDate(referral.getCreatedAt()) : "Unknown"));
            holder.statusText.setText("Status: " + (referral.getStatus() != null ? referral.getStatus() : "Unknown"));
            holder.reasonText.setText("Concern: " + (referral.getAcademicReason() != null ? referral.getAcademicReason() : "N/A") + ", "+(referral.getSocialEmotionalReason() != null ? referral.getSocialEmotionalReason() : "N/A"));

            // Set background color for the statusText based on the referral status
            if (referral.getStatus() != null) {
                switch (referral.getStatus().toLowerCase()) {
                    case "pending":
                        holder.statusText.setBackgroundColor(context.getResources().getColor(R.color.light_yellow)); // Light Yellow
                        break;
                    case "processing":
                        holder.statusText.setBackgroundColor(context.getResources().getColor(R.color.light_orange)); // Light Orange
                        break;
                    case "counseled":
                        holder.statusText.setBackgroundColor(context.getResources().getColor(R.color.light_blue)); // Light Blue
                        break;
                    case "follow-up":
                        holder.statusText.setBackgroundColor(context.getResources().getColor(R.color.light_purple)); // Light Purple
                        break;
                    case "done":
                        holder.statusText.setBackgroundColor(context.getResources().getColor(R.color.light_green)); // Light Green
                        break;
                    default:
                        holder.statusText.setBackgroundColor(context.getResources().getColor(R.color.light_gray)); // Light Gray for unknown status
                }
            }

            // Set click listener for the item
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReferralClick(referral);
                }
            });
        } catch (Exception e) {
            Log.e("ReferralAdapter", "Error binding referral data: ", e);
        }
    }

    @Override
    public long getItemId(int position) {
        return referralList.get(position).getId().hashCode();
    }


    @Override
    public int getItemCount() {
        return referralList.size();
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static class ReferralViewHolder extends RecyclerView.ViewHolder {

        TextView referredName, referredBy, referralDate, statusText, reasonText;

        public ReferralViewHolder(@NonNull View itemView) {
            super(itemView);
            referredName = itemView.findViewById(R.id.referred_name);
            referredBy = itemView.findViewById(R.id.referred_by);
            referralDate = itemView.findViewById(R.id.referral_date);
            statusText = itemView.findViewById(R.id.referral_status);
            reasonText = itemView.findViewById(R.id.reason_text);
        }
    }
}
