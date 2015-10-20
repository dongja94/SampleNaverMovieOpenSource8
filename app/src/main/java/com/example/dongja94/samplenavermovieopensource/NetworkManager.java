package com.example.dongja94.samplenavermovieopensource;

import android.content.Context;

import com.begentgroup.xmlparser.XMLParser;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Created by dongja94 on 2015-10-20.
 */
public class NetworkManager {
    private static NetworkManager instance;
    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    AsyncHttpClient client;
    XMLParser parser;
    Gson gson;
    private NetworkManager() {

        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client = new AsyncHttpClient();
            client.setSSLSocketFactory(socketFactory);
            client.setCookieStore(new PersistentCookieStore(MyApplication.getContext()));
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }


        parser = new XMLParser();
        gson = new Gson();
        client.setCookieStore(new PersistentCookieStore(MyApplication.getContext()));
    }

    public HttpClient getHttpClient() {
        return client.getHttpClient();
    }

    public interface OnResultListener<T> {
        public void onSuccess(T result);
        public void onFail(int code);
    }


    private static final String SERVER = "http://openapi.naver.com";

    private static final String MOVIE_URL = SERVER + "/search";
    private static final String KEY = "55f1e342c5bce1cac340ebb6032c7d9a";
    private static final String TARGET = "movie";

    public void getNetworkMelon(Context context, String keyword, int start, int display, final OnResultListener<NaverMovies> listener) {
        final RequestParams params = new RequestParams();
        params.put("key", KEY);
        params.put("query", keyword);
        params.put("display", display);
        params.put("start", start);
        params.put("target", TARGET);

        client.get(context, MOVIE_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                ByteArrayInputStream bais = new ByteArrayInputStream(responseBody);
                NaverMovies movies = parser.fromXml(bais, "channel", NaverMovies.class);
                listener.onSuccess(movies);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                listener.onFail(statusCode);
            }
        });
    }

    private static final String MELON_URL = "http://apis.skplanetx.com/melon/charts/realtime";

    public void getMelonChart(Context context, int page, int count, final OnResultListener<Melon> listener) {
        RequestParams params = new RequestParams();
        params.put("count", count);
        params.put("page", page);
        params.put("version",1);
//        File file = new File("abc");
//
//        for (int i = 0; i < 5; i++) {
//            params.add("count", "" + i);
//        }
//
//        File[] files = new File[3];
//        try {
//            params.put("files" , files);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }


        Header[] headers = new Header[2];
        headers[0] = new BasicHeader("Accept", "application/json");
        headers[1] = new BasicHeader("appKey", "458a10f5-c07e-34b5-b2bd-4a891e024c2a");

        client.get(context, MELON_URL, headers, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                MelonResult result = gson.fromJson(responseString, MelonResult.class);
                listener.onSuccess(result.melon);
            }
        });


    }

    public void cancelAll(Context context) {
        client.cancelRequests(context, true);
    }
}
