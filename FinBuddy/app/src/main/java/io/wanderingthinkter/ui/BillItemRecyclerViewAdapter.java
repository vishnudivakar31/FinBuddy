package io.wanderingthinkter.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

import io.wanderingthinkter.R;
import io.wanderingthinkter.models.BillItem;

public class BillItemRecyclerViewAdapter extends RecyclerView.Adapter<BillItemRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<BillItem> billItemList;
    private OnItemRemoved onItemRemoved;

    public void setOnItemRemoved(OnItemRemoved onItemRemoved) {
        this.onItemRemoved = onItemRemoved;
    }

    public BillItemRecyclerViewAdapter(Context context, List<BillItem> billItemList) {
        this.context = context;
        this.billItemList = billItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.create_bill_item, null);
        return new ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        BillItem billItem = billItemList.get(position);
        holder.serialTV.setText(String.format("%d. ", position + 1));
        holder.titleTV.setText(billItem.getItemName());
        holder.qtyTV.setText(String.format("%s%d", context.getString(R.string.qty_txt), billItem.getQty()));
        holder.priceTV.setText(String.format("%s%s", context.getString(R.string.price_disp_txt), decimalFormat.format(billItem.getPrice())));
    }

    @Override
    public int getItemCount() {
        return billItemList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView serialTV, titleTV, qtyTV, priceTV;
        ImageView deleteItem;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            serialTV = itemView.findViewById(R.id.bill_item_serial_number);
            titleTV = itemView.findViewById(R.id.bill_item_title);
            qtyTV = itemView.findViewById(R.id.bill_item_qty);
            priceTV = itemView.findViewById(R.id.bill_item_price);
            deleteItem = itemView.findViewById(R.id.bill_item_delete_button);
            deleteItem.setOnClickListener(view -> {
                // TODO: Delete item
                int itemPosition = getAdapterPosition();
                billItemList.remove(itemPosition);
                notifyItemRemoved(itemPosition);
                notifyItemRangeChanged(itemPosition, billItemList.size());
                onItemRemoved.itemRemovedEvent();
            });
        }
    }

    public interface OnItemRemoved {
        void itemRemovedEvent();
    }
}
