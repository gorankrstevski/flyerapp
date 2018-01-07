package com.cuponation.android.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by goran on 3/2/17.
 */

public class UniqueCode {


    private String code;
    private String customerKey;
    private String idClient;
    @SerializedName("id_voucher_code")
    private String idVoucherCode;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCustomerKey() {
        return customerKey;
    }

    public void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public String getIdVoucherCode() {
        return idVoucherCode;
    }

    public void setIdVoucherCode(String idVoucherCode) {
        this.idVoucherCode = idVoucherCode;
    }
}
