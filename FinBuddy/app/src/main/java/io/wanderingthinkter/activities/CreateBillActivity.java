package io.wanderingthinkter.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.wanderingthinkter.R;
import io.wanderingthinkter.models.BillItem;
import io.wanderingthinkter.models.CurrentUser;
import io.wanderingthinkter.ui.BillItemRecyclerViewAdapter;

import static android.view.Gravity.RIGHT;

public class CreateBillActivity extends AppCompatActivity implements View.OnClickListener {

    private List<BillItem> billItemList;
    private Button addItemButton;
    private RecyclerView recyclerView;
    private BillItemRecyclerViewAdapter adapter;
    private TextView billTotal, itemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAnimation();
        setContentView(R.layout.activity_create_bill);

        billItemList = new ArrayList<>();

        billTotal = findViewById(R.id.create_bill_total);
        itemCount = findViewById(R.id.create_bill_item_count);
        addItemButton = findViewById(R.id.create_bill_add_item);
        recyclerView = findViewById(R.id.create_bill_recycler_view);
        adapter = new BillItemRecyclerViewAdapter(CreateBillActivity.this, billItemList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnItemRemoved(new BillItemRecyclerViewAdapter.OnItemRemoved() {
            @Override
            public void itemRemovedEvent() {
                updateBillItemCount();
                updateBillTotal();
            }
        });
        recyclerView.setAdapter(adapter);

        addItemButton.setOnClickListener(this);
        updateBillTotal();
        updateBillItemCount();

        CurrentUser user = CurrentUser.getInstance();
        Log.d("CurrUser", user.getUsername());
    }

    private void updateBillItemCount() {
        itemCount.setText(String.format("%s%s", getString(R.string.item_count_txt), String.valueOf(billItemList.size())));
    }

    @SuppressLint("RtlHardcoded")
    public void setAnimation() {
        Slide slide = new Slide();
        slide.setSlideEdge(RIGHT);
        slide.setDuration(400);
        slide.setInterpolator(new DecelerateInterpolator());
        getWindow().setExitTransition(slide);
        getWindow().setEnterTransition(slide);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_bill_add_item:
                addItem();
                break;
        }
    }

    private void addItem() {
        final View view = getLayoutInflater().inflate(R.layout.add_item_modal, null);
        final EditText itemTitleEt = view.findViewById(R.id.add_item_name_et);
        final EditText itemQtyEt = view.findViewById(R.id.add_item_qty_et);
        final EditText itemPriceEt = view.findViewById(R.id.add_item_price_et);
        Button saveButton = view.findViewById(R.id.add_item_save_button);
        Button cancelButton = view.findViewById(R.id.add_item_cancel_button);
        final AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateBillActivity.this);
        builder.setView(view);
        dialog = builder.create();
        dialog.show();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String itemTitle = itemTitleEt.getText().toString().trim();
                String itemQty = itemQtyEt.getText().toString().trim();
                String itemPrice = itemPriceEt.getText().toString().trim();
                if(!TextUtils.isEmpty(itemTitle) && !TextUtils.isEmpty(itemQty) &&
                        !TextUtils.isEmpty(itemPrice)) {
                    BillItem billItem = new BillItem(itemTitle, Integer.parseInt(itemQty), Double.parseDouble(itemPrice));
                    billItemList.add(billItem);
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                    updateBillTotal();
                    updateBillItemCount();
                } else {
                    Toast.makeText(CreateBillActivity.this, R.string.all_fields_are_mandatory, Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void updateBillTotal() {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        AtomicReference<Double> sum = new AtomicReference<>(0D);
        billItemList.stream().forEach(item -> sum.set(sum.get() + item.getPrice()));
        billTotal.setText(decimalFormat.format(sum.get()));
    }
}
