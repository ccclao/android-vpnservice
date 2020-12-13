package com.mocyx.basic_client;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mocyx.basic_client.bio.BioTcpHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = BioTcpHandler.class.getSimpleName();

    public static AtomicLong downByte = new AtomicLong(0);
    public static AtomicLong upByte = new AtomicLong(0);
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.btn_stop)
    Button btnStop;
    @BindView(R.id.btn_http)
    Button btnHttp;
    @BindView(R.id.btn_dns)
    Button btnDns;
    @BindView(R.id.textView1)
    TextView textView;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    private HttpURLConnection httpURLConnection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        Thread t = new Thread(new UpdateText(textView));
        t.start();
    }

    @OnClick({R.id.btn_start, R.id.btn_stop, R.id.btn_http, R.id.btn_dns, R.id.fab})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                startVpn();
                break;
            case R.id.btn_stop:
                clickStop(view);
                break;
            case R.id.btn_http:
                clickHttp(view);
                break;
            case R.id.btn_dns:
                clickDns(view);
                break;
            case R.id.fab:
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
        }
    }

    /**
     * 更新显示文本
     */
    static class UpdateText implements Runnable {

        TextView textView;

        UpdateText(TextView textView) {
            this.textView = textView;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);
                    textView.setText(String.format("up %dKB down %dKB", MainActivity.upByte.get() / 1024, MainActivity.downByte.get() / 1024));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }


    private static final int VPN_REQUEST_CODE = 0x0F;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: 开启服务");
            //waitingForVPNStart = true;
            startService(new Intent(this, LocalVPNService.class));
            //enableButton(false);
        }
    }

    /**
     * 点击开始
     */
    public void startVpn() {

        Intent vpnIntent = VpnService.prepare(this);

        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
        } else {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     * d点击HTTP
     * @param view
     */
    public void clickHttp(View view) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long ts = System.currentTimeMillis();
                    //URL yahoo = new URL("https://www.google.com/");
                    URL url = new URL("https://www.baidu.com/");
//                    URL url = new URL("http://120.24.61.225:8888/proxy.pac");
                    httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setRequestProperty("Connection", "close");
                    httpURLConnection.setConnectTimeout(3000);

                    httpURLConnection.setReadTimeout(3000);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(httpURLConnection.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        Log.d(TAG, "run: " + inputLine);
                    }
                    Log.d(TAG, "run: http");

                    in.close();
                    long te = System.currentTimeMillis();
                    Log.i(TAG, String.format("http cost %d", te - ts));

                    System.out.printf("1http readline end\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    /**
     * 点击停止
     * @param view
     */
    public void clickStop(View view) {
        httpURLConnection.disconnect();
    }

    public static void displayStuff(String whichHost, InetAddress inetAddress) {
        Log.d(TAG, "displayStuff: ");
        Log.d(TAG, "displayStuff: --------------------------");
        Log.d(TAG, "displayStuff: Which Host:" + whichHost);
        Log.d(TAG, "displayStuff: Canonical Host Name:" + inetAddress.getCanonicalHostName());
        Log.d(TAG, "displayStuff: Host Name:" + inetAddress.getHostName());
        Log.d(TAG, "displayStuff: Host Address:" + inetAddress.getHostAddress());
    }

    /**
     * 点击DNS
     * @param view
     */
    public void clickDns(View view) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long ts = System.currentTimeMillis();
//                    String host = "www.baidu.com";
                    String host = "120.24.61.225";

                    for (InetAddress inetAddress : InetAddress.getAllByName(host)) {
                        displayStuff(host, inetAddress);
                    }
                    long te = System.currentTimeMillis();
                    Log.i(TAG, String.format("dns cost %d", te - ts));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
