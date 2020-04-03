package io.wanderingthinkter.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import io.wanderingthinkter.R;
import io.wanderingthinkter.models.BillItem;
import io.wanderingthinkter.models.BillModel;
import io.wanderingthinkter.models.CurrentUser;
import io.wanderingthinkter.ui.BillItemRecyclerViewAdapter;

import static android.view.Gravity.RIGHT;
import static io.wanderingthinkter.util.Constants.BILL_COLLECTION;

public class CreateBillActivity extends AppCompatActivity implements View.OnClickListener {

    private List<BillItem> billItemList;
    private Button addItemButton, saveButton, cancelButton;
    private RecyclerView recyclerView;
    private BillItemRecyclerViewAdapter adapter;
    private TextView billTotal, itemCount;
    private TextView dateET;
    private EditText shopName;
    private Timestamp billDate;
    private ProgressBar progressBar;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(BILL_COLLECTION);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAnimation();
        setContentView(R.layout.activity_create_bill);

        billItemList = new ArrayList<>();

        dateET = findViewById(R.id.create_bill_date);
        cancelButton = findViewById(R.id.create_bill_cancel_button);
        progressBar = findViewById(R.id.create_bill_progressbar);
        shopName = findViewById(R.id.create_bill_shop_name);
        billTotal = findViewById(R.id.create_bill_total);
        itemCount = findViewById(R.id.create_bill_item_count);
        addItemButton = findViewById(R.id.create_bill_add_item);
        recyclerView = findViewById(R.id.create_bill_recycler_view);
        saveButton = findViewById(R.id.create_bill_save_button);
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
        dateET.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        init();
    }

    private void init() {
        updateBillTotal();
        updateBillItemCount();
        setDate(new Date());
    }

    private void setDate(Date date) {
        billDate = new Timestamp(date);
        dateET.setText(DateFormat.getDateInstance(DateFormat.LONG, Locale.US).format(date));
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
            case R.id.create_bill_date:
                setDateModal();
                break;
            case R.id.create_bill_save_button:
                saveBill();
                break;
            case R.id.create_bill_cancel_button:
                goToHomePage();
                break;
        }
    }

    private void goToHomePage() {
        Intent intent = new Intent(CreateBillActivity.this, HomeActivity.class);

        ActivityOptions options =
                ActivityOptions.makeSceneTransitionAnimation(CreateBillActivity.this);
        startActivity(intent,options.toBundle());
        finish();
    }

    private void saveBill() {
        String billTitle = shopName.getText().toString().trim();
        Double total = Double.parseDouble(billTotal.getText().toString().trim());
        Integer count = billItemList.size();

        if(!TextUtils.isEmpty(billTitle) && billItemList != null &&billItemList.size() > 0) {
            progressBar.setVisibility(View.VISIBLE);
            BillModel billModel = new BillModel(billTitle,
                    billDate,
                    total,
                    count,
                    CurrentUser.getInstance().getUserId(),
                    billItemList);

            collectionReference
                    .add(billModel)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            goToHomePage();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(findViewById(R.id.create_bill_activity),
                                    R.string.error_message, Snackbar.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });

        } else {
            Snackbar.make(findViewById(R.id.create_bill_activity),
                    R.string.all_fields_are_mandatory, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void setDateModal() {
        View view = getLayoutInflater().inflate(R.layout.calendar_modal, null);
        CalendarView calendarView = view.findViewById(R.id.calendar_modal_calendar);
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateBillActivity.this);
        builder.setView(view);
        dialog = builder.create();
        dialog.show();
        calendarView.setMaxDate(System.currentTimeMillis());
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                setDate(calendar.getTime());
                dialog.dismiss();
            }
        });
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

    // TODO: Create auth listner for change of user.
}
