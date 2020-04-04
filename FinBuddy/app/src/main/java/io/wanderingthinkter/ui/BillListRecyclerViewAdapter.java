package io.wanderingthinkter.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import io.wanderingthinkter.R;
import io.wanderingthinkter.models.BillModel;

public class BillListRecyclerViewAdapter extends RecyclerView.Adapter<BillListRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<BillModel> modelList;

    public BillListRecyclerViewAdapter(Context context, List<BillModel> modelList) {
        this.context = context;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bill_fragment_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillModel billModel = modelList.get(position);
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
        holder.itemTitle.setText(billModel.getBillName());
        holder.itemDate.setText(df.format(billModel.getBillDate().toDate()));
        holder.itemQty.setText(String.valueOf(billModel.getItemCount()));
        holder.itemPrice.setText(String.valueOf(billModel.getTotalBill()));
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView itemTitle, itemQty, itemPrice, itemDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.bill_fragment_item_title);
            itemDate = itemView.findViewById(R.id.bill_fragment_item_date);
            itemQty = itemView.findViewById(R.id.bill_fragment_qty);
            itemPrice = itemView.findViewById(R.id.bill_fragment_price);
        }
    }
}
