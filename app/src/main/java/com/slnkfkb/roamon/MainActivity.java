package com.slnkfkb.roamon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    static public TextView myTview;
    static public TextView myToCHview;
    static public TextView myFmCHview;
    static public TextView myToRSSIview;
    static public TextView myFmRSSIview;

    private int oldChannel=999;
    private int newChannel=998;

    private int oldRSSI=0;
    private int newRSSI=0;

    //private int count = 0;
    private boolean frozen  =   false;

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

                    char[]  t   =   new char[line.length()];
                    for(int i=0; i< line.length(); i++) {
                        t[i] =   line.charAt(i);
                    }
                    MainActivity.myTview.setText(t, 0, t.length);

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


                    break;
                }

            }


        }
    };


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


        myTview = (TextView) findViewById(R.id.helloText);


        int oldChannel  = 0;
        int newChannel  = 0;
        int oldRSSI     = 0;
        int newRSSI     = 0;

        myToCHview      = (TextView) findViewById(R.id.toChannel);
        myFmCHview      = (TextView) findViewById(R.id.fromChannel);
        myToRSSIview    = (TextView) findViewById(R.id.toRSSI);
        myFmRSSIview    = (TextView) findViewById(R.id.fmRSSI);



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
        myToCHview.setText(n, 0, n.length);

        char[] o = intToCharA(oldChannel);
        myFmCHview.setText(o, 0, o.length);

        char[] nR = intToCharA( newRSSI );
        myToRSSIview.setText(nR, 0, nR.length);

        char[] oR = intToCharA( oldRSSI );
        myFmRSSIview.setText(oR, 0, oR.length);
    }
}
