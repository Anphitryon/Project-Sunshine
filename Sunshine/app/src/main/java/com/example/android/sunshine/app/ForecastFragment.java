package com.example.android.sunshine.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.service.SunshineService;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;
import com.example.android.sunshine.app.sync.SunshineSyncService;

import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private ForecastAdapter mForecastAdapter;
    private static final int MY_LOADER = 1;
    Callback callbackListener;
    private final String POSITION = "POS";
    private int savedPosition;
    private ListView listView;
    private boolean mUseTodayLayout;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 4;
    static final int COL_WEATHER_DATE = 2;
    static final int COL_WEATHER_DESC = 3;
    static final int COL_WEATHER_MAX_TEMP = 6;
    static final int COL_WEATHER_MIN_TEMP = 5;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    public ForecastFragment() {
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mForecastAdapter != null) {
                mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
            }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View baseView = inflater.inflate(R.layout.fragment_main, container, false);

        if(savedInstanceState != null){
            savedPosition = savedInstanceState.getInt(POSITION);
        }

        listView = (ListView) getActivity().findViewById(R.id.listview_forecast);

        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);
        mForecastAdapter = new ForecastAdapter(getActivity(), cur, 0);

        ListView listView = (ListView) baseView.findViewById(R.id.listview_forecast);

        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity())
                            .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                    ));
                    savedPosition = position;
                }
            }
        });

        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        if(savedInstanceState != null && savedInstanceState.containsKey(POSITION)){
            savedPosition = savedInstanceState.getInt(POSITION);
        }

        return baseView;
    }

    private void updateWeather() {
        /*
        Intent intent = new Intent(getActivity(), SunshineService.class);
        intent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, Utility.getPreferredLocation(getActivity()));
        getActivity().startService(intent);

        Intent i = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
        i.putExtra(SunshineService.LOCATION_QUERY_EXTRA, Utility.getPreferredLocation(getActivity()));
        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, i, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService((Context.ALARM_SERVICE));
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);
        */
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    public void onLocationChanged(){
        updateWeather();
        getLoaderManager().restartLoader(MY_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle){
        String locationSetting = Utility.getPreferredLocation(getActivity());

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                null,
                null,
                null,
                sortOrder);
    }

    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor){
        mForecastAdapter.swapCursor(cursor);

        if(savedPosition != ListView.INVALID_POSITION){
            listView.setSelection(savedPosition);
        }

    }

    public void onLoaderReset(Loader<Cursor> cursorLoader){
        mForecastAdapter.swapCursor(null);
    }

    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            callbackListener = (Callback) activity;
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString());
        }
    }

    public void onSavedInstanceState(Bundle outState){
        if(savedPosition != ListView.INVALID_POSITION){
            outState.putInt(POSITION, savedPosition);
        }
        super.onSaveInstanceState(outState);
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }


}
