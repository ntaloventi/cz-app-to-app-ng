package cool.iqbal.bridgeapi;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("MmBridgeApi/v1/payment-request")
    Call<ResponseBody> requestPayment(@Body PaymentRequest paymentRequest);
}
