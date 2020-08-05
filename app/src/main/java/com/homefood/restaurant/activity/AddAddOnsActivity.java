package com.homefood.restaurant.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.homefood.restaurant.helper.SharedHelper;
import com.homefood.restaurant.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.homefood.restaurant.R;
import com.homefood.restaurant.helper.ConnectionHelper;
import com.homefood.restaurant.helper.CustomDialog;
import com.homefood.restaurant.helper.GlobalData;
import com.homefood.restaurant.model.Addon;
import com.homefood.restaurant.model.ServerError;
import com.homefood.restaurant.network.ApiClient;
import com.homefood.restaurant.network.ApiInterface;
import com.homefood.restaurant.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAddOnsActivity extends AppCompatActivity {


    @BindView(R.id.back_img)
    ImageView backImg;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.et_addons_name)
    EditText etAddonsName;
    @BindView(R.id.save_btn)
    Button saveBtn;

    Context context = AddAddOnsActivity.this;
    Activity activity;
    ConnectionHelper connectionHelper;
    CustomDialog customDialog;
    ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
    String TAG = "AddAddOnsActivity";
    boolean isInternet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_add_ons);
        ButterKnife.bind(this);
        if (getIntent().getBooleanExtra("is_update", false)) {
            title.setText(getString(R.string.update_add_ons));
            Addon addon = GlobalData.selectedAddon;
            if (addon != null && addon.getName() != null &&
                    !addon.getName().equalsIgnoreCase("null") && addon.getName().length() > 0) {
                etAddonsName.setText(addon.getName());
            }
        } else
            title.setText(getString(R.string.create_add_ons));
        backImg.setVisibility(View.VISIBLE);
        context = AddAddOnsActivity.this;
        activity = AddAddOnsActivity.this;
        connectionHelper = new ConnectionHelper(context);
        isInternet = connectionHelper.isConnectingToInternet();
        customDialog = new CustomDialog(context);


    }

    @OnClick({R.id.back_img, R.id.save_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back_img:
                onBackPressed();
                break;
            case R.id.save_btn:
                String name = etAddonsName.getText().toString();
                if (!name.isEmpty()) {
                    addAddOns(name);
                } else
                    Utils.displayMessage(activity, getResources().getString(R.string.please_enter_addons_name));
                break;
        }
    }

    private void addAddOns(String name) {
        customDialog.show();
        Call<Addon> call;
        String shop_id = SharedHelper.getKey(this, Constants.PREF.PROFILE_ID);
        if (getIntent().getBooleanExtra("is_update", false))
            call = apiInterface.updateAddon(GlobalData.selectedAddon.getId(), name,shop_id);
        else
            call = apiInterface.addAddon(name,shop_id);

        call.enqueue(new Callback<Addon>() {
            @Override
            public void onResponse(@NonNull Call<Addon> call, @NonNull Response<Addon> response) {
                customDialog.dismiss();
                if (response.isSuccessful()) {
                    if (getIntent().getBooleanExtra("is_update", false))
                        Toast.makeText(context, getResources().getString(R.string.addons_updated_successfully), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(context, getResources().getString(R.string.addons_added_successfully), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Gson gson = new Gson();
                    try {
                        ServerError serverError = gson.fromJson(response.errorBody().charStream(), ServerError.class);
                        Utils.displayMessage(activity, serverError.getError());
                        if (response.code() == 401) {
                            context.startActivity(new Intent(context, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        }
                    } catch (JsonSyntaxException e) {
                        Utils.displayMessage(activity, getString(R.string.something_went_wrong));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Addon> call, @NonNull Throwable t) {
                customDialog.dismiss();
                Utils.displayMessage(activity, getString(R.string.something_went_wrong));
            }
        });

    }

}
