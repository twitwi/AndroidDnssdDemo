/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.androiddnssd.discovery;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import javax.jmdns.ServiceInfo;

public class DiscoveryActivity extends Activity {

    //ServiceFactory f;
    //ServiceRepository repo;
    //Service s;
    android.net.wifi.WifiManager.MulticastLock lock;
    android.os.Handler handler = new android.os.Handler();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button button = (Button)this.findViewById(R.id.button);
        button.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    //s.sendToAllClients("androidInited", Utility.message("(an)droooiiiiid"));
                }
            });

        handler.postDelayed(new Runnable() {
            public void run() {
                setUp();
            }
            }, 1000);

    }    /** Called when the activity is first created. */


    private String type = "_workstation._tcp.local.";
    private JmDNS jmdns = null;
    private ServiceListener listener = null;
    private ServiceInfo serviceInfo;
    private void setUp() {
        android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
        lock = wifi.createMulticastLock("mylockthereturn");
        lock.setReferenceCounted(true);
        lock.acquire();
        try {
            jmdns = JmDNS.create();
            jmdns.addServiceListener(type, listener = new ServiceListener() {

                @Override
                public void serviceResolved(ServiceEvent ev) {
                    notifyUser("Service resolved: " + ev.getInfo().getQualifiedName() + " port:" + ev.getInfo().getPort());
                }

                @Override
                public void serviceRemoved(ServiceEvent ev) {
                    notifyUser("Service removed: " + ev.getName());
                }

                @Override
                public void serviceAdded(ServiceEvent event) {
                    // Required to force serviceResolved to be called again (after the first search)
                    jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
                }
            });
            serviceInfo = ServiceInfo.create("_test._tcp.local.", "AndroidTest", 0, "plain test service from android");
            jmdns.registerService(serviceInfo);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }


    private void notifyUser(final String msg) {
        handler.postDelayed(new Runnable() {
            public void run() {

        TextView t = (TextView)findViewById(R.id.text);
        t.setText(msg+"\n=== "+t.getText());
            }
            }, 1);

    }

    @Override
        protected void onStart() {
        super.onStart();
        //new Thread(){public void run() {setUp();}}.start();
    }

    @Override
        protected void onStop() {
    	if (jmdns != null) {
            if (listener != null) {
                jmdns.removeServiceListener(type, listener);
                listener = null;
            }
            jmdns.unregisterAllServices();
            try {
                jmdns.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            jmdns = null;
    	}
    	//repo.stop();
        //s.stop();
        lock.release();
    	super.onStop();
    }

}
