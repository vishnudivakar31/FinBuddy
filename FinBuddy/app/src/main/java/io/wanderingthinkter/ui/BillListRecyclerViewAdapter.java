package io.wanderingthinkter.ui;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import io.wanderingthinkter.R;
import io.wanderingthinkter.activities.HomeActivity;
import io.wanderingthinkter.activities.LoginActivity;
import io.wanderingthinkter.activities.UpdateBillActivity;
import io.wanderingthinkter.models.BillModel;

import static io.wanderingthinkter.util.Constants.KEY_BILL_ID;

public class BillListRecyclerViewAdapter extends RecyclerView.Adapter<BillListRecyclerViewAdapter.ViewHolder> {

    private static List<BillModel> modelList;

    public BillListRecyclerViewAdapter(List<BillModel> modelList) {
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bill_fragment_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillModel billModel = modelList.get(position);
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
        holder.itemTitle.setText(String.format("%s%s",
                billModel.getBillName().substring(0, 1).toUpperCase(), billModel.getBillName().substring(1)));
        holder.itemDate.setText(df.format(billModel.getBillDate().toDate()));
        holder.itemQty.setText(String.valueOf(billModel.getItemCount()));
        holder.itemPrice.setText(String.valueOf(billModel.getTotalBill()));
        holder.itemCategory.setText(String.format("%s%s",
                billModel.getCategory().substring(0, 1).toUpperCase(), billModel.getCategory().substring(1)));
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemTitle, itemQty, itemPrice, itemDate, itemCategory;
        CardView cardView;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.bill_fragment_item_title);
            itemDate = itemView.findViewById(R.id.bill_fragment_item_date);
            itemQty = itemView.findViewById(R.id.bill_fragment_qty);
            itemPrice = itemView.findViewById(R.id.bill_fragment_price);
            itemCategory = itemView.findViewById(R.id.bill_fragment_category);
            cardView = itemView.findViewById(R.id.bill_fragment_item);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), UpdateBillActivity.class);
                    intent.putExtra(KEY_BILL_ID, modelList.get(getPosition()).getId());
                    view.getContext().startActivity(intent);
                }
            });
        }
    }
}
