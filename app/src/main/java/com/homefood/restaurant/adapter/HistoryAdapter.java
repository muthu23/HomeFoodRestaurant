package com.homefood.restaurant.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.homefood.restaurant.R;
import com.homefood.restaurant.activity.OrderDetailActivity;
import com.homefood.restaurant.helper.GlobalData;
import com.homefood.restaurant.model.Order;
import com.homefood.restaurant.utils.Utils;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {
    Context context;
    Activity activity;
    private List<Order> list;

    public HistoryAdapter(List<Order> list, Context con) {
        this.list = list;
        this.context = con;
        this.activity = activity;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Order order = list.get(position);
        holder.address.setText(order.getAddress().getMapAddress());
        holder.price.setText(/*context.getString(R.string.currency_value)*/GlobalData.profile.getCurrency() + "" + order.getInvoice().getNet());

        //Default Status and color
        String status = context.getResources().getString(R.string.dispute_created);
        holder.status.setTextColor(ContextCompat.getColor(context, R.color.colorRed));

        if (order.getDispute().equalsIgnoreCase("CREATED")) {
            holder.status.setText(status);
        } else if (order.getStatus().equals("CANCELLED") || order.getStatus().equals("COMPLETED") ) {
            status = "";
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.colorGreen));
            holder.status.setText(status);

        } else {
            status = context.getResources().getString(R.string.status_ongoing);
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.colorGreen));
            holder.status.setText(status);
        }

        String name = Utils.toFirstCharUpperAll(order.getUser().getName());
        String payment_mode = Utils.toFirstCharUpperAll(order.getInvoice().getPaymentMode());

        holder.userName.setText(name);
        holder.paymentMode.setText(payment_mode);

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalData.selectedOrder = list.get(position);
                context.startActivity(new Intent(context, OrderDetailActivity.class));
            }
        });
        Glide.with(context).load(order.getUser().getAvatar())
                .apply(new RequestOptions().placeholder(R.drawable.ic_place_holder_image).error(R.drawable.ic_place_holder_image).dontAnimate()).into(holder.userImg);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void add(Order item, int position) {
        list.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(Order item) {
        int position = list.indexOf(item);
        list.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public void setList(List<Order> list) {
        this.list = list;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView userName, address, paymentMode, price, status;
        CardView itemLayout;
        ImageView userImg;

        public MyViewHolder(View view) {
            super(view);
            userName = view.findViewById(R.id.user_name);
            price = view.findViewById(R.id.price);
            address = view.findViewById(R.id.address);
            paymentMode = view.findViewById(R.id.payment_mode);
            status = view.findViewById(R.id.status);
            itemLayout = view.findViewById(R.id.item_layout);
            userImg = view.findViewById(R.id.user_img);
        }
    }

}
