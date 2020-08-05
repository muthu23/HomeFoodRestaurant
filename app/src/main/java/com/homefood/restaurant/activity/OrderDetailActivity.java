package com.homefood.restaurant.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.homefood.restaurant.R;
import com.homefood.restaurant.adapter.OrderFlowAdapter;
import com.homefood.restaurant.adapter.OrderProductAdapter;
import com.homefood.restaurant.helper.ConnectionHelper;
import com.homefood.restaurant.helper.CustomDialog;
import com.homefood.restaurant.helper.GlobalData;
import com.homefood.restaurant.model.Order;
import com.homefood.restaurant.model.OrderFlow;
import com.homefood.restaurant.model.ServerError;
import com.homefood.restaurant.model.ordernew.OrderResponse;
import com.homefood.restaurant.network.ApiClient;
import com.homefood.restaurant.network.ApiInterface;
import com.homefood.restaurant.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.homefood.restaurant.helper.GlobalData.ORDER_STATUS;

public class OrderDetailActivity extends AppCompatActivity {

    @BindView(R.id.back_img)
    ImageView backImg;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.user_img)
    CircleImageView userImg;
    @BindView(R.id.user_name)
    TextView userName;
    @BindView(R.id.address)
    TextView address;
    @BindView(R.id.payment_mode)
    TextView paymentMode;
    @BindView(R.id.call_img)
    ImageView callImg;
    @BindView(R.id.order_product_rv)
    RecyclerView orderProductRv;
    @BindView(R.id.sub_total)
    TextView subTotal;
    @BindView(R.id.service_tax)
    TextView service_tax;
    @BindView(R.id.promocode_amount)
    TextView promocode_amount;
    @BindView(R.id.tv_cgst)
    TextView tv_cgst;
    @BindView(R.id.tv_sgst)
    TextView tv_sgst;
    @BindView(R.id.delivery_charges)
    TextView deliveryCharges;
    @BindView(R.id.total)
    TextView total;
    @BindView(R.id.cancel_btn)
    Button cancelBtn;
    @BindView(R.id.accept_btn)
    Button acceptBtn;
    @BindView(R.id.button_lay)
    LinearLayout buttonLay;
    @BindView(R.id.dispute_btn)
    Button disputeBtn;
    Handler handler;
    Runnable orderStatusRunnable;
    Order order;
    OrderProductAdapter orderProductAdapter;

    Context context;
    Activity activity;
    ConnectionHelper connectionHelper;
    CustomDialog customDialog;
    boolean isInternet;
    ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
    String TAG = "OrderDetailActivity";
    @BindView(R.id.notes)
    TextView notes;
    @BindView(R.id.promocodeLayout)
    LinearLayout promocodeLayout;
    @BindView(R.id.discount)
    TextView discount;
    @BindView(R.id.order_flow_rv)
    RecyclerView orderFlowRv;
    OrderFlowAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        ButterKnife.bind(this);
        context = OrderDetailActivity.this;
        activity = OrderDetailActivity.this;
        connectionHelper = new ConnectionHelper(context);
        isInternet = connectionHelper.isConnectingToInternet();
        customDialog = new CustomDialog(context);

        backImg.setVisibility(View.VISIBLE);

        if (GlobalData.selectedOrder != null) {
            order = GlobalData.selectedOrder;
        } else {
            finish();
            return;
        }

        title.setText("#" + order.getId());

        String name = order.getUser().getName();
        String payment_mode = order.getInvoice().getPaymentMode();

        //No minimum character limit in register screen.
        if (name.length() > 1)
            name = name.substring(0, 1).toUpperCase() + name.substring(1);

        payment_mode = payment_mode.substring(0, 1).toUpperCase() + payment_mode.substring(1);

        userName.setText(name);
        address.setText(order.getAddress().getMapAddress());
        paymentMode.setText(payment_mode);

        if (order.getNote() != null)
            notes.setText(order.getNote());
        else
            notes.setText(getResources().getString(R.string.empty));

        orderProductAdapter = new OrderProductAdapter(context, order.getItems());
        orderProductRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        orderProductRv.setHasFixedSize(true);
        orderProductRv.setAdapter(orderProductAdapter);

      /*  Double cgst_percentage_multiplayer = (Double.parseDouble(order.getInvoice().getCGST())/100);
        Double sgst_percentage_multiplayer = (Double.parseDouble(order.getInvoice().getSGST())/100);*/

        double cgst_percentage_multiplayer = order.getInvoice().getCGST() / 100;
        double sgst_percentage_multiplayer = order.getInvoice().getSGST() / 100;

        double gross_amount = order.getInvoice().getGross() - order.getInvoice().getDiscount();

        double cgst = (gross_amount * (cgst_percentage_multiplayer));
        double sgst = (gross_amount * (sgst_percentage_multiplayer));


        subTotal.setText(GlobalData.profile.getCurrency() + /*String.format("%.2f"*/order.getInvoice().getGross());
        discount.setText(GlobalData.profile.getCurrency() + /*String.format("%.2f"*/order.getInvoice().getDiscount());
        tv_cgst.setText(GlobalData.profile.getCurrency() + /*String.format("%.2f"*/cgst);
        tv_sgst.setText(GlobalData.profile.getCurrency() + /*String.format("%.2f"*/sgst);
        deliveryCharges.setText(GlobalData.profile.getCurrency() + /*String.format("%.2f"*/order.getInvoice().getDeliveryCharge());
        total.setText(GlobalData.profile.getCurrency() + /*String.format("%.2f"*/order.getInvoice().getNet());
        service_tax.setText(GlobalData.profile.getCurrency() + /*String.format("%.2f"*/order.getInvoice().getTax());

        if (order.getInvoice().getPromocode_amount() > 0) {
            promocodeLayout.setVisibility(View.VISIBLE);
        } else {
            promocodeLayout.setVisibility(View.GONE);
        }

        promocode_amount.setText(GlobalData.profile.getCurrency() + "-" +/*String.format("%.2f"*/(order.getInvoice().getPromocode_amount()));

        /*if(order.getStatus().equals("ORDERED")&&order.getDispute().equals("NODISPUTE")){
            disputeBtn.setVisibility(View.GONE);
            buttonLay.setVisibility(View.VISIBLE);
        }else{
            disputeBtn.setVisibility(View.VISIBLE);
            buttonLay.setVisibility(View.GONE);
        }*/
        if (connectionHelper.isConnectingToInternet()) {
            handler = new Handler();
            getParticularOrders(order.getId());
            orderStatusRunnable = new Runnable() {
                public void run() {
                    getParticularOrders(order.getId());
                    handler.postDelayed(this, 5000);
                }
            };
            handler.postDelayed(orderStatusRunnable, 5000);
        } else {
            Utils.displayMessage(this, getString(R.string.oops_no_internet));
        }
    }

    private void setOrderFlowAdapter() {
        List<OrderFlow> orderFlowList = new ArrayList<>();
        orderFlowList.add(new OrderFlow(getString(R.string.order_placed), getString(R.string.description_1), R.drawable.ic_order_placed, ORDER_STATUS.get(0)));
        orderFlowList.add(new OrderFlow(getString(R.string.order_confirmed), getString(R.string.description_2), R.drawable.ic_order_confirmed, ORDER_STATUS.get(1)));
        orderFlowList.add(new OrderFlow(getString(R.string.order_processed), getString(R.string.description_3), R.drawable.ic_order_processed, ORDER_STATUS.get(2) + ORDER_STATUS.get(3) + ORDER_STATUS.get(4)));
        orderFlowList.add(new OrderFlow(getString(R.string.order_pickedup), getString(R.string.description_4), R.drawable.ic_order_picked_up, ORDER_STATUS.get(5) + ORDER_STATUS.get(6)));
        orderFlowList.add(new OrderFlow(getString(R.string.order_delivered), getString(R.string.description_5), R.drawable.ic_order_delivered, ORDER_STATUS.get(7)));

        LinearLayoutManager manager = new LinearLayoutManager(this);
        orderFlowRv.setLayoutManager(manager);
        adapter = new OrderFlowAdapter(orderFlowList, this);
        orderFlowRv.setAdapter(adapter);
        orderFlowRv.setHasFixedSize(false);
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(this, R.anim.item_animation_slide_right);
        orderFlowRv.setLayoutAnimation(controller);
        orderFlowRv.scheduleLayoutAnimation();
    }


    @OnClick({R.id.back_img, R.id.call_img, R.id.cancel_btn, R.id.accept_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back_img:
                onBackPressed();
                break;
            case R.id.call_img:
                Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + order.getUser().getPhone()));
                if (dialIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(dialIntent);
                else
                    Utils.displayMessage(this, "Call feature not supported");
                break;
            case R.id.cancel_btn:
                AlertDialog.Builder cancelAlert = new AlertDialog.Builder(this);
                cancelAlert.setTitle(getResources().getString(R.string.order));
                cancelAlert.setMessage(getResources().getString(R.string.are_you_sure_want_to_cancel_the_order));
                cancelAlert.setPositiveButton(getResources().getString(R.string.okay), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("status", "CANCELLED");
                        updateOrderStatus(map);
                    }
                });
                cancelAlert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                        dialog.dismiss();
                    }
                });
                cancelAlert.show();
                break;
            case R.id.accept_btn:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.order_delivery_time, null);
                final EditText edittext = alertLayout.findViewById(R.id.edit_text);
                alert.setTitle(getResources().getString(R.string.order_delivery_time));
                alert.setView(alertLayout);
                alert.setPositiveButton(getResources().getString(R.string.okay), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String deliveryTime = edittext.getText().toString();
                        if (deliveryTime.isEmpty()) {
                            Toast.makeText(context, getResources().getString(R.string.please_enter_delivery_time), Toast.LENGTH_SHORT).show();
                        } else {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("status", "RECEIVED");
                            map.put("order_ready_time", deliveryTime);
                            updateOrderStatus(map);
                        }
                    }
                });

                alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                        dialog.dismiss();
                    }
                });
                alert.show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(orderStatusRunnable);
    }

    private void getParticularOrders(int order_id) {
        Call<OrderResponse> call = apiInterface.getParticularOrders(order_id);
        call.enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
                if (response.isSuccessful()) {
                    GlobalData.isselectedOrder = response.body().getOrder();
                    Log.i("isSelectedOrder : ", GlobalData.selectedOrder.toString());
                    if (adapter == null)
                        setOrderFlowAdapter();
                    else
                        adapter.notifyDataSetChanged();
                } else {
                    try {

                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(context, jObjError.optString("message"), Toast.LENGTH_LONG).show();
                        if (response.code() == 401) {
                            startActivity(new Intent(OrderDetailActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        }
                    } catch (Exception e) {
//                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderResponse> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }


    private void updateOrderStatus(HashMap<String, String> map) {
        customDialog.show();
        Call<Order> call = apiInterface.updateOrderStatus(GlobalData.selectedOrder.getId(), map);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(@NonNull Call<Order> call, Response<Order> response) {
                customDialog.dismiss();
                if (response.isSuccessful()) {
                    finish();
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
            public void onFailure(@NonNull Call<Order> call, Throwable t) {
                customDialog.dismiss();
                Utils.displayMessage(activity, getString(R.string.something_went_wrong));
            }
        });
    }

}
