package cool.iqbal.bridgeapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BridgeApi {
    private static BridgeApiBuilder bridgeApiBuilder;
    public static BridgeApi initBridge(BridgeListener listener, AppCompatActivity activity){
        if (bridgeApiBuilder == null){
            bridgeApiBuilder = new BridgeApiBuilder();
        }
        bridgeApiBuilder.initBridge(activity);
        bridgeApiBuilder.setListener(listener);
        return bridgeApiBuilder.bridgeApi;
    }

    public BridgeApi withHost(String host){
        if (bridgeApiBuilder == null){
            bridgeApiBuilder = new BridgeApiBuilder();
        }
        bridgeApiBuilder.setHost(host);
        return bridgeApiBuilder.bridgeApi;
    }

    public BridgeApi withApiKey(String apiKey){
        if (bridgeApiBuilder == null){
            bridgeApiBuilder = new BridgeApiBuilder();
        }
        bridgeApiBuilder.setApiKey(apiKey);
        return bridgeApiBuilder.bridgeApi;
    }

    public void attemptRequestPayment(PaymentRequest paymentRequest){
        bridgeApiBuilder.attemptRequestPayment(paymentRequest);
    }

    private static class BridgeApiBuilder {
        private BridgeApi bridgeApi;
        private AppCompatActivity activity;
        private BridgeListener listener;
        private String host;
        private String apiKey;

        public void initBridge(AppCompatActivity activity) {
            bridgeApi = new BridgeApi();
            this.activity = activity;
        }

        public void setListener(BridgeListener listener) {
            this.listener = listener;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public void attemptRequestPayment(PaymentRequest paymentRequest) {
            Retrofit retrofit = new Retrofit.Builder().baseUrl(host).addConverterFactory(GsonConverterFactory.create()).build();
            ApiService apiService = retrofit.create(ApiService.class);
            Call<ResponseBody> apiCall = apiService.requestPayment(paymentRequest);
            apiCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()){
                        try {
                            String responseBody = Objects.requireNonNull(response.body()).string();
                            listener.onPosted(responseBody);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        listener.onError("Error: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    listener.onError("Failure: " +t.getMessage());
                }
            });
        }
    }

    public interface BridgeListener {
        void onPosted(String responseBody);
        void onError(String error);
    }
}
