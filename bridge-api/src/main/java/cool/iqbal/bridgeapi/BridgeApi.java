package cool.iqbal.bridgeapi;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.here.oksse.OkSse;
import com.here.oksse.ServerSentEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Request;
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

    private static class BridgeApiBuilder implements ServerSentEvent.Listener {
        private BridgeApi bridgeApi;
        private AppCompatActivity activity;
        private BridgeListener listener;
        private String host;
        private String apiKey;
        private ApiService apiService;
        private ExecutorService executor = Executors.newSingleThreadExecutor();

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
            apiService = retrofit.create(ApiService.class);
            Call<ResponseBody> apiCall = apiService.requestPayment(paymentRequest);
            apiCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()){
                        try {
                            String responseBody = Objects.requireNonNull(response.body()).string();
                            listener.onPosted(responseBody);

                            //start listening server event
                            subscribeSseEvent(paymentRequest);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        listener.onError(String.valueOf(response.code()));
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    listener.onError(t.getMessage());
                }
            });
        }

        private void  subscribeSseEvent(PaymentRequest paymentRequest){
            try {
                //String baseUrl = "http://192.168.100.6:8000/MmBridgeApi/v1/payment-subscribe"; // local lan [SUCCESS]
                String baseUrl = "https://learning-cat-saving.ngrok-free.app/MmBridgeApi/v1/payment-subscribe"; // ngrok [SUCCESS]
                Map<String, String> queryData = new HashMap<>();
                queryData.put("client_id", paymentRequest.getClientId());
                queryData.put("request_id", paymentRequest.getRequestId());
                queryData.put("device_user", paymentRequest.getDeviceUser());

                String hostUrl = baseUrl + "?" + Reused.mapToQueryParam(queryData);
                listener.onInfo(hostUrl);

                Request request = new Request.Builder().url(hostUrl).build();
                OkSse okSse = new OkSse();
                ServerSentEvent sse = okSse.newServerSentEvent(request, this);
                //sse.close();
            } catch (Exception e){
                listener.onError(e.getMessage());
            }
        }

        @Override
        public void onOpen(ServerSentEvent sse, okhttp3.Response response) {
            //listener.onInfo("onOpen called");
        }

        @Override
        public void onMessage(ServerSentEvent sse, String id, String event, String message) {
            listener.onSseEvent(event, message);
            if (event.equals("update") || event.equals("error")){
                sse.close();
            }
        }

        @Override
        public void onComment(ServerSentEvent sse, String comment) {
            listener.onInfo(comment);
        }

        @Override
        public boolean onRetryTime(ServerSentEvent sse, long milliseconds) {
            return false;
        }

        @Override
        public boolean onRetryError(ServerSentEvent sse, Throwable throwable, okhttp3.Response response) {
            return false;
        }

        @Override
        public void onClosed(ServerSentEvent sse) {

        }

        @Override
        public Request onPreRetry(ServerSentEvent sse, Request originalRequest) {
            return null;
        }
    }

    public interface BridgeListener {
        void onPosted(String responseBody);
        void onError(String error);
        void onSseEvent(String eventType, String data);
        void onInfo(String info);
    }
}
