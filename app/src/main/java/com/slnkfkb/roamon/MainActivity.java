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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.content.pm.PackageManager;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    static public TextView myTview;
    static public TextView myTview2;
    static public TextView myTview3;
    static public TextView myTview4;
    static public TextView myTviewFooter;
    static public ImageView myIview;

    static public CustomAdapter customAdapter;


    private int oldChannel=999;
    private int newChannel=998;

    private int oldRSSI=0;
    private int newRSSI=0;

    private int alt1CH=0;
    private int alt1RSSI=0;
    private int alt2CH=0;
    private int alt2RSSI=0;

    private boolean frozen  =   false;
    private boolean whiteScreen =   true;

    public int str2Int(String strSource, char delimiter)    {
        return ( Integer.parseInt(strSource.substring(0,strSource.indexOf(delimiter))));
    }
    public String searchNthAfterTarget (String strSource, String strTarget, String strAfter, int countOfAfters)  {

        String retStr   =   strSource;
        int start       =   0;

        // return to end of strSource begging at strTarget which occurs after countOfAfters occurances of strAfter
        for (int i=0; i<countOfAfters; i++) {
            start       =   retStr.indexOf(strAfter) + strAfter.length();
            retStr      =   retStr.substring(start);
        }
        start           =   retStr.indexOf(strTarget)+strTarget.length();
        retStr          =   retStr.substring(start);

        return retStr;
    }
    public int intSearchNthAfterTarget (String strSource, String strTarget, String strAfter, int countOfAfters) {
        return str2Int( searchNthAfterTarget ( strSource, strTarget, strAfter, countOfAfters ), ' ');
    }

    public boolean isDFSchannel( int ch )   {
        return ( ch == 52 || ch == 56 || ch == 60 || ch == 64 || ch == 100 || ch == 104 || ch == 108 || ch == 112 || ch == 116 || ch == 120 || ch == 124 || ch == 128 || ch == 132 || ch == 140 );
    }

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
        public String faultDFS;

        public boolean anyFault;
        public boolean DFSfault;
        public boolean CCIfault;

        public LogEntry (String rawLog) {
            this.rawLog =   rawLog;
            this.anyFault   =   false;
            this.DFSfault   =   false;
            this.CCIfault   =   false;


            this.APalt1CH   =   "999";
            this.APalt1RSSI =   "-99";
            this.APalt2CH   =   "999";
            this.APalt2RSSI =   "-99";

            //line = "Successful Handoff to 84b2.617e.be76(Channel:44 Score:451 RSSI:-49 dBm Penalty:0) from 84b2.617e.6756(Channel:40 Score:439 RSSI:-61 dBm Penalty:0), Reason 153, Other Aps: 84b2.617e.be76 (Channel:44 Score:451 RSSI:-49 dBm Penalty:0 Reason:140), 84b2.6189.56e6 (Channel:157 Score:442 RSSI:-58 dBm Penalty:0 Reason:140), 84b2.618d.fd96 (Channel:161 Score:432 RSSI:-68 dBm Penalty:0 Reason:140), 84b2.6194.bcd6 (Channel:149 Score:427 RSSI:-73 dBm Penalty:0 Reason:140), TxPO:15 dBm, TxPN:15 dBm";
            // check for co-channel interference
            try {
                newChannel  =   intSearchNthAfterTarget( rawLog, "Channel:", "", 0);
                newRSSI     =   intSearchNthAfterTarget( rawLog, "RSSI:", "", 0);
                oldChannel  =   intSearchNthAfterTarget( rawLog, "Channel:", "from", 1);
                oldRSSI     =   intSearchNthAfterTarget( rawLog, "RSSI:", "from", 1);
                String strAPalt1    =   searchNthAfterTarget( rawLog, "Channel:", "Other Aps:", 1);

                alt1CH              =   intSearchNthAfterTarget( strAPalt1, "Channel:", "", 0);
                this.APalt1CH       =   Integer.toString( alt1CH );

                alt1RSSI            =   intSearchNthAfterTarget( strAPalt1, "RSSI:", "Channel:", 1);
                this.APalt1RSSI     =   Integer.toString( alt1RSSI );

                alt2CH              =   intSearchNthAfterTarget( strAPalt1, "Channel:", "Channel:", 1);
                this.APalt2CH       =   Integer.toString( alt2CH );

                alt2RSSI            =   intSearchNthAfterTarget( strAPalt1, "RSSI:", "Channel:", 2);
                this.APalt2RSSI     =   Integer.toString( alt2RSSI );

            } catch (Exception e) {
                // no problem!
            }


            this.toCH   =   Integer.toString(newChannel);
            this.fmCH   =   Integer.toString(oldChannel);

            // test
            //newChannel = oldChannel;
            //alt1CH      =   60;
            //alt1RSSI    =   -40;

            if (oldChannel == newChannel) {
                this.faultCoChannelInterference =   "YES";
                this.anyFault   =   true;
                this.CCIfault   =   true;
            } else {
                this.faultCoChannelInterference =   "NO";
            }

            this.fmRSSI =   Integer.toString(oldRSSI);
            this.toRSSI =   Integer.toString( newRSSI );

            // is alt1 a DFS channel?  Does it have a higher RSSI than chosen?  Then we have a DFS fault!
            if ( isDFSchannel( alt1CH ) && ( alt1RSSI > newRSSI )  )   {
                this.faultDFS   =   "YES";
                this.anyFault   =   true;
                this.DFSfault   =   true;
            } else {
                this.faultDFS   =   "NO";
            }
        }
    }
    private ArrayList<LogEntry> aLogs = new ArrayList<LogEntry>();


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (frozen)
                return;

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

                    if ( whiteScreen ) {
                        whiteScreen =   false;
                        myTview.setText("");
                        myTview2.setText("");
                        myTview3.setText("");
                        myTview4.setText("");
                        myTviewFooter.setText("");
                        myIview.setImageBitmap(null);
                    }

                    LogEntry newLog = new LogEntry(line);
                    customAdapter.add(newLog);

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
        registerReceiver(receiver, filter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

    // negative returns 2 digits and a leading '-', positive returns 3 digits
    /*
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
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTview = (TextView) findViewById(R.id.title);
        myIview = (ImageView) findViewById(R.id.icon);
        myTview2 = (TextView) findViewById(R.id.title2);
        myTview3 = (TextView) findViewById(R.id.title3);
        myTview4 = (TextView) findViewById(R.id.title4);
        myTviewFooter = (TextView) findViewById(R.id.footer);

        /*  test code... leave this here
        int oldChannel  = 0;
        int newChannel  = 0;
        int oldRSSI     = 0;
        int newRSSI     = 0;

        String line = "Successful Handoff to 84b2.617e.be76(Channel:44 Score:451 RSSI:-49 dBm Penalty:0) from 84b2.617e.6756(Channel:40 Score:439 RSSI:-61 dBm Penalty:0), Reason 153, Other Aps: 84b2.617e.be76 (Channel:44 Score:451 RSSI:-49 dBm Penalty:0 Reason:140), 84b2.6189.56e6 (Channel:157 Score:442 RSSI:-58 dBm Penalty:0 Reason:140), 84b2.618d.fd96 (Channel:161 Score:432 RSSI:-68 dBm Penalty:0 Reason:140), 84b2.6194.bcd6 (Channel:149 Score:427 RSSI:-73 dBm Penalty:0 Reason:140), TxPO:15 dBm, TxPN:15 dBm";
        // check for co-channel interference
        try {
            newChannel  =   intSearchNthAfterTarget( line, "Channel:", "", 0);

            newRSSI     =   intSearchNthAfterTarget( line, "RSSI:", "", 0);

            oldChannel  =   intSearchNthAfterTarget( line, "Channel:", "from", 1);

            oldRSSI     =   intSearchNthAfterTarget( line, "RSSI:", "from", 1);

        } catch (Exception e) {
            // no problem!
        }
        */

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;

            this.setTitle( "RoaMon " + version );

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

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
            TextView tvDFSfault = (TextView) convertView.findViewById(R.id.textView_DFSfault);

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
            tvDFSfault.setText(logEntry.faultDFS);

            if ( logEntry.anyFault ) {
                tvRawLog.setBackgroundColor(Color.RED);
            }
            if ( logEntry.DFSfault ) {
                tvDFSfault.setBackgroundColor(Color.RED);
            }
            if ( logEntry.CCIfault ) {
                tvCCIfault.setBackgroundColor(Color.RED);
            }

            return convertView;
        }
    }
}
