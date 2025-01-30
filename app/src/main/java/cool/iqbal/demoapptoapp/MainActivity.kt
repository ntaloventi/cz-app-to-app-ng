package cool.iqbal.demoapptoapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cool.iqbal.bridgeapi.BridgeApi
import cool.iqbal.bridgeapi.BridgeApi.BridgeListener
import cool.iqbal.bridgeapi.PaymentRequest
import cool.iqbal.jumpapp.JumApp
import cool.iqbal.jumpapp.MiniAtmRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity(), JumApp.JumpListener, BridgeListener {

    enum class PaymentMethod {
        QRIS,
        CDCP
    }
    private var tvResult:TextView? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tvResult)

        val jumpApp = JumApp.initJumpApp(this, this)
        val data = MiniAtmRequest().apply {
            isLauncher = true
            callback = "bukalapak://czresult.data"
        }

        val btnCb: Button = findViewById(R.id.btnCb)
        btnCb.setOnClickListener {
            jumpApp.withData(data).checkBalance()
        }

        val btnTf: Button = findViewById(R.id.btnTf)
        btnTf.setOnClickListener {
            data.amount = 150000
            data.beneBankCode = "008"
            data.beneAccountNo = "15500088992"
            jumpApp.withData(data).transfer()
        }

        val btnWd: Button = findViewById(R.id.btnWd)
        btnWd.setOnClickListener {
            data.amount = 160000
            jumpApp.withData(data).cashWithdraw()
        }

        val btnDp: Button = findViewById(R.id.btnDp)
        btnDp.setOnClickListener {
            data.amount = 170000
            jumpApp.withData(data).cashDeposit()
        }

        /*payment request */

        val host = "https://learning-cat-saving.ngrok-free.app/MmBridgeApi/"
        val apiKey = "Some-Api-Key"
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateTime = formatter.format(Date())
        val bridgeApi = BridgeApi.initBridge(this, this).withHost(host).withApiKey(apiKey)
        val paymentDataRequest = PaymentRequest().apply {
            posReqType = "Kiosk"
            requestId = "Auto-Id"
            clientId = "Client-Id"
            deviceUser = "iqbal"
            invoiceNumber = "inv-001"
            amount = 10000
            status = "Pending"
            requestAt = currentDateTime
        }

        val btnCdcp:Button = findViewById(R.id.btnCdcp)
        btnCdcp.setOnClickListener{
            paymentDataRequest.paymentMethod = PaymentMethod.CDCP.name
            bridgeApi.attemptRequestPayment(paymentDataRequest)
        }

        val btnQris:Button = findViewById(R.id.btnQris)
        btnQris.setOnClickListener{
            paymentDataRequest.paymentMethod = PaymentMethod.QRIS.name
            bridgeApi.attemptRequestPayment(paymentDataRequest)
        }

    }

    override fun onRawResult(result: Intent) {
        val rawResult = uriToHashMap(result.data)
        Log.e("TAG", "onRawResult: $rawResult")
        runOnUiThread { tvResult?.text = rawResult.toString() }
    }

    override fun onCancel() {
        val msg = "Transaction Cancelled"
        Log.e("TAG", "onCancel: $msg")
        runOnUiThread{ tvResult?.text = msg }
    }

    private fun uriToHashMap(uri: Uri?): HashMap<String, String?> {
        val map = HashMap<String, String?>()
        if (uri != null) {
            for (key in uri.queryParameterNames) {
                map[key] = uri.getQueryParameter(key)
            }
        }
        return map
    }

    override fun onPosted(responseBody: String?) {
        Log.e("TAG", "onPosted: $responseBody")
        runOnUiThread { tvResult?.text = responseBody }
    }

    override fun onError(error: String?) {
        Log.e("TAG", "onError: $error" )
        runOnUiThread { tvResult?.text = error }
    }
}