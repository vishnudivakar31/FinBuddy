package io.wanderingthinkter.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.wanderingthinkter.R;
import io.wanderingthinkter.models.BillItem;

public class BillItemRecyclerViewAdapter extends RecyclerView.Adapter<BillItemRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<BillItem> billItemList;

    public BillItemRecyclerViewAdapter(Context context, List<BillItem> billItemList) {
        this.context = context;
        this.billItemList = billItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.create_bill_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillItem billItem = billItemList.get(position);
        holder.serialTV.setText(String.format("%d. ", position + 1));
        holder.titleTV.setText(billItem.getItemName());
        holder.qtyTV.setText(String.valueOf(billItem.getQty()));
        holder.priceTV.setText(String.valueOf(billItem.getPrice()));
    }

    @Override
    public int getItemCount() {
        return billItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView serialTV, titleTV, qtyTV, priceTV;
        public ImageView deleteItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            serialTV = itemView.findViewById(R.id.bill_item_serial_number);
            titleTV = itemView.findViewById(R.id.bill_item_title);
            qtyTV = itemView.findViewById(R.id.bill_item_qty);
            priceTV = itemView.findViewById(R.id.bill_item_price);
            deleteItem = itemView.findViewById(R.id.bill_item_delete_button);
            deleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO: Delete item
                    int itemPosition = getAdapterPosition();
                    billItemList.remove(itemPosition);
                    notifyItemRemoved(itemPosition);
                    notifyItemRangeChanged(itemPosition, billItemList.size());
                }
            });
        }
    }
}
