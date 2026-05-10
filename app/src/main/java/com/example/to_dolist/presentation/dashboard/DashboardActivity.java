package com.example.to_dolist.presentation.dashboard;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import com.example.to_dolist.R;
import com.example.to_dolist.core.util.DateUtils;
import com.example.to_dolist.databinding.ActivityDashboardBinding;
import com.example.to_dolist.domain.model.Task;
import com.example.to_dolist.presentation.BaseActivity;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashboardActivity extends BaseActivity {

    private ActivityDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        DashboardViewModel vm = new ViewModelProvider(this).get(DashboardViewModel.class);

        vm.getStats().observe(this, stats -> {
            if (stats == null) return;
            binding.textOpenCount.setText(getString(R.string.stats_total_open) + ": " + stats.openTasks);
            binding.textCompletedWeek.setText(getString(R.string.dashboard_completed_week) + ": "
                    + stats.completedDueThisWeek);
            binding.textRate.setText(getString(R.string.dashboard_productivity) + ": "
                    + String.format(java.util.Locale.getDefault(), "%.0f%%", stats.completionRatePercent));

            if (stats.pieSlices == null || stats.pieSlices.isEmpty()) {
                binding.pieChart.clear();
                binding.pieChart.invalidate();
            } else {
                PieDataSet pieSet = new PieDataSet(stats.pieSlices, "");
                pieSet.setColors(ColorTemplate.MATERIAL_COLORS);
                pieSet.setValueTextSize(12f);
                binding.pieChart.setData(new PieData(pieSet));
                binding.pieChart.getDescription().setEnabled(false);
                binding.pieChart.getLegend().setEnabled(true);
                binding.pieChart.invalidate();
            }

            BarDataSet barSet = new BarDataSet(stats.upcomingByDay, getString(R.string.dashboard_bar_upcoming));
            barSet.setColors(ColorTemplate.COLORFUL_COLORS);
            BarData barData = new BarData(barSet);
            barData.setBarWidth(0.65f);
            binding.barChart.setData(barData);
            binding.barChart.getDescription().setEnabled(false);
            XAxis x = binding.barChart.getXAxis();
            x.setPosition(XAxis.XAxisPosition.BOTTOM);
            x.setGranularity(1f);
            x.setValueFormatter(new IndexAxisValueFormatter(stats.dayLabels));
            binding.barChart.invalidate();
        });

        vm.getUpcomingTasks().observe(this, this::renderUpcoming);
    }

    private void renderUpcoming(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            binding.textUpcomingList.setText(R.string.no_tasks);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Task t : tasks) {
            sb.append("• ");
            sb.append(t.getTitle() != null ? t.getTitle() : "");
            sb.append(" — ");
            sb.append(DateUtils.toFriendlyLabel(this, t.getDueDate()));
            sb.append('\n');
        }
        binding.textUpcomingList.setText(sb.toString());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
