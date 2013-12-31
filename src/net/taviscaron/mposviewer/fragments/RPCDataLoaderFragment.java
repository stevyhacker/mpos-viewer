package net.taviscaron.mposviewer.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import net.taviscaron.mposviewer.rpc.RPC;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Data loader fragment
 * @author Andrei Senchuk
 */
public class RPCDataLoaderFragment extends Fragment {
    private static final String TAG = "RPCDataLoaderFragment";
    public static final String DEFAULT_FRAGMENT_TAG = "rpcDataLoaderFragment";

    public interface RPCDataLoaderFragmentListener {
        public void onDataLoadStarted();
        public void onDataLoadFinished(Map<RPC.Method, RPC.RPCResult> results);
    }

    private RPCDataLoaderFragmentListener listener;
    private String url;
    private String token;
    private final AtomicBoolean isLoading = new AtomicBoolean();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (RPCDataLoaderFragmentListener)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isLoading() {
        return isLoading.get();
    }

    public void load(RPC.Method... methods) {
        if(!isLoading.compareAndSet(false, true)) {
            Log.w(TAG, "loader is loading now");
            return;
        }

        new AsyncTask<RPC.Method, Void, Map<RPC.Method, RPC.RPCResult>>() {
            @Override
            protected void onPreExecute() {
                listener.onDataLoadStarted();
            }

            @Override
            protected Map<RPC.Method, RPC.RPCResult> doInBackground(RPC.Method... methods) {
                RPC rpc = new RPC();
                rpc.setUrl(url);
                rpc.setToken(token);

                Map<RPC.Method, RPC.RPCResult> results = new HashMap<RPC.Method, RPC.RPCResult>();

                for(RPC.Method m : methods) {
                    results.put(m, rpc.call(m));
                }

                return results;
            }

            @Override
            protected void onPostExecute(Map<RPC.Method, RPC.RPCResult> results) {
                isLoading.set(false);
                listener.onDataLoadFinished(results);
            }
        }.execute(methods);
    }
}