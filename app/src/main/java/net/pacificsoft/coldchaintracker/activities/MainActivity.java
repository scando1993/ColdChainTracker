package net.pacificsoft.coldchaintracker.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import net.pacificsoft.coldchaintracker.adapter.MessagesAdapter;
import net.pacificsoft.coldchaintracker.models.Message;
import net.pacificsoft.coldchaintracker.models.WifiData;
import net.pacificsoft.coldchaintracker.models.WifiDataNetwork;
import net.pacificsoft.coldchaintracker.R;
import net.pacificsoft.coldchaintracker.network.ApiClient;
import net.pacificsoft.coldchaintracker.network.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "MainActivity";
    private List<Message> messages = new ArrayList<>();
    private RecyclerView recyclerView;
    private MessagesAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private WifiData mWifiData;
    private WifiManager wifiManager;
    private MainActivityReceiver wifiReceiver = new MainActivityReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWifiData = null;

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            //do something if have the permissions
        } else {
            //do something, permission was previously granted; or legacy device
        }

//        // set receiver
//        MainActivityReceiver mReceiver = new MainActivityReceiver();
//        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.APP_NAME));
//
//        // launch WiFi service
//        Intent intent = new Intent(this, WifiService.class);
//        startService(intent);
//
//        // recover retained object
//        mWifiData = (WifiData) getLastCustomNonConfigurationInstance();

        // set layout
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Scanned networks", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                scanWifi();
                plotData();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        scanWifi();
        plotData();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.routes) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mWifiData;
    }

    public void plotData() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.scanningResultBlock);
        linearLayout.removeAllViews();

        if (mWifiData == null) {
            Log.d(TAG, "Plotting data: no networks");
            TextView noDataView = new TextView(this);
            noDataView.setText(getResources().getString(R.string.wifi_no_data));
            noDataView.setGravity(Gravity.CENTER_HORIZONTAL);
            noDataView.setPadding(0, 50, 0, 0);
            noDataView.setTextSize(18);
            linearLayout.addView(noDataView);
        } else {
            Log.d(TAG, "Plotting data");

            TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT);

            TableLayout tableLayout = new TableLayout(this);
            tableLayout.setLayoutParams(tableParams);
            tableLayout.setStretchAllColumns(true);

            // row header
            TableRow tableRowHeader = new TableRow(this);
            tableRowHeader.setLayoutParams(rowParams);

            TextView ssidText = new TextView(this);
            ssidText.setText(getResources().getString(R.string.ssid_text));
            ssidText.setTypeface(null, Typeface.BOLD);

            TextView chText = new TextView(this);
            chText.setText(getResources().getString(R.string.ch_text));
            chText.setTypeface(null, Typeface.BOLD);

            TextView rxText = new TextView(this);
            rxText.setText(getResources().getString(R.string.rx_text));
            rxText.setTypeface(null, Typeface.BOLD);

            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                TextView bssidText = new TextView(this);
                bssidText.setText(getResources().getString(R.string.bssid_text));
                bssidText.setTypeface(null, Typeface.BOLD);

                tableRowHeader.addView(ssidText);
                tableRowHeader.addView(bssidText);
                tableRowHeader.addView(chText);
                tableRowHeader.addView(rxText);
            } else {
                tableRowHeader.addView(ssidText);
                tableRowHeader.addView(chText);
                tableRowHeader.addView(rxText);
            }

            tableLayout.addView(tableRowHeader);

            // rows data
            for (WifiDataNetwork net : mWifiData.getNetworks()) {
                TextView ssidVal = new TextView(this);
                ssidVal.setText(net.getSsid());

                TextView chVal = new TextView(this);
                chVal.setText(String.valueOf(WifiDataNetwork.convertFrequencyToChannel(net.getFrequency())));

                TextView rxVal = new TextView(this);
                rxVal.setText(String.valueOf(net.getLevel()));

                TableRow tableRow = new TableRow(this);
                tableRow.setLayoutParams(rowParams);

                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    TextView bssidVal = new TextView(this);
                    bssidVal.setText(net.getBssid());

                    rxVal.setText(String.valueOf(net.getLevel()) + " dBm");

                    tableRow.addView(ssidVal);
                    tableRow.addView(bssidVal);
                    tableRow.addView(chVal);
                    tableRow.addView(rxVal);
                } else {
                    tableRow.addView(ssidVal);
                    tableRow.addView(chVal);
                    tableRow.addView(rxVal);
                }

                tableLayout.addView(tableRow);
            }

            linearLayout.addView(tableLayout);
        }
    }

    public void scanWifi(){
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
    }


    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            WifiData data = (WifiData) intent.getParcelableExtra(Constants.WIFI_DATA);
            WifiData data = new WifiData();
            List<ScanResult> mResults = wifiManager.getScanResults();
            Log.d(TAG, "New scan result: (" + mResults.size() + ") networks found");
            unregisterReceiver(this);

            data.addNetworks(mResults);

            if (data != null) {
                mWifiData = data;
                plotData();
            }
        }
    }

    /**
     * Fetches mail messages by making HTTP request
     * url: http://api.androidhive.info/json/inbox.json
     */
    private void getInbox() {
        swipeRefreshLayout.setRefreshing(true);

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Call<List<Message>> call = apiService.getInbox();
        call.enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                // clear the inbox
                messages.clear();

                // add all the messages
                // messages.addAll(response.body());

                // TODO - avoid looping
                // the loop was performed to add colors to each message
                for (Message message : response.body()) {
                    // generate a random color
                    message.setColor(getRandomMaterialColor("400"));
                    messages.add(message);
                }

                mAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unable to fetch json: " + t.getMessage(), Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * chooses a random color from array.xml
     */
    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", getPackageName());

        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }


    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//            mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);

            // disable swipe refresh if action mode is enabled
            swipeRefreshLayout.setEnabled(false);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
//                case R.id.action_delete:
//                    // delete all the selected messages
//                    deleteMessages();
//                    mode.finish();
//                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelections();
            swipeRefreshLayout.setEnabled(true);
            actionMode = null;
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.resetAnimationIndex();
                    // mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    // deleting the messages from recycler view
    private void deleteMessages() {
        mAdapter.resetAnimationIndex();
        List<Integer> selectedItemPositions =
                mAdapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            mAdapter.removeData(selectedItemPositions.get(i));
        }
        mAdapter.notifyDataSetChanged();
    }
}
