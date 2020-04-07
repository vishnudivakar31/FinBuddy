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

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.wanderingthinkter.R;
import io.wanderingthinkter.models.BillModel;
import io.wanderingthinkter.models.CurrentUser;
import io.wanderingthinkter.ui.OverviewRecyclerAdapter;

import static io.wanderingthinkter.util.Constants.BILL_COLLECTION;
import static io.wanderingthinkter.util.Constants.KEY_BILL_DATE;
import static io.wanderingthinkter.util.Constants.KEY_USER_ID;

public class OverviewFragment extends Fragment {

    private static final String FROM_DATE = "from_date";
    private static final String TO_DATE = "to_date";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(BILL_COLLECTION);
    private List<BillModel> billItemList;
    private TextView itemCount, totalPrice, fromDateTV, toDateTV;
    private Timestamp fromDate, toDate;
    private PieChart pieChart;
    private List<BillModel> topFiveExpenses;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        billItemList = new ArrayList<>();
        topFiveExpenses = new ArrayList<>();
        View view = inflater.inflate(R.layout.fragment_overview_fragment, container, false);
        recyclerView = view.findViewById(R.id.overview_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemCount = view.findViewById(R.id.overview_total_count);
        totalPrice = view.findViewById(R.id.overview_total);
        fromDateTV = view.findViewById(R.id.overview_fragment_from_date);
        toDateTV = view.findViewById(R.id.overview_fragment_to_date);
        pieChart = view.findViewById(R.id.overview_pie_chart);
        toDate = new Timestamp(new Date(System.currentTimeMillis()));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        fromDate = new Timestamp(calendar.getTime());

        ImageView fromDateIV = view.findViewById(R.id.bill_list_fragment_calendar_1);
        ImageView toDateIV = view.findViewById(R.id.bill_list_fragment_calendar_2);

        fromDateIV.setOnClickListener(view1 -> showCalendarModal(FROM_DATE));

        toDateIV.setOnClickListener(view12 -> showCalendarModal(TO_DATE));

        setFromAndToDate();
        return view;
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

    private void setFromAndToDate() {
        fromDateTV.setText(getDateText(fromDate));
        toDateTV.setText(getDateText(toDate));
    }

    private String getDateText(Timestamp date) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
        return df.format(date.toDate());
    }

    @Override
    public void onStart() {
        super.onStart();
        getBillItemsList(fromDate, toDate);
    }

    private void getBillItemsList(Timestamp fromDate, Timestamp toDate) {
        billItemList.clear();
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
                        Collections.sort(billItemList, Collections.reverseOrder());
                        DecimalFormat df = new DecimalFormat("0.00");
                        itemCount.setText(String.valueOf(billItemList.size()));
                        totalPrice.setText(String.valueOf(df.format(getTotalPrice(billItemList))));
                        setPieBarChart();
                        setTopFiveExpense();
                    }
                })
                .addOnFailureListener(e -> Snackbar.make(Objects.requireNonNull(getView()).findViewById(R.id.bill_overview_fragment),
                        R.string.error_message, Snackbar.LENGTH_SHORT).show());
    }

    private void setTopFiveExpense() {
        int size = Math.min(billItemList.size(), 5);
        topFiveExpenses = billItemList.subList(0, size);
        OverviewRecyclerAdapter adapter = new OverviewRecyclerAdapter(topFiveExpenses);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void setPieBarChart() {
        Map<String, Float> categoryDistribution = new HashMap<>();
        for(BillModel billModel : billItemList) {
            String category = billModel.getCategory().toLowerCase();
            if(categoryDistribution.containsKey(category)) {
                Float currentPrice = categoryDistribution.get(category);
                currentPrice += Float.parseFloat(String.valueOf(billModel.getTotalBill()));
                categoryDistribution.put(category, currentPrice);
            } else {
                categoryDistribution.put(category, Float.parseFloat(String.valueOf(billModel.getTotalBill())));
            }
        }
        setPieChart(categoryDistribution);
    }

    private void setPieChart(Map<String, Float> categoryDistribution) {
        List<PieEntry> distribution = new ArrayList<>();
        for(Map.Entry<String, Float> entry : categoryDistribution.entrySet()) {
            distribution.add(new PieEntry(entry.getValue(), entry.getKey()));
        }
        PieDataSet dataSet = new PieDataSet(distribution, getString(R.string.total_expense_distribution_txt));
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setCenterText(getString(R.string.category_distribution_txt));
        pieChart.animateXY(2000, 2000);
    }

    private Double getTotalPrice(List<BillModel> billItemList) {
        AtomicReference<Double> sum = new AtomicReference<>(0D);
        billItemList.forEach(item -> sum.updateAndGet(v -> v + item.getTotalBill()));
        return sum.get();
    }
}
