package com.homefood.restaurant.model.product;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;


public class Prices implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Prices> CREATOR = new Parcelable.Creator<Prices>() {
        @Override
        public Prices createFromParcel(Parcel in) {
            return new Prices(in);
        }

        @Override
        public Prices[] newArray(int size) {
            return new Prices[size];
        }
    };
    @SerializedName("price")
    private String price;
    @SerializedName("discount")
    private String discount;
    @SerializedName("currency")
    private String currency;
    @SerializedName("id")
    private int id;
    @SerializedName("discount_type")
    private String discountType;
    @SerializedName("orignal_price")
    private Double orignalPrice;

    protected Prices(Parcel in) {
        price = in.readString();
        discount = in.readString();
        currency = in.readString();
        id = in.readInt();
        discountType = in.readString();
        orignalPrice = in.readDouble();
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public Double getOrignalPrice() {
        return orignalPrice;
    }

    public void setOrignalPrice(Double orignalPrice) {
        this.orignalPrice = orignalPrice;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(price);
        dest.writeString(discount);
        dest.writeString(currency);
        dest.writeInt(id);
        dest.writeString(discountType);
        dest.writeDouble(orignalPrice);
    }
}