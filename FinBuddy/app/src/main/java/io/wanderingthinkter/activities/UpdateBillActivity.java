package io.wanderingthinkter.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Slide;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import io.wanderingthinkter.R;
import io.wanderingthinkter.models.BillItem;
import io.wanderingthinkter.models.BillModel;
import io.wanderingthinkter.models.CurrentUser;
import io.wanderingthinkter.ui.BillItemRecyclerViewAdapter;

import static android.view.Gravity.RIGHT;
import static io.wanderingthinkter.R.string.item_count_txt;
import static io.wanderingthinkter.util.Constants.BILL_COLLECTION;
import static io.wanderingthinkter.util.Constants.KEY_BILL_ID;
import static io.wanderingthinkter.util.Constants.KEY_USER_ID;

public class UpdateBillActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(BILL_COLLECTION);
    private BillModel model;
    private String billId;
    private EditText billTitle, billCategory;
    private TextView billDate, totalPrice, totalCount;
    private RecyclerView recyclerView;
    private BillItemRecyclerViewAdapter adapter;
    private ProgressBar progressBar;
    private Double total = 0D;
    private Timestamp billTimestamp;
    private String documentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAnimation();
        setContentView(R.layout.activity_update_bill);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        billId = bundle.getString(KEY_BILL_ID);

        billTitle = findViewById(R.id.update_bill_shop_name);
        billCategory = findViewById(R.id.update_bill_category);
        billDate = findViewById(R.id.update_bill_date);
        totalPrice = findViewById(R.id.update_bill_total);
        totalCount = findViewById(R.id.update_bill_item_count);
        progressBar = findViewById(R.id.update_bill_progressbar);
        Button cancelButton = findViewById(R.id.update_bill_cancel_button);
        Button addItem = findViewById(R.id.update_bill_add_item);
        Button updateButton = findViewById(R.id.update_bill_save_button);
        cancelButton.setOnClickListener(this);
        addItem.setOnClickListener(this);
        updateButton.setOnClickListener(this);
        billDate.setOnClickListener(this);
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
    protected void onStart() {
        super.onStart();
        collectionReference
                .whereEqualTo(KEY_BILL_ID, billId)
                .whereEqualTo(KEY_USER_ID, CurrentUser.getInstance().getUserId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        documentID = snapshot.getId();
                        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
                        model = snapshot.toObject(BillModel.class);
                        billTitle.setText(model.getBillName());
                        billCategory.setText(model.getCategory());
                        billDate.setText(df.format(model.getBillDate().toDate()));
                        setTotalPriceAndCount();
                        recyclerView = findViewById(R.id.update_bill_recycler_view);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        adapter = new BillItemRecyclerViewAdapter(UpdateBillActivity.this, model.getItems());
                        adapter.setOnItemRemoved(() -> setTotalPriceAndCount());
                        recyclerView.setAdapter(adapter);
                        // TODO: Set adapter
                    }
                })
                .addOnFailureListener(e -> {

                });
    }

    @SuppressLint("DefaultLocale")
    private void setTotalPriceAndCount() {
        DecimalFormat df = new DecimalFormat("0.00");
        AtomicReference<Double> price = new AtomicReference<>(0D);
        model.getItems().forEach(item -> price.updateAndGet(v -> v + item.getPrice()));
        totalPrice.setText(df.format(price.get()));
        totalCount.setText(String.format("%s%d", getString(item_count_txt), model.getItems().size()));
        total = price.get();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.update_bill_cancel_button:
                finish();
                break;
            case R.id.update_bill_add_item:
                addItem();
                break;
            case R.id.update_bill_save_button:
                saveBill();
                break;
            case R.id.update_bill_date:
                setDateModal();
        }
    }

    private void addItem() {
        @SuppressLint("InflateParams")
        final View view = getLayoutInflater().inflate(R.layout.add_item_modal, null);
        final EditText itemTitleEt = view.findViewById(R.id.add_item_name_et);
        final EditText itemQtyEt = view.findViewById(R.id.add_item_qty_et);
        final EditText itemPriceEt = view.findViewById(R.id.add_item_price_et);
        Button saveButton = view.findViewById(R.id.add_item_save_button);
        Button cancelButton = view.findViewById(R.id.add_item_cancel_button);
        final AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateBillActivity.this);
        builder.setView(view);
        dialog = builder.create();
        dialog.show();

        saveButton.setOnClickListener(buttonView -> {
            String itemTitle = itemTitleEt.getText().toString().trim();
            String itemQty = itemQtyEt.getText().toString().trim();
            String itemPrice = itemPriceEt.getText().toString().trim();
            if(!TextUtils.isEmpty(itemTitle) && !TextUtils.isEmpty(itemQty) &&
                    !TextUtils.isEmpty(itemPrice)) {
                BillItem billItem = new BillItem(itemTitle, Integer.parseInt(itemQty), Double.parseDouble(itemPrice));
                model.getItems().add(billItem);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
                setTotalPriceAndCount();
            } else {
                Toast.makeText(UpdateBillActivity.this, R.string.all_fields_are_mandatory, Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(buttonView -> dialog.dismiss());
    }

    private void saveBill() {
        String billName = billTitle.getText().toString().trim();
        String category = billCategory.getText().toString().trim().toLowerCase();
        Integer count = model.getItems().size();

        if(!TextUtils.isEmpty(billName) && model.getItems() != null && model.getItems().size() > 0) {
            progressBar.setVisibility(View.VISIBLE);
            model.setBillName(billName);
            model.setCategory(category);
            model.setItemCount(count);
            model.setBillDate(billTimestamp);

            collectionReference
                    .document(documentID)
                    .set(model)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            finish();
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
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.calendar_modal, null);
        CalendarView calendarView = view.findViewById(R.id.calendar_modal_calendar);
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateBillActivity.this);
        builder.setView(view);
        dialog = builder.create();
        dialog.show();
        calendarView.setMaxDate(System.currentTimeMillis());
        calendarView.setOnDateChangeListener((calendarView1, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            setDate(calendar.getTime());
            dialog.dismiss();
        });
    }
    private void setDate(Date date) {
        billTimestamp = new Timestamp(date);
        billDate.setText(DateFormat.getDateInstance(DateFormat.LONG, Locale.US).format(date));
    }
}
