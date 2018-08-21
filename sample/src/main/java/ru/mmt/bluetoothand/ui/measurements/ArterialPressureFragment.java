package ru.mmt.bluetoothand.ui.measurements;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TabHost;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.mmt.bluetoothand.R;
import ru.mmt.bluetoothand.utils.ArterialPressure;
import ru.mmt.bluetoothand.utils.Constants;
import ru.mmt.bluetoothand.utils.MeasurementsResultContainer;


/**
 * Fragment for displaying user arterial pressure
 * {@link ArterialPressureFragment.OnArterialPressureListener} interface
 * to handle interaction events.

 */
public class ArterialPressureFragment extends Fragment implements MeasurementsResultContainer.Listener {

    public static final String TAG = ArterialPressureFragment.class.getName();
    private static final String EXTRA_CURRENT_TAB = "extra_current_tab";
    private FloatingActionButton floatingActionButton;
    private ScrollView arterialPressureScrollView;
    private LineChart arterialPressureGraphView;
    private LineChart pulseGraphView;
    private OnArterialPressureListener listener;
    private TabHost mTabs;
    private IAxisValueFormatter axisDateValueFormatter = new IAxisValueFormatter() {
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            long longValue = (long) value;
            return Constants.DATE_FORMAT.format(longValue);
        }
    };
    private IAxisValueFormatter axisValueFormatter = new IAxisValueFormatter() {
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            long longValue = (long) value;
            return String.valueOf(longValue);
        }
    };
    private IValueFormatter valueFormatter = new IValueFormatter() {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            long longValue = (long) value;
            return String.valueOf(longValue);
        }
    };

    public ArterialPressureFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_arterial_pressure, container, false);
        floatingActionButton = view.findViewById(R.id.fragment_arterial_pressure_fab);
        arterialPressureScrollView = view.findViewById(R.id.fragment_arterial_pressure_scroll_view);
        arterialPressureGraphView = view.findViewById(R.id.fragment_arterial_pressure_graph);
        pulseGraphView = view.findViewById(R.id.fragment_pulse_graph);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onArterialPressureFabClicked();
            }
        });
        mTabs = view.findViewById(android.R.id.tabhost);
        setUpTabs(savedInstanceState);
        setUpGraph();
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnArterialPressureListener) {
            listener = (OnArterialPressureListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnArterialPressureListener");
        }
        MeasurementsResultContainer.getInstance().attachObserver(this);
    }



    private void setUpGraph() {
        //todo this list contains static data, but in the real application this list contains real data that are loaded from server
        List<ArterialPressure> pressureList = MeasurementsResultContainer.getInstance().getArterialPressures();
        List<Entry> systolicPressure = new ArrayList<>();
        List<Entry> diastolicPressure = new ArrayList<>();
        List<Entry> pulse = new ArrayList<>();
        for (ArterialPressure data : pressureList) {

            Date date;
            try {
                date = Constants.DATE_FORMAT_FULL.parse(data.updatedAt);
            } catch (ParseException e) {
                continue;
            }
            systolicPressure.add(new Entry(date.getTime(), data.upperPressure));
            diastolicPressure.add(new Entry(date.getTime(), data.lowerPressure));
            pulse.add(new Entry(date.getTime(), data.pulse));
        }
        if (!systolicPressure.isEmpty()) {
            LineDataSet systolicDataSet = new LineDataSet(systolicPressure, getString(R.string.systolic_pressure));
            LineDataSet diastolicDataSet = new LineDataSet(diastolicPressure, getString(R.string.diastolic_pressure));
            LineDataSet pulseDataSet = new LineDataSet(pulse, getString(R.string.pulse));// add entries to dataset
            systolicDataSet.setColor(ContextCompat.getColor(getContext(), R.color.graph_red));
            diastolicDataSet.setColor(ContextCompat.getColor(getContext(), R.color.graph_blue));
            LineData arterialPressureData = new LineData(systolicDataSet, diastolicDataSet);
            arterialPressureData.setValueFormatter(valueFormatter);
            arterialPressureGraphView.setData(arterialPressureData);
            LineData pulseData = new LineData(pulseDataSet);
            pulseGraphView.setData(pulseData);
            pulseData.setValueFormatter(valueFormatter);
        }
        arterialPressureGraphView.getXAxis().setValueFormatter(axisDateValueFormatter);
        arterialPressureGraphView.getAxisLeft().setValueFormatter(axisValueFormatter);
        arterialPressureGraphView.getAxisRight().setValueFormatter(axisValueFormatter);
        pulseGraphView.getXAxis().setValueFormatter(axisDateValueFormatter);
        pulseGraphView.getAxisLeft().setValueFormatter(axisValueFormatter);
        pulseGraphView.getAxisRight().setValueFormatter(axisValueFormatter);
        arterialPressureGraphView.getXAxis().setAxisMaximum(System.currentTimeMillis());
        pulseGraphView.getXAxis().setAxisMaximum(System.currentTimeMillis());

        Description emptyDescription = new Description();
        emptyDescription.setText("");

        arterialPressureGraphView.setNoDataText(getString(R.string.no_data));
        arterialPressureGraphView.setDescription(emptyDescription);
        arterialPressureGraphView.setTouchEnabled(false);

        pulseGraphView.setNoDataText(getString(R.string.no_data));
        pulseGraphView.setDescription(emptyDescription);
        pulseGraphView.setTouchEnabled(false);

        arterialPressureGraphView.invalidate();
        pulseGraphView.invalidate();
    }

    private void setUpTabs(Bundle bundle) {
        mTabs.setup();
        for (Tab tab : Tab.values())
            setUpTab(tab);
        if (bundle != null) {
            mTabs.setCurrentTab(bundle.getInt(EXTRA_CURRENT_TAB));
        } else {
            mTabs.setCurrentTab(Tab.ARTERIAL_PRESSURE.ID);
        }
    }

    private void setUpTab(Tab tab) {
        TabHost.TabSpec spec = mTabs.newTabSpec(tab.tag);
        spec.setContent(tab.contentId);
        spec.setIndicator(getString(tab.header));
        mTabs.addTab(spec);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MeasurementsResultContainer.getInstance().removeObserver(this);
        listener = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mTabs != null) {
            outState.putInt(EXTRA_CURRENT_TAB, mTabs.getCurrentTab());
        }
    }

    @Override
    public void onDataChanged() {
        setUpGraph();
    }

    private enum Tab {


        ARTERIAL_PRESSURE("arterial_pressure_tab", 0, R.id.fragment_arterial_pressure_tab, R.string.arterial_pressure),
        PULSE("pulse_tab", 1, R.id.fragment_pulse_tab, R.string.pulse);

        final String tag;
        final int ID;
        final int contentId;
        final int header;

        Tab(String tag, int id, int contentId, int header) {
            this.tag = tag;
            this.ID = id;
            this.contentId = contentId;
            this.header = header;
        }

        public static Tab findByTag(String tag) {
            for (Tab tab : Tab.values())
                if (tab.tag.equals(tag)) {
                    return tab;
                }
            return null;
        }


    }


    public interface OnArterialPressureListener {

        void onArterialPressureFabClicked();
    }
}
