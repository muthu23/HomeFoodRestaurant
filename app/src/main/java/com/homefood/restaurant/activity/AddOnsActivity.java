package com.homefood.restaurant.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.homefood.restaurant.R;
import com.homefood.restaurant.adapter.AddOnsAdapter;
import com.homefood.restaurant.helper.ConnectionHelper;
import com.homefood.restaurant.helper.CustomDialog;
import com.homefood.restaurant.model.Addon;
import com.homefood.restaurant.model.ServerError;
import com.homefood.restaurant.network.ApiClient;
import com.homefood.restaurant.network.ApiInterface;
import com.homefood.restaurant.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddOnsActivity extends AppCompatActivity {

    @BindView(R.id.back_img)
    ImageView backImg;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.add_ons_rv)
    RecyclerView addOnsRv;
    @BindView(R.id.add_add_ons_btn)
    Button addAddOnsBtn;

    @BindView(R.id.llNoRecords)
    LinearLayout llNoRecords;

    Context context;
    Activity activity;
    ConnectionHelper connectionHelper;
    CustomDialog customDialog;
    ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
    String TAG = "AddOnsActivity";
    boolean isInternet;

    List<Addon> addOnsList;
    AddOnsAdapter addOnsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ons);
        ButterKnife.bind(this);
        title.setText(getString(R.string.add_ons_list));
        backImg.setVisibility(View.VISIBLE);
        context = AddOnsActivity.this;
        activity = AddOnsActivity.this;
        connectionHelper = new ConnectionHelper(context);
        isInternet = connectionHelper.isConnectingToInternet();
        customDialog = new CustomDialog(context);

        addOnsList = new ArrayList<>();

    }

    private void setUpAdapter() {
        if (addOnsAdapter == null) {
            addOnsAdapter = new AddOnsAdapter(addOnsList, context);
            addOnsRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            addOnsRv.setHasFixedSize(true);
            addOnsRv.setAdapter(addOnsAdapter);
        } else {
            addOnsAdapter.notifyDataSetChanged();
        }

        if (addOnsList.size() > 0) {
            llNoRecords.setVisibility(View.GONE);
            addOnsRv.setVisibility(View.VISIBLE);
        } else {
            llNoRecords.setVisibility(View.VISIBLE);
            addOnsRv.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isInternet)
            getAddons();
        else
            Utils.displayMessage(activity, getString(R.string.oops_no_internet));

    }

    private void getAddons() {
        customDialog.show();
        Call<List<Addon>> call = apiInterface.getAddons();
        call.enqueue(new Callback<List<Addon>>() {
            @Override
            public void onResponse(Call<List<Addon>> call, Response<List<Addon>> response) {
                customDialog.dismiss();
                if (response.isSuccessful()) {
                    addOnsList.clear();
                    addOnsList.addAll(response.body());
                    setUpAdapter();

                } else {
                    Gson gson = new Gson();
                    try {
                        ServerError serverError = gson.fromJson(response.errorBody().charStream(), ServerError.class);
                        Utils.displayMessage(activity, serverError.getError());
                    } catch (JsonSyntaxException e) {
                        Utils.displayMessage(activity, getString(R.string.something_went_wrong));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Addon>> call, Throwable t) {
                customDialog.dismiss();
                Utils.displayMessage(activity, getString(R.string.something_went_wrong));
            }
        });

    }

    @OnClick({R.id.back_img, R.id.add_add_ons_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back_img:
                onBackPressed();
                break;
            case R.id.add_add_ons_btn:
                startActivity(new Intent(context, AddAddOnsActivity.class));
                break;
        }
    }

    public void deleteAddon(final Addon addon, Integer position) {
        customDialog.show();
        Call<List<Addon>> call = apiInterface.deleteAddon(addon.getId());
        call.enqueue(new Callback<List<Addon>>() {
            @Override
            public void onResponse(@NonNull Call<List<Addon>> call, @NonNull Response<List<Addon>> response) {
                customDialog.dismiss();
                if (response.isSuccessful()) {
                    addOnsAdapter.remove(addon);
                } else {
                    Gson gson = new Gson();
                    try {
                        ServerError serverError = gson.fromJson(response.errorBody().charStream(), ServerError.class);
                        Utils.displayMessage(activity, serverError.getError());
                        if (response.code() == 401)
                        {context.startActivity(new Intent(context, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            activity.finish();}
                    } catch (JsonSyntaxException e) {
                        Utils.displayMessage(activity, getString(R.string.something_went_wrong));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Addon>> call, @NonNull Throwable t) {
                customDialog.dismiss();
                Utils.displayMessage(activity, getString(R.string.something_went_wrong));
            }
        });


    }

}
