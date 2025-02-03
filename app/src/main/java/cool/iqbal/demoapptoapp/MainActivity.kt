package cool.iqbal.demoapptoapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import cool.iqbal.bridgeapi.BridgeApi
import cool.iqbal.bridgeapi.BridgeApi.BridgeListener
import cool.iqbal.bridgeapi.PaymentRequest
import cool.iqbal.jumpapp.JumApp
import cool.iqbal.jumpapp.MiniAtmRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random


class MainActivity : AppCompatActivity(), JumApp.JumpListener, BridgeListener {

    enum class PaymentMethod {
        QRIS,
        CDCP
    }
    private var tvTag:TextView? = null
    private var tvResult:TextView? = null
    val gson = GsonBuilder().setPrettyPrinting().create()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTag = findViewById(R.id.tvTag)
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
            data.amount = getRandomAmount()
            data.beneBankCode = "008"
            data.beneAccountNo = "15500088992"
            jumpApp.withData(data).transfer()
        }

        val btnWd: Button = findViewById(R.id.btnWd)
        btnWd.setOnClickListener {
            data.amount = getRandomAmount()
            jumpApp.withData(data).cashWithdraw()
        }

        val btnDp: Button = findViewById(R.id.btnDp)
        btnDp.setOnClickListener {
            data.amount = getRandomAmount()
            jumpApp.withData(data).cashDeposit()
        }

        /*payment request */

        val host = "https://iqhost.icu/"
        val apiKey = "Some-Api-Key"
        val bridgeApi = BridgeApi.initBridge(this, this).withHost(host).withApiKey(apiKey)
        val paymentDataRequest = PaymentRequest().apply {
            posReqType = "Kiosk"
            clientId = "CLID-9512DD2103143011"
            deviceUser = "iqbal"
            status = "Pending"
            callbackUrl = "https://learning-cat-saving.ngrok-free.app/api-callback"
        }

        val btnCdcp:Button = findViewById(R.id.btnCdcp)
        btnCdcp.setOnClickListener{
            paymentDataRequest.amount = getRandomAmount().toLong()
            paymentDataRequest.requestId = "ReqId-" + getStampId()
            paymentDataRequest.invoiceNumber = "inv-" + getStampId()
            paymentDataRequest.paymentMethod = PaymentMethod.CDCP.name
            paymentDataRequest.requestAt = getCurrentStamp()
            bridgeApi.attemptRequestPayment(paymentDataRequest)
        }

        val btnQris:Button = findViewById(R.id.btnQris)
        btnQris.setOnClickListener{
            paymentDataRequest.amount = getRandomAmount().toLong()
            paymentDataRequest.requestId = "ReqId-" + getStampId()
            paymentDataRequest.invoiceNumber = "inv-" + getStampId()
            paymentDataRequest.paymentMethod = PaymentMethod.QRIS.name
            paymentDataRequest.requestAt = getCurrentStamp()
            bridgeApi.attemptRequestPayment(paymentDataRequest)
        }

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

    private fun getRandomAmount(): Int {
        val min = 10_000
        val max = 2_000_000
        val step = 10_000

        return Random.nextInt(min / step, (max / step) + 1) * step
    }

    private fun getCurrentStamp(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun getStampId(): String {
        return (System.currentTimeMillis() /1000).toString()
    }

    private fun isValidJson(input: String): Boolean {
        return try {
            JsonParser.parseString(input)
            true
        } catch (e: JsonSyntaxException) {
            false
        }
    }

    private fun updateResult(result: String?, tag: String) {
        val stringToPrint = result?.takeIf { isValidJson(it) }
            ?.let { gson.toJson(gson.fromJson(it, Any::class.java)) }
            ?: result

        runOnUiThread {
            tvTag?.text = tag
            tvResult?.text = stringToPrint
        }
    }

    override fun onRawResult(result: Intent) {
        val rawResult = uriToHashMap(result.data)
        Log.i("TAG", "onRawResult: $rawResult")
        updateResult(rawResult.toString(), "onRawResult")
    }

    override fun onCancel() {
        val msg = "Transaction Cancelled"
        Log.e("TAG", "onCancel: $msg")
        updateResult(msg, "onCancel")
    }

    override fun onPosted(responseBody: String?) {
        Log.i("TAG", "onPosted: $responseBody")
        updateResult(responseBody, "onPosted")
    }

    override fun onError(error: String?) {
        Log.e("TAG", "onError: $error" )
        updateResult(error, "onError")
    }

    override fun onSseEvent(eventType: String?, data: String?) {
        Log.i(eventType, "onSseEvent: $data")
        if (!eventType.equals("heartbeat")){
            updateResult(data, "onSseEvent - $eventType");
        }
    }

    override fun onInfo(info: String?) {
        Log.i("TAG", "onInfo: $info" )
        updateResult(info, "onInfo")
    }
}