package io.wanderingthinkter.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import io.wanderingthinkter.R;
import io.wanderingthinkter.models.BillModel;

public class OverviewRecyclerAdapter extends RecyclerView.Adapter<OverviewRecyclerAdapter.ViewHolder> {

    private List<BillModel> billModelList;

    public OverviewRecyclerAdapter(List<BillModel> billModelList) {
        this.billModelList = billModelList;
    }

    @NonNull
    @Override
    public OverviewRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.overview_top_five_items, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OverviewRecyclerAdapter.ViewHolder holder, int position) {
        BillModel billModel = billModelList.get(position);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
        holder.billTitle.setText(billModel.getBillName());
        holder.billCategory.setText(billModel.getCategory());
        holder.billPrice.setText(String.valueOf(billModel.getTotalBill()));
        holder.billDate.setText(dateFormat.format(billModel.getBillDate().toDate()));
    }

    @Override
    public int getItemCount() {
        return billModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView billTitle, billCategory, billPrice, billDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            billTitle = itemView.findViewById(R.id.overview_bill_title);
            billCategory = itemView.findViewById(R.id.overview_bill_category);
            billPrice = itemView.findViewById(R.id.overview_bill_price);
            billDate = itemView.findViewById(R.id.overview_bill_date);
        }
    }
}
