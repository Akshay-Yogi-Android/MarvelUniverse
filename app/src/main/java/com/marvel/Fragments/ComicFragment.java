package com.marvel.Fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.marvel.ComicsAdapter;
import com.marvel.Model.Comic;
import com.marvel.MySingleton;
import com.marvel.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ComicFragment extends Fragment {
    RecyclerView recycle_comic;
    ComicsAdapter comicsAdapter;
    ArrayList<Comic> mComic;

    public static String API_KEY = "b4eabb50233b23461e82c535eeb709d6";
    public static String HASH = "b2be560f16a6090ea794b2a4b081db5a";
    String url = "https://gateway.marvel.com:443/v1/public/comics";

    int limit = 20;
    int offset = 0;
    ProgressBar loadmore_progress;
    ImageView img_filter;
    boolean isFilter = false;
    String dateDescriptor;
    String finalUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_comic, container, false);

        loadmore_progress = v.findViewById(R.id.load_more_progress);
        img_filter = v.findViewById(R.id.img_filter);
        recycle_comic = v.findViewById(R.id.recycle_comics);
        recycle_comic.setHasFixedSize(true);
        recycle_comic.setLayoutManager(new GridLayoutManager(getContext(),2));

        mComic = new ArrayList<>();
        comicsAdapter = new ComicsAdapter(getContext(),mComic);

        recycle_comic.setAdapter(comicsAdapter);

        (new AsyncCallWS()).execute();

        recycle_comic.addOnScrollListener(endOnScrollListener);
        img_filter.setOnClickListener(v1 -> {
            PopupMenu popupMenu = new PopupMenu(getContext(),v1);
            popupMenu.getMenuInflater().inflate(R.menu.comic_filter_menu,popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if(item.getItemId()==R.id.mnu_this_week){
                    offset = 0;
                    isFilter = true;
                    dateDescriptor = "thisWeek";
                    (new AsyncCallWS()).execute();
                }

                else if(item.getItemId()==R.id.mnu_last_week)
                {
                    offset = 0;
                    isFilter = true;
                    dateDescriptor = "lastWeek";
                    (new AsyncCallWS()).execute();
                }
                else if(item.getItemId()==R.id.mnu_next_week)
                {
                    offset = 0;
                    isFilter = true;
                    dateDescriptor = "nextWeek";
                    (new AsyncCallWS()).execute();
                }
                else if(item.getItemId()==R.id.mnu_this_month)
                {
                    offset = 0;
                    isFilter = true;
                    dateDescriptor = "thisMonth";
                    (new AsyncCallWS()).execute();
                }
                return true;
            });
            popupMenu.show();

        });
        return v;
    }
    private RecyclerView.OnScrollListener endOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if(isLastItemDisplaying(recyclerView)){
                Log.i("Reached end: ", "Load more");

                (new AsyncCallWS()).execute();

            }
        }

    };

    private boolean isLastItemDisplaying(RecyclerView recyclerView){
        //Check if the adapter item count is greater than 0
        if(recyclerView.getAdapter().getItemCount() != 0){
            //get the last visible item on screen using the layout manager
            int lastVisibleItemPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();

            if(lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount()-1){
                return true;
            }

        }
        return false;

    }

    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            loadmore_progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            GetComics(offset);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            loadmore_progress.setVisibility(View.GONE);
        }

    }

    private void GetComics(int start) {

        if (start == 0)
            mComic.clear();

        loadmore_progress.setVisibility(View.VISIBLE);

        if (isFilter){
            finalUrl = url+"?ts=1&apikey="+API_KEY+"&hash="+HASH+"&limit="+limit+"&offset="+start+"&dateDescriptor="+dateDescriptor;
        }else {
            finalUrl = url+"?ts=1&apikey="+API_KEY+"&hash="+HASH+"&limit="+limit+"&offset="+start;
        }

        RequestQueue queue = MySingleton.getInstance(this.getContext()).getRequestQueue();
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, finalUrl,
                new Response.Listener<String>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(String response) {


                        Log.d("character_response",response);

                        try {
                            JSONObject j = new JSONObject(response);
                            String status = j.getString("status");
                            if (status.equals("Ok")){

                                JSONObject applist = j.getJSONObject("data");
                                JSONArray jsonArray = applist.getJSONArray("results");
                                if (jsonArray != null && jsonArray.length() > 0){
                                    for (int i = 0; i < jsonArray.length(); i++){
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        final Comic comic = new Comic();
                                        comic.setTitle(jsonObject.getString("title"));

                                        JSONObject jsonObject1 = jsonObject.getJSONObject("thumbnail");
                                        comic.setImage(jsonObject1.getString("path")+"."+jsonObject1.getString("extension"));


                                        mComic.add(comic);

                                    }
                                    comicsAdapter.notifyDataSetChanged();
                                }

                            }


                        } catch (JSONException e) {
                            e.printStackTrace();

                        }
                        loadmore_progress.setVisibility(View.GONE);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                if (volleyError instanceof TimeoutError || volleyError instanceof NoConnectionError) {
                    // Is thrown if there's no network connection or server is down
                    // We return to the last fragment
                    Log.d("eerror", String.valueOf(volleyError));
                    NetworkDialog();
                    assert getFragmentManager() != null;
                    if (getFragmentManager().getBackStackEntryCount() != 0) {
                        getFragmentManager().popBackStack();
                    }

                } else {
                    // Is thrown if there's no network connection or server is down
                    Log.d("eerror", String.valueOf(volleyError));
                    NetworkDialog();
                    // We return to the last fragment
                    assert getFragmentManager() != null;
                    if (getFragmentManager().getBackStackEntryCount() != 0) {
                        getFragmentManager().popBackStack();
                    }
                }

                loadmore_progress.setVisibility(View.GONE);
            }


        })

        {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 3 * 60 * 1000; // in 3 minutes cache will be hit, but also refreshed on background
                    final long cacheExpired = 24 * 60 * 60 * 1000; // in 24 hours this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(jsonString, cacheEntry);
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                }
            }
            @Override
            protected void deliverResponse(String response) {
                super.deliverResponse(response);
            }
            @Override
            public void deliverError(VolleyError error) {
                super.deliverError(error);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                return super.parseNetworkError(volleyError);
            }
        };

        queue.add(stringRequest);
        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
            @Override
            public void onRequestFinished(Request<Object> request) {

                loadmore_progress.setVisibility(View.GONE);
                offset = offset + 20;


            }
        });

    }
    private void NetworkDialog() {
        final Dialog dialogs = new Dialog(getContext());
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogs.setContentView(R.layout.networkdialog);
        dialogs.setCanceledOnTouchOutside(false);
        Button done = (Button) dialogs.findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogs.dismiss();
                (new AsyncCallWS()).execute();
            }
        });
        dialogs.show();
    }
}