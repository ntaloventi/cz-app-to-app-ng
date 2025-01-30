package cool.iqbal.bridgeapi;

import com.google.gson.annotations.SerializedName;

public class PaymentRequest {
    @SerializedName("pos_request_type")
    private String posReqType;
    @SerializedName("request_id")
    private String requestId;
    @SerializedName("client_id")
    private String clientId;
    @SerializedName("device_user")
    private String deviceUser;
    @SerializedName("payment_method")
    private String paymentMethod;
    @SerializedName("invoice_number")
    private String invoiceNumber;
    @SerializedName("amount")
    private Long amount;
    @SerializedName("status")
    private String status;
    @SerializedName("request_at")
    private String requestAt;

    public String getPosReqType() {
        return posReqType;
    }

    public void setPosReqType(String posReqType) {
        this.posReqType = posReqType;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getDeviceUser() {
        return deviceUser;
    }

    public void setDeviceUser(String deviceUser) {
        this.deviceUser = deviceUser;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestAt() {
        return requestAt;
    }

    public void setRequestAt(String requestAt) {
        this.requestAt = requestAt;
    }
}
