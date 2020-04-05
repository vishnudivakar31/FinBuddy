package io.wanderingthinkter.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.wanderingthinkter.R;
import io.wanderingthinkter.models.BillModel;
import io.wanderingthinkter.models.CurrentUser;
import io.wanderingthinkter.ui.BillListRecyclerViewAdapter;

import static io.wanderingthinkter.util.Constants.BILL_COLLECTION;
import static io.wanderingthinkter.util.Constants.KEY_BILL_DATE;
import static io.wanderingthinkter.util.Constants.KEY_USER_ID;

public class BillListFragment extends Fragment {

    private static final String FROM_DATE = "from_date";
    private static final String TO_DATE = "to_date";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(BILL_COLLECTION);
    private List<BillModel> billItemList;
    private Timestamp fromDate, toDate;
    private BillListRecyclerViewAdapter adapter;
    private TextView totalPrice, itemCount;
    private TextView toDateTV, fromDateTV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        billItemList = new ArrayList<>();
        collectionReference.addSnapshotListener((queryDocumentSnapshots, e) -> {
            assert queryDocumentSnapshots != null;
            if(e != null) {
                for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    BillModel billModel = dc.getDocument().toObject(BillModel.class);
                    switch (dc.getType()) {
                        case ADDED:
                            billItemList.add(billModel);
                            adapter.notifyDataSetChanged();
                            break;
                        case MODIFIED:
                            updateBillList(billModel);
                            break;
                        case REMOVED:
                            removeBillList(billModel);
                            break;
                    }
                }
            }
        });
    }

    private void removeBillList(BillModel billModel) {
        int position = findModelIndex(billModel);
        billItemList.remove(position);
        adapter.notifyItemRemoved(position);
    }

    private void updateBillList(BillModel billModel) {
        int position = findModelIndex(billModel);
        billItemList.set(position, billModel);
        adapter.notifyItemChanged(position, billModel);
    }

    private int findModelIndex(BillModel billModel) {
        for(int i = 0; i < billItemList.size(); i++) {
            if(billItemList.get(i).getId().equals(billModel.getId())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bill_list, container, false);

        fromDateTV = view.findViewById(R.id.overview_fragment_from_date);
        toDateTV = view.findViewById(R.id.overview_fragment_to);
        ImageView fromDateIV = view.findViewById(R.id.bill_list_fragment_calendar_1);
        ImageView toDateIV = view.findViewById(R.id.bill_list_fragment_calendar_2);
        RecyclerView recyclerView = view.findViewById(R.id.bill_list_fragment_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        totalPrice = view.findViewById(R.id.bill_list_fragment_total);
        itemCount = view.findViewById(R.id.bill_list_fragment_item_count);
        adapter = new BillListRecyclerViewAdapter(billItemList);
        recyclerView.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        fromDate = new Timestamp(calendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        toDate = new Timestamp(new Date(System.currentTimeMillis()));

        setFromAndToDate();

        fromDateIV.setOnClickListener(view1 -> showCalendarModal(FROM_DATE));

        toDateIV.setOnClickListener(view12 -> showCalendarModal(TO_DATE));

        return view;
    }

    private void setFromAndToDate() {
        fromDateTV.setText(getDateText(fromDate));
        toDateTV.setText(getDateText(toDate));
    }

    private void showCalendarModal(String dateType) {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        @SuppressLint("InflateParams") View calendarModal = LayoutInflater.from(getActivity()).inflate(R.layout.calendar_modal, null);
        CalendarView calendarView = calendarModal.findViewById(R.id.calendar_modal_calendar);
        calendarView.setMaxDate(System.currentTimeMillis());
        builder.setView(calendarModal);
        dialog = builder.create();
        dialog.show();

        calendarView.setOnDateChangeListener((calendarView1, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            if(dateType.equals(FROM_DATE)) {
                fromDate = new Timestamp(calendar.getTime());
            } else {
                toDate = new Timestamp(calendar.getTime());
            }
            billItemList.clear();
            setFromAndToDate();
            getBillItemsList(fromDate, toDate);
            dialog.dismiss();
        });
    }

    private String getDateText(Timestamp date) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
        return df.format(date.toDate());
    }

    @Override
    public void onStart() {
        super.onStart();
        billItemList.clear();
        getBillItemsList(fromDate, toDate);
    }

    private void getBillItemsList(Timestamp fromDate, Timestamp toDate) {
        collectionReference
                .whereEqualTo(KEY_USER_ID, CurrentUser.getInstance().getUserId())
                .whereGreaterThanOrEqualTo(KEY_BILL_DATE, fromDate)
                .whereLessThanOrEqualTo(KEY_BILL_DATE, toDate)
                .orderBy(KEY_BILL_DATE, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if(!queryDocumentSnapshots.isEmpty()) {
                        for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                            BillModel billModel = snapshot.toObject(BillModel.class);
                            billItemList.add(billModel);
                        }
                        DecimalFormat df = new DecimalFormat("0.00");
                        itemCount.setText(String.valueOf(billItemList.size()));
                        totalPrice.setText(String.valueOf(df.format(getTotalPrice(billItemList))));
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Snackbar.make(Objects.requireNonNull(getView()).findViewById(R.id.bill_list_fragment),
                        R.string.error_message, Snackbar.LENGTH_SHORT).show());
    }

    private Double getTotalPrice(List<BillModel> billItemList) {
        AtomicReference<Double> sum = new AtomicReference<>(0D);
        billItemList.forEach(item -> sum.updateAndGet(v -> v + item.getTotalBill()));
        return sum.get();
    }

}
