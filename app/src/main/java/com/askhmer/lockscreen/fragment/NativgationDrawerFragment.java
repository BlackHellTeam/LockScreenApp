package com.askhmer.lockscreen.fragment;



import android.app.Fragment;
import android.os.Bundle;


import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.askhmer.lockscreen.R;
import com.askhmer.lockscreen.adapter.AdpterNewFeed;
import com.askhmer.lockscreen.model.VideoNewFeed;
import com.askhmer.lockscreen.network.JsonConverter;
import com.askhmer.lockscreen.network.MySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class NativgationDrawerFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<VideoNewFeed> videoNewFeeds = new ArrayList<>();
    private AdpterNewFeed adpterNewFeed;
    private int currentPage = 1;
    private String startRowId;
    private boolean loading = true;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private LinearLayout retryLinearLayout;
    private Button btnRetry;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nativgation_drawer, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        retryLinearLayout = (LinearLayout) view.findViewById(R.id.retry_layout);
        btnRetry = (Button) view.findViewById(R.id.btn_retry);

         /*set up recyler*/
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy > 0) //check for scroll down{
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            loading = false;
                            adpterNewFeed.isDisplayLoading(true);
                            requestLoadData(true);
                        }
                    }
                }

            });

        requestLoadData(false);

        return view;
    }

    /***
     * request new data to server
     * http://testpotal.medayi.com/funapi.php?rows=5&page=2
     */
    private void requestLoadData(final boolean isPagination) {
        String url = "";
        if (!isPagination) {
            url = "http://testpotal.medayi.com/funapi.php?rows=5&page="+ currentPage ++ ;
        }else {
            url = "http://testpotal.medayi.com/funapi.php?rows=5&page=" + currentPage ++ + "&start_row_id=" + startRowId;
        }
        Log.e("url_show", url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        boolean dataNotFound = false;
                        try {
                            JSONObject jsonObj = new JSONObject(response);
                            if (jsonObj.getString("STATUS").contains("200")) {
                                String data = jsonObj.getString("data");
                                videoNewFeeds = new JsonConverter().toArrayList(data, VideoNewFeed.class);
                            }else {
                                dataNotFound = true;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            if (!isPagination) {
                                startRowId = videoNewFeeds.get(0).getId();
                                Log.e("start_id", startRowId);
                                adpterNewFeed = new AdpterNewFeed(videoNewFeeds, getActivity());
                                recyclerView.setAdapter(adpterNewFeed);
                            }else {
                                if (dataNotFound) {
                                     adpterNewFeed.isDisplayLoading(false);
                                }else {
                                    adpterNewFeed.setData(videoNewFeeds);
                                    loading = true;
                                }
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isPagination == false) {
                    recyclerView.setVisibility(View.GONE);
                    retryLinearLayout.setVisibility(View.VISIBLE);
                    btnRetry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            recyclerView.setVisibility(View.VISIBLE);
                            retryLinearLayout.setVisibility(View.GONE);
                            requestLoadData(false);
                        }
                    });
                }
                loading = true;
            }
        });
        MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }
}
