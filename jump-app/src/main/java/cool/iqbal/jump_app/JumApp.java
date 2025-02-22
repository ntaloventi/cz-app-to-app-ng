package cool.iqbal.jump_app;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class JumApp {
    private static JumpBuilder jumpBuilder;

    public static JumApp initJumpApp(JumpListener listener, AppCompatActivity activity){
        if (jumpBuilder == null){
            jumpBuilder = new JumpBuilder();
        }
        jumpBuilder.initJumpApp(activity);
        jumpBuilder.setListener(listener);
        return jumpBuilder.jumApp;
    }

    public JumApp withData(MiniAtmRequest data){
        if (jumpBuilder == null){
            jumpBuilder = new JumpBuilder();
        }
        jumpBuilder.setData(data);
        return jumpBuilder.jumApp;
    }

    public void openApp(){
        jumpBuilder.attemptRequestNg(ActionName.Action_Open);
    }

    public void checkBalance(){
        jumpBuilder.attemptRequestNg(ActionName.Action_Check_Balance);
    }

    public void transfer(){
        jumpBuilder.attemptRequestNg(ActionName.Action_Transfer);
    }

    public void cashWithdraw(){
        jumpBuilder.attemptRequestNg(ActionName.Action_Cash_Withdrawal);
    }

    public void cashDeposit(){
        jumpBuilder.attemptRequestNg(ActionName.Action_Cash_Deposit);
    }

    private static class JumpBuilder {
        private JumApp jumApp;
        private AppCompatActivity activity;
        private MiniAtmRequest data;
        private JumpListener listener;
        private ActivityResultLauncher<Intent> nextLauncher;

        public void  initJumpApp(AppCompatActivity activity){
            jumApp = new JumApp();
            this.activity = activity;

            nextLauncher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK){
                    listener.onRawResult(result.getData());
                } else {
                    listener.onCancel();
                }
            });
        }

        public void setData(MiniAtmRequest data) {
            this.data = data;
        }

        public void setListener(JumpListener listener){
            this.listener = listener;
        }

        public void attemptRequestNg(ActionName actionName) {
            Intent intent = new Intent();
            StringBuilder sbUri = new StringBuilder();
            sbUri.append("cashlez://miniatm.");
            sbUri.append(actionName.getName());
            sbUri.append("?launcher=");
            sbUri.append(data.isLauncher());
            sbUri.append("&amount=");
            sbUri.append(data.getAmount());
            sbUri.append("&callback=");
            sbUri.append(data.getCallback());
            if (actionName.equals(ActionName.Action_Transfer)){
                sbUri.append("&beneBankCode=");
                sbUri.append(data.getBeneBankCode());
                sbUri.append("&beneAccountNo=");
                sbUri.append(data.getBeneAccountNo());
            }
            String strUri = sbUri.toString();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse(strUri));
            nextLauncher.launch(intent);
        }


    }

    public interface JumpListener {
        void onRawResult(Intent result);
        void onCancel();
    }
}
