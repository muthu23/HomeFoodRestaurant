package com.homefood.restaurant.fragment;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.homefood.restaurant.activity.LoginActivity;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.homefood.restaurant.R;
import com.homefood.restaurant.adapter.RequestAdapter;
import com.homefood.restaurant.controller.GetProfile;
import com.homefood.restaurant.controller.ProfileListener;
import com.homefood.restaurant.helper.ConnectionHelper;
import com.homefood.restaurant.helper.CustomDialog;
import com.homefood.restaurant.helper.GlobalData;
import com.homefood.restaurant.model.IncomingOrders;
import com.homefood.restaurant.model.Order;
import com.homefood.restaurant.model.Profile;
import com.homefood.restaurant.model.ServerError;
import com.homefood.restaurant.network.ApiClient;
import com.homefood.restaurant.network.ApiInterface;
import com.homefood.restaurant.utils.Constants;
import com.homefood.restaurant.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements ProfileListener {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;
    @BindView(R.id.incoming_rv)
    RecyclerView incomingRv;
    @BindView(R.id.activity_main)
    CoordinatorLayout activityMain;
    Unbinder unbinder;
    RequestAdapter requestAdapter;
    List<Order> orderList;
    Context context;
    Activity activity;
    ConnectionHelper connectionHelper;
    CustomDialog customDialog;
    boolean isInternet;
    ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
    String TAG = "HomeFragment";
    String email, password;
    @BindView(R.id.shop_img)
    ImageView shopImg;
    @BindView(R.id.shop_name)
    TextView shopName;
    @BindView(R.id.shop_address)
    TextView shopAddress;
    @BindView(R.id.llNoRecords)
    LinearLayout llNoRecords;
    @BindView(R.id.resturant_rating)
    AppCompatRatingBar resturant_rating;
    private Handler homeHandler = new Handler();
    private boolean isVisible = true;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isInternet) {
                // getIncomingOrders();
            } else {
                Utils.displayMessage(activity, getString(R.string.oops_no_internet));
            }
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        orderList = new ArrayList<>();
        context = getContext();
        activity = getActivity();
        connectionHelper = new ConnectionHelper(context);
        isInternet = connectionHelper.isConnectingToInternet();
        customDialog = new CustomDialog(context);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) scrollRange = appBarLayout.getTotalScrollRange();
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    collapsingToolbar.setTitle(GlobalData.profile.getName());
                } else if (isShow) {
                    isShow = false;
                    collapsingToolbar.setTitle("");
                }
            }
        });

       /* isVisible = true;
        getProfile();
        getIncomingOrders();
        homeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isInternet) {
                    if (isVisible && incomingRv != null) {
                        getIncomingOrders();
                        homeHandler.postDelayed(this, 3000);
                    }
                }
            }
        }, 3000);*/


      /*  LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(Constants.BROADCAST.UPDATE_ORDERS));*/

    }

    private void updateUI(Profile profile) {
        if (profile != null && profile.getDefaultBanner() != null)
            Glide.with(context)
                    .load(profile.getDefaultBanner())
                    .apply(new RequestOptions()
                            .centerCrop()
                            .placeholder(R.drawable.ic_place_holder_image)
                            .error(R.drawable.ic_place_holder_image).dontAnimate())
                    .into(shopImg);
        if (profile != null && profile.getName() != null)
            shopName.setText(profile.getName());
        if (profile != null && profile.getAddress() != null)
            shopAddress.setText(profile.getMapsAddress());
        if (profile != null && profile.getRating() != null)
            resturant_rating.setRating(profile.getRating());
    }


    private void prepareAdapter() {
        if (incomingRv != null) {
            requestAdapter = new RequestAdapter(orderList, context);
            incomingRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            incomingRv.setHasFixedSize(true);
            incomingRv.setAdapter(requestAdapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isVisible = true;
        getIncomingOrders();
        homeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isInternet) {
                    if (isVisible && incomingRv != null) {
                        getIncomingOrders();
                        homeHandler.postDelayed(this, 3000);
                    }
                }
            }
        }, 3000);
        getProfile();


        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(Constants.BROADCAST.UPDATE_ORDERS));
    }

    private void getProfile() {
        if (connectionHelper.isConnectingToInternet()) {
            new GetProfile(apiInterface, this);
        }
    }

   /* @Override
    public void onPause() {
        super.onPause();
        isVisible = false;
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }*/


    private void getIncomingOrders() {
        //customDialog.show();
        Call<IncomingOrders> call = apiInterface.getIncomingOrders("ordered");
        call.enqueue(new Callback<IncomingOrders>() {
            @Override
            public void onResponse(Call<IncomingOrders> call, Response<IncomingOrders> response) {
                customDialog.dismiss();
                if (response.isSuccessful()) {
                    if (response.body().getOrders() != null &&
                            !response.body().getOrders().isEmpty() && response.body().getOrders().size() > 0) {
                        if (incomingRv != null && llNoRecords != null) {
                            incomingRv.setVisibility(View.VISIBLE);
                            llNoRecords.setVisibility(View.GONE);
                        }
                        orderList.clear();
                        orderList.addAll(response.body().getOrders());
                        if (requestAdapter == null) {
                            prepareAdapter();
                        } else {
                            requestAdapter.notifyDataSetChanged();
                        }
                    } else {
                        if (incomingRv != null && llNoRecords != null) {
                            incomingRv.setVisibility(View.GONE);
                            llNoRecords.setVisibility(View.VISIBLE);
                        }
                    }

                } else {
                    Gson gson = new Gson();
                    try {
                        ServerError serverError = gson.fromJson(response.errorBody().charStream(), ServerError.class);
                        Utils.displayMessage(activity, serverError.getError());
                        if (response.code() == 401) {
                            context.startActivity(new Intent(context, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            activity.finish();
                        }
                    } catch (JsonSyntaxException e) {
//                        Utils.displayMessage(activity, getString(R.string.something_went_wrong));
                    }
                }
            }

            @Override
            public void onFailure(Call<IncomingOrders> call, Throwable t) {
                customDialog.dismiss();
//                Utils.displayMessage(activity, getString(R.string.something_went_wrong));
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onSuccess(Profile profile) {
        try {
            updateUI(profile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onFailure(String error) {
        Log.e(TAG, error);
    }
}
