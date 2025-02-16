package cool.iqbal.bridgeapi;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.here.oksse.OkSse;
import com.here.oksse.ServerSentEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
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

    public BridgeApi useWebSockeT(Boolean useWs){
        if (bridgeApiBuilder == null){
            bridgeApiBuilder = new BridgeApiBuilder();
        }
        bridgeApiBuilder.useWebSocket(useWs);
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
        private Boolean useWs;
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

        public void useWebSocket(Boolean useWs){
            this.useWs = useWs;
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
                            if (useWs){
                                subscribeWithWebSocket(paymentRequest);
                            } else {
                                subscribeWithSse(paymentRequest);
                            }
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

        private void  subscribeWithSse(PaymentRequest paymentRequest){
            try {
                String subscribeUrl = host + "/MmBridgeApi/v1/payment-subscribe";
                Map<String, String> queryData = new HashMap<>();
                queryData.put("client_id", paymentRequest.getClientId());
                queryData.put("request_id", paymentRequest.getRequestId());
                queryData.put("device_user", paymentRequest.getDeviceUser());

                String hostUrl = subscribeUrl + "?" + Reused.mapToQueryParam(queryData);
                //listener.onInfo(hostUrl);

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

        private WebSocket webSocket;
        private void subscribeWithWebSocket(PaymentRequest paymentRequest){
            Gson gson = new Gson();
            String bodyMessage = gson.toJson(paymentRequest);

            String subscribeUrl = host.replace("https", "ws") + "MmBridgeApi/v1/ws/payment-subscribe";
            //listener.onInfo(subscribeUrl);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(subscribeUrl).build();

            if (webSocket != null){
                webSocket.cancel();
            }
            webSocket = client.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
                    webSocket.send(bodyMessage);
                }

                @Override
                public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                    if (text.equals("ping")){

                    } else if (text.equals("subscribed") || text.equals("acknowledge")){
                        listener.onInfo(text);
                    } else {
                        listener.onWsEvent(text);
                        listener = null;
                        webSocket.cancel();
                    }
                }

                @Override
                public void onFailure(@NonNull WebSocket webSocket, Throwable t, okhttp3.Response response) {
                    if (listener != null){
                        listener.onError(t.getMessage());
                    }
                }

                @Override
                public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                    if (listener != null){
                        listener.onError(reason);
                    }
                    webSocket.close(1000, null);
                }

                @Override
                public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                    if (listener != null){
                        listener.onError(reason);
                    }
                }
            });
        }
    }

    public interface BridgeListener {
        void onPosted(String responseBody);
        void onError(String error);
        void onSseEvent(String eventType, String data);
        void onWsEvent(String message);
        void onInfo(String info);
    }
}
