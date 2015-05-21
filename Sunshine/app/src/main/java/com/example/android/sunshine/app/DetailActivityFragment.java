package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

import org.w3c.dom.Text;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private ShareActionProvider mShareActionProvider;
    private String dayForecast;
    private static final int DETAIL_LOADER=0;
    static final String DETAIL_URI = "URI";
    private Uri mUri;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 4;
    static final int COL_WEATHER_MIN_TEMP = 3;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_WIND_SPEED = 6;
    static final int COL_WEATHER_PRESSURE = 8;
    static final int COL_WEATHER_DEGRESS = 7;
    static final int COL_WEATHER_CONDITION_ID = 9;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    private TextView dayView;
    private TextView dateView;
    private TextView highView;
    private TextView lowView;
    private TextView humidityView;
    private TextView windView;
    private TextView pressureView;
    private TextView descriptionView;
    private ImageView iconView;


    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);


        MenuItem shareItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        if(dayForecast != null){
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        dayView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        dateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        highView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        lowView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        humidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        windView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        pressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        descriptionView = (TextView) rootView.findViewById(R.id.detail_description_textview);
        iconView = (ImageView) rootView.findViewById(R.id.detail_iconview);

        return rootView;

    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, dayForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    public void onLocationChanged(String newLocation){
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data){
        if (!data.moveToFirst()) { return; }

        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);

        int dateInMillis = data.getInt(COL_WEATHER_DATE);

        String weatherDescription = data.getString(COL_WEATHER_DESC);

        iconView.setContentDescription(weatherDescription);

        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);

        String low = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

        Float humidity = data.getFloat(COL_WEATHER_HUMIDITY);

        String wind = Utility.getFormattedWind(getActivity(), data.getFloat(COL_WEATHER_WIND_SPEED), data.getFloat(COL_WEATHER_DEGRESS));

        Float pressure = data.getFloat(COL_WEATHER_PRESSURE);

        /*dayForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

        TextView textForecast = (TextView) getActivity().findViewById(R.id.detail_dayForecast);
        textForecast.setText(dayForecast);
        */
        dayView.setText(Utility.getDayName(getActivity(), dateInMillis));
        dateView.setText(Utility.getFormattedMonthDay(getActivity(), dateInMillis));
        highView.setText(high);
        lowView.setText(low);
        humidityView.setText(String.format(getActivity().getString(R.string.format_humidity), humidity));
        windView.setText(wind);
        pressureView.setText(String.format(getActivity().getString(R.string.format_pressure), pressure));
        iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        descriptionView.setText(weatherDescription);

        if (mShareActionProvider != null){
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader){
    }
}
