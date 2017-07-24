package com.slnkfkb.roamon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import android.content.pm.PackageManager;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    static public TextView myTview;
    static public TextView myToCHview;
    static public TextView myFmCHview;
    static public TextView myToRSSIview;
    static public TextView myFmRSSIview;

    static public CustomAdapter customAdapter;


    private int oldChannel=999;
    private int newChannel=998;

    private int oldRSSI=0;
    private int newRSSI=0;

    //private int count = 0;
    private boolean frozen  =   false;

    // model data, an array of strings
    //private String[]  aLogs =   new String[] { "testString 1", "testString 2"};
    //private ArrayList<String> aLogs =   new ArrayList<String>();

    public class LogEntry {
        public String rawLog;
        public String toCH;
        public String fmCH;
        public String toRSSI;
        public String fmRSSI;

        public String APalt1CH;
        public String APalt1RSSI;
        public String APalt2CH;
        public String APalt2RSSI;

        public String faultCoChannelInterference;
        public boolean anyFault;

        public LogEntry (String rawLog) {
            this.rawLog =   rawLog;
            this.anyFault   =   false;


            //this.toCH   =   "000";
            //this.fmCH   =   "000";
            //this.toRSSI   =   "-40";
            //this.fmRSSI   =   "-75";
            this.APalt1CH   =   "999";
            this.APalt1RSSI =   "-99";
            this.APalt2CH   =   "999";
            this.APalt2RSSI =   "-99";
            //this.faultCoChannelInterference =   false;

            //char[]  t   =   new char[rawLog.length()];
            //for(int i=0; i< rawLog.length(); i++) {
            //    t[i] =   rawLog.charAt(i);
            //}
            //MainActivity.myTview.setText(t, 0, t.length);

            //line = "Successful Handoff to 84b2.617e.be76(Channel:44 Score:451 RSSI:-49 dBm Penalty:0) from 84b2.617e.6756(Channel:40 Score:439 RSSI:-61 dBm Penalty:0), Reason 153, Other Aps: 84b2.617e.be76 (Channel:44 Score:451 RSSI:-49 dBm Penalty:0 Reason:140), 84b2.6189.56e6 (Channel:157 Score:442 RSSI:-58 dBm Penalty:0 Reason:140), 84b2.618d.fd96 (Channel:161 Score:432 RSSI:-68 dBm Penalty:0 Reason:140), 84b2.6194.bcd6 (Channel:149 Score:427 RSSI:-73 dBm Penalty:0 Reason:140), TxPO:15 dBm, TxPN:15 dBm";
            // check for co-channel interference
            try {
                String newTarget = "Channel:";
                int start = rawLog.indexOf(newTarget) + newTarget.length();
                int end = start + 9;
                CharSequence csChannel = rawLog.subSequence(start, end);
                newChannel = Integer.parseInt(csChannel.subSequence(0, csChannel.toString().indexOf(" ")).toString());

                String RSSITarget = "RSSI:";
                start = rawLog.indexOf(RSSITarget) + RSSITarget.length();
                end = start + 9;
                CharSequence csRSSI = rawLog.subSequence(start,end);
                newRSSI =   Integer.parseInt(csRSSI.subSequence(0, csRSSI.toString().indexOf(" ")).toString());

                String midTarget = "from";
                String midEndTarget = "Reason";
                start = rawLog.indexOf(midTarget) + midTarget.length();
                end = rawLog.indexOf(midEndTarget);
                CharSequence mid = rawLog.subSequence(start, end);

                String oldTarget = newTarget;
                start = mid.toString().indexOf(oldTarget) + oldTarget.length();
                end = start + 9;
                csChannel = mid.subSequence(start, end);
                oldChannel = Integer.parseInt(csChannel.subSequence(0, csChannel.toString().indexOf(" ")).toString());

                start = mid.toString().indexOf(RSSITarget) + RSSITarget.length();
                end = start + 9;
                csRSSI = mid.toString().subSequence(start,end);
                oldRSSI =   Integer.parseInt(csRSSI.subSequence(0, csRSSI.toString().indexOf(" ")).toString());

            } catch (Exception e) {
                // no problem!
            }


            //char[] n = intToCharA(newChannel);// {(char) ('0' + (newChannel / 100) % 10), (char) ('0' + (newChannel / 10) % 10), (char) ('0' + newChannel % 10), ' '};
            //char[] o = intToCharA(oldChannel);// {(char) ('0' + (oldChannel / 100) % 10), (char) ('0' + (oldChannel / 10) % 10), (char) ('0' + oldChannel % 10), ' '};
            this.toCH   =   Integer.toString(newChannel);
            this.fmCH   =   Integer.toString(oldChannel);

            if (oldChannel == newChannel) {
                this.faultCoChannelInterference =   "YES";
                this.anyFault   =   true;
                //MainActivity.myToCHview.setBackgroundColor(Color.RED);
                //MainActivity.myFmCHview.setBackgroundColor(Color.RED);
                //frozen=true;
            } else {
                this.faultCoChannelInterference =   "NO";
                //MainActivity.myToCHview.setBackgroundColor(Color.GREEN);
                //MainActivity.myFmCHview.setBackgroundColor(Color.GREEN);
            }

            //char[] oR = intToCharA( oldRSSI );
            this.fmRSSI =   Integer.toString(oldRSSI);
            //myFmRSSIview.setText(oR, 0, oR.length);

            //char[] nR = intToCharA( newRSSI );
            this.toRSSI =   Integer.toString( newRSSI );
            //myToRSSIview.setText(nR, 0, nR.length);

        }
    }
    private ArrayList<LogEntry> aLogs = new ArrayList<LogEntry>();


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (frozen)
                return;

            //++count;

            // extras for android.net.conn.CONNECTIVITY_CHANGE:
            // networkInfo
            // networkType
            // netCondition
            // extraInfo

            //  it REALLY doesn't like this so we write crappy code instead:
            //                                              String line = intent.getStringExtra("networkInfo");

            // FKB Not understood why intent.getStringExtra("networkInfo") crashes the app -- so reading every key is a workaround
            Bundle bundle = intent.getExtras();
            int j=0;
            if (bundle != null) {
                for (String key : bundle.keySet()) {


                    String line;
                    Object value = bundle.get(key);
                    line=value.toString();

                    if (! line.contains("Successful Handoff"))
                        break;

                    LogEntry newLog = new LogEntry(line);
                    customAdapter.add(newLog);

                    /*
                    char[]  t   =   new char[line.length()];
                    for(int i=0; i< line.length(); i++) {
                        t[i] =   line.charAt(i);
                    }
                    //MainActivity.myTview.setText(t, 0, t.length);

                    //line = "Successful Handoff to 84b2.617e.be76(Channel:44 Score:451 RSSI:-49 dBm Penalty:0) from 84b2.617e.6756(Channel:40 Score:439 RSSI:-61 dBm Penalty:0), Reason 153, Other Aps: 84b2.617e.be76 (Channel:44 Score:451 RSSI:-49 dBm Penalty:0 Reason:140), 84b2.6189.56e6 (Channel:157 Score:442 RSSI:-58 dBm Penalty:0 Reason:140), 84b2.618d.fd96 (Channel:161 Score:432 RSSI:-68 dBm Penalty:0 Reason:140), 84b2.6194.bcd6 (Channel:149 Score:427 RSSI:-73 dBm Penalty:0 Reason:140), TxPO:15 dBm, TxPN:15 dBm";
                    // check for co-channel interference
                    try {
                        String newTarget = "Channel:";
                        int start = line.indexOf(newTarget) + newTarget.length();
                        int end = start + 9;
                        CharSequence csChannel = line.subSequence(start, end);
                        newChannel = Integer.parseInt(csChannel.subSequence(0, csChannel.toString().indexOf(" ")).toString());

                        String RSSITarget = "RSSI:";
                        start = line.indexOf(RSSITarget) + RSSITarget.length();
                        end = start + 9;
                        CharSequence csRSSI = line.subSequence(start,end);
                        newRSSI =   Integer.parseInt(csRSSI.subSequence(0, csRSSI.toString().indexOf(" ")).toString());

                        String midTarget = "from";
                        String midEndTarget = "Reason";
                        start = line.indexOf(midTarget) + midTarget.length();
                        end = line.indexOf(midEndTarget);
                        CharSequence mid = line.subSequence(start, end);

                        String oldTarget = newTarget;
                        start = mid.toString().indexOf(oldTarget) + oldTarget.length();
                        end = start + 9;
                        csChannel = mid.subSequence(start, end);
                        oldChannel = Integer.parseInt(csChannel.subSequence(0, csChannel.toString().indexOf(" ")).toString());

                        start = mid.toString().indexOf(RSSITarget) + RSSITarget.length();
                        end = start + 9;
                        csRSSI = mid.toString().subSequence(start,end);
                        oldRSSI =   Integer.parseInt(csRSSI.subSequence(0, csRSSI.toString().indexOf(" ")).toString());

                    } catch (Exception e) {
                        // no problem!
                    }


                    char[] n = intToCharA(newChannel);// {(char) ('0' + (newChannel / 100) % 10), (char) ('0' + (newChannel / 10) % 10), (char) ('0' + newChannel % 10), ' '};
                    char[] o = intToCharA(oldChannel);// {(char) ('0' + (oldChannel / 100) % 10), (char) ('0' + (oldChannel / 10) % 10), (char) ('0' + oldChannel % 10), ' '};
                    myToCHview.setText(n,0,n.length);
                    myFmCHview.setText(o,0,o.length);

                    if (oldChannel == newChannel) {
                        MainActivity.myToCHview.setBackgroundColor(Color.RED);
                        MainActivity.myFmCHview.setBackgroundColor(Color.RED);
                        frozen=true;
                    } else {
                        MainActivity.myToCHview.setBackgroundColor(Color.GREEN);
                        MainActivity.myFmCHview.setBackgroundColor(Color.GREEN);
                    }

                    char[] oR = intToCharA( oldRSSI );
                    myFmRSSIview.setText(oR, 0, oR.length);

                    char[] nR = intToCharA( newRSSI );
                    myToRSSIview.setText(nR, 0, nR.length);

                    */
                    break;
                }

            }


        }
    };

    private int changeCount =   0;

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.qos.lib.TSPEC_STATUS");
        //if (changeCount==0) {
        //    changeCount++;
        //} else {
        //    LogEntry newLog = new LogEntry("bogus "+changeCount++);
        //    customAdapter.add(newLog);
        //    //if (aLogs.length < 5) {
        //    //    aLogs[aLogs.length] = "length=" + aLogs.length;
        //    //    customAdapter.notifyDataSetChanged();
        //    //}
        //}
        registerReceiver(receiver, filter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

    // negative returns 2 digits and a leading '-', positive returns 3 digits
    protected char[] intToCharA(int i)  {
        if (i<0) {
            int r=-i;
            char[] n = {'-', (char) ('0' + (r / 10) % 10), (char) ('0' + r % 10), ' '};
            return n;
        } else {
            char[] p = {(char) ('0' + (i / 100) % 10), (char) ('0' + (i / 10) % 10), (char) ('0' + i % 10), ' '};
            return p;
        }
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*
        myTview = (TextView) findViewById(R.id.helloText);


        int oldChannel  = 0;
        int newChannel  = 0;
        int oldRSSI     = 0;
        int newRSSI     = 0;

        String line = "";//"Successful Handoff to 84b2.617e.be76(Channel:44 Score:451 RSSI:-49 dBm Penalty:0) from 84b2.617e.6756(Channel:40 Score:439 RSSI:-61 dBm Penalty:0), Reason 153, Other Aps: 84b2.617e.be76 (Channel:44 Score:451 RSSI:-49 dBm Penalty:0 Reason:140), 84b2.6189.56e6 (Channel:157 Score:442 RSSI:-58 dBm Penalty:0 Reason:140), 84b2.618d.fd96 (Channel:161 Score:432 RSSI:-68 dBm Penalty:0 Reason:140), 84b2.6194.bcd6 (Channel:149 Score:427 RSSI:-73 dBm Penalty:0 Reason:140), TxPO:15 dBm, TxPN:15 dBm";
        // check for co-channel interference
        try {
            String newTarget = "Channel:";
            int start = line.indexOf(newTarget) + newTarget.length();
            int end = start + 9;
            CharSequence csChannel = line.subSequence(start, end);
            newChannel = Integer.parseInt(csChannel.subSequence(0, csChannel.toString().indexOf(" ")).toString());

            String RSSITarget = "RSSI:";
            start = line.indexOf(RSSITarget) + RSSITarget.length();
            end = start + 9;
            CharSequence csRSSI = line.subSequence(start,end);
            newRSSI =   Integer.parseInt(csRSSI.subSequence(0, csRSSI.toString().indexOf(" ")).toString());

            String midTarget = "from";
            String midEndTarget = "Reason";
            start = line.indexOf(midTarget) + midTarget.length();
            end = line.indexOf(midEndTarget);
            CharSequence mid = line.subSequence(start, end);

            String oldTarget = newTarget;
            start = mid.toString().indexOf(oldTarget) + oldTarget.length();
            end = start + 9;
            csChannel = mid.subSequence(start, end);
            oldChannel = Integer.parseInt(csChannel.subSequence(0, csChannel.toString().indexOf(" ")).toString());

            start = mid.toString().indexOf(RSSITarget) + RSSITarget.length();
            end = start + 9;
            csRSSI = mid.toString().subSequence(start,end);
            oldRSSI =   Integer.parseInt(csRSSI.subSequence(0, csRSSI.toString().indexOf(" ")).toString());


        } catch (Exception e) {
            // no problem!
        }
        char[] n = intToCharA(newChannel);
        //myToCHview.setText(n, 0, n.length);

        char[] o = intToCharA(oldChannel);
        //myFmCHview.setText(o, 0, o.length);

        char[] nR = intToCharA( newRSSI );
        //myToRSSIview.setText(nR, 0, nR.length);

        char[] oR = intToCharA( oldRSSI );
        //myFmRSSIview.setText(oR, 0, oR.length);
*/
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;

            this.setTitle( "RoaMon " + version );

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // magic:
        //setContentView(R.layout.activity_main);

        //ListView lvLogs = (ListView)findViewById(R.id.lvRawLogs);
        // no good:
        //ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1 , aLogs);
        //ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1 , aLogs);
        //lvLogs.setAdapter(adapter);

        //customAdapter = new CustomAdapter();
        //lvLogs.setAdapter(customAdapter);



// Create the adapter to convert the array to views
        customAdapter = new CustomAdapter(this, aLogs);


// Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.lvRawLogs);
        listView.setAdapter(customAdapter);


    }

    class CustomAdapter extends ArrayAdapter<LogEntry>    {

        public CustomAdapter(Context context, ArrayList<LogEntry> logs) {
            super(context, 0, logs);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LogEntry logEntry   =   getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_logentry, parent, false);
            }
            // Lookup view for data population
            TextView tvRawLog = (TextView) convertView.findViewById(R.id.textView_rawlog);
            TextView tvToCH = (TextView) convertView.findViewById(R.id.textView_toCH);
            TextView tvToRSSI = (TextView) convertView.findViewById(R.id.textView_toRSSI);
            TextView tvFmCH = (TextView) convertView.findViewById(R.id.textView_fmCH);
            TextView tvFmRSSI = (TextView) convertView.findViewById(R.id.textView_fmRSSI);
            TextView tvAlt1CH = (TextView) convertView.findViewById(R.id.textView_alt1CH);
            TextView tvAlt1RSSI = (TextView) convertView.findViewById(R.id.textView_alt1RSSI);
            TextView tvAlt2CH = (TextView) convertView.findViewById(R.id.textView_alt2CH);
            TextView tvAlt2RSSI = (TextView) convertView.findViewById(R.id.textView_alt2RSSI);
            TextView tvCCIfault = (TextView) convertView.findViewById(R.id.textView_CCIfault);

            // Populate the data into the template view using the data object
            tvRawLog.setText(logEntry.rawLog);
            tvToCH.setText(logEntry.toCH);
            tvToRSSI.setText(logEntry.toRSSI);
            tvFmCH.setText(logEntry.fmCH);
            tvFmRSSI.setText(logEntry.fmRSSI);
            tvAlt1CH.setText(logEntry.APalt1CH);
            tvAlt1RSSI.setText(logEntry.APalt1RSSI);
            tvAlt2CH.setText(logEntry.APalt2CH);
            tvAlt2RSSI.setText(logEntry.APalt2RSSI);
            tvCCIfault.setText(logEntry.faultCoChannelInterference);

            if ( logEntry.anyFault ) {
                tvRawLog.setBackgroundColor(Color.RED);
            }

            return convertView;
        }

    }

    /*
    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return aLogs.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = getLayoutInflater().inflate(R.layout.rawlog,null);
            TextView textView_rawlog = (TextView)convertView.findViewById(R.id.textView_rawlog);
            textView_rawlog.setText(aLogs[position]);

            return convertView;
        }
    }
    */
}
