package com.example.logbackdemo.network;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.example.logbackdemo.R;

public class NetworkActivity extends Activity implements View.OnClickListener {

    TextView tvAboutNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        tvAboutNetwork = (TextView) findViewById(R.id.tv_about_network);
        findViewById(R.id.btn_open_wireless_settings).setOnClickListener(this);
        findViewById(R.id.btn_set_wifi_enabled).setOnClickListener(this);
        findViewById(R.id.btn_F5).setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_wireless_settings:
                NetworkUtils.openWirelessSettings();
                break;
            case R.id.btn_set_data_enabled:
                NetworkUtils.setDataEnabled(!NetworkUtils.getDataEnabled());
                break;
            case R.id.btn_set_wifi_enabled:
                NetworkUtils.setWifiEnabled(!NetworkUtils.getWifiEnabled());
                break;
            case R.id.btn_F5:
                break;
        }
        setAboutNetwork();
    }

    private void setAboutNetwork() {
        tvAboutNetwork.setText("isConnected: " + NetworkUtils.isConnected()
                        + "\ngetDataEnabled: " + NetworkUtils.getDataEnabled()
                        + "\nis4G: " + NetworkUtils.is4G()
                        + "\ngetWifiEnabled: " + NetworkUtils.getWifiEnabled()
                        + "\nisWifiConnected: " + NetworkUtils.isWifiConnected()
                        + "\nisWifiAvailable: " + NetworkUtils.isWifiAvailable()
                        + "\nisAvailableByPing: " + NetworkUtils.isAvailableByPing()
                        + "\ngetNetworkOperatorName: " + NetworkUtils.getNetworkOperatorName()
                        + "\ngetNetworkTypeName: " + NetworkUtils.getNetworkType()
                        + "\ngetIPAddress: " + NetworkUtils.getIPAddress(true)
//                + "\ngetDomainAddress: " + NetworkUtils.getDomainAddress("baidu.com")
        );

        String str = "isConnected:      " + NetworkUtils.isConnected()
                + "\ngetWifiEnabled:    " + NetworkUtils.getWifiEnabled()
                + "\nisWifiConnected:   " + NetworkUtils.isWifiConnected()
                + "\nisWifiAvailable:   " + NetworkUtils.isWifiAvailable()
                + "\nisAvailableByPing: " + NetworkUtils.isAvailableByPing()
                + "\ngetNetworkTypeName: " + NetworkUtils.getNetworkType()
                + "\ngetIPAddress: " + NetworkUtils.getIPAddress(true);

        LogUtils.e(str);
    }
}
