package com.homefood.restaurant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.homefood.restaurant.R;
import com.homefood.restaurant.helper.ConnectionHelper;
import com.homefood.restaurant.helper.CustomDialog;
import com.homefood.restaurant.model.ForgotPasswordResponse;
import com.homefood.restaurant.model.ServerError;
import com.homefood.restaurant.network.ApiClient;
import com.homefood.restaurant.network.ApiInterface;
import com.homefood.restaurant.utils.Utils;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.homefood.restaurant.utils.TextUtils.isValidEmail;

public class ForgotPassword extends AppCompatActivity {

    @BindView(R.id.et_email)
    EditText etEmail;

    @BindView(R.id.next_btn)
    Button btnNext;

    @BindView(R.id.txt_register)
    TextView txtRegister;

    ConnectionHelper connectionHelper;
    CustomDialog customDialog;


    ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
    String strEmail;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);
        ButterKnife.bind(this);

        setUp();
    }

    private void setUp() {
        connectionHelper = new ConnectionHelper(this);
        customDialog = new CustomDialog(this);
    }

    @OnClick({R.id.next_btn, R.id.txt_register})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.next_btn:
                if (validateInput()) {
                    if (connectionHelper.isConnectingToInternet()) {
                        HashMap<String, String> params = new HashMap<>();
                        params.put("email", strEmail);
                        getOTP(params);
                    } else
                        Utils.displayMessage(this, getString(R.string.oops_no_internet));
                }
                break;

            case R.id.txt_register:
                startActivity(new Intent(this, RegisterActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
        }
    }

    private void getOTP(HashMap<String, String> map) {
        customDialog.show();
        Call<ForgotPasswordResponse> call = apiInterface.forgotPassword(map);
        call.enqueue(new Callback<ForgotPasswordResponse>() {
            @Override
            public void onResponse(Call<ForgotPasswordResponse> call, Response<ForgotPasswordResponse> response) {
                customDialog.dismiss();
                if (response.isSuccessful()) {
//                    Utils.displayMessage(ForgotPassword.this, response.body().getMessage());
                    Utils.displayMessage(ForgotPassword.this, getString(R.string.forgot_email_send));

                    new Handler(getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            redirectToOtpScreen();
                            onBackPressed();
                        }
                    }, 1500);
                } else {
                    try {
                        if (response.code() == 422) {
                            Utils.displayMessage(ForgotPassword.this, getString(R.string.invalid_email));
                        } else {
                            ServerError serverError = new Gson().fromJson(response.errorBody().charStream(), ServerError.class);
                            Utils.displayMessage(ForgotPassword.this, getString(R.string.something_went_wrong));
                        }

                    } catch (JsonSyntaxException e) {
                        Utils.displayMessage(ForgotPassword.this, getString(R.string.something_went_wrong));
                    }

                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordResponse> call, Throwable t) {
                customDialog.dismiss();
                Utils.displayMessage(ForgotPassword.this, getString(R.string.something_went_wrong));
            }
        });
    }

    private void redirectToOtpScreen() {
        Intent intent = new Intent(this, OtpActivity.class);
        intent.putExtra("email", strEmail);
        startActivity(intent);
        //finish();
    }

    private boolean validateInput() {
        strEmail = etEmail.getText().toString().trim();
        if (strEmail.isEmpty()) {
            Utils.displayMessage(this, getString(R.string.please_enter_mail_id));
            return false;
        } else if (!isValidEmail(strEmail)) {
            Utils.displayMessage(this, getResources().getString(R.string.please_enter_valid_mail_id));
            return false;
        }
        return true;
    }

}
