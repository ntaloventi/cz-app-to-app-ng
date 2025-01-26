package cool.iqbal.demoapptoapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import cool.iqbal.jump_app.JumApp
import cool.iqbal.jump_app.MiniAtmRequest


class MainActivity : AppCompatActivity(), JumApp.JumpListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val jumpApp = JumApp.initJumpApp(this, this)
        val data = MiniAtmRequest().apply {
            isLauncher = true
            callback = "bukalapak://czresult.data"
        }

        val btnCb: Button = findViewById(R.id.btnCb)
        btnCb.setOnClickListener {
            jumpApp.withData(data)
            jumpApp.checkBalance()
        }

        val btnTf: Button = findViewById(R.id.btnTf)
        btnTf.setOnClickListener {
            data.amount = 150000
            data.beneBankCode = "008"
            data.beneAccountNo = "15500088992"
            jumpApp.withData(data)
            jumpApp.transfer()
        }

        val btnWd: Button = findViewById(R.id.btnWd)
        btnWd.setOnClickListener {
            data.amount = 160000
            jumpApp.withData(data)
            jumpApp.cashWithdraw()
        }

        val btnDp: Button = findViewById(R.id.btnDp)
        btnDp.setOnClickListener {
            data.amount = 170000
            jumpApp.withData(data)
            jumpApp.cashDeposit()
        }

    }

    override fun onRawResult(result: Intent) {
        Log.e("TAG", "onRawResult: " + uriToHashMap(result.data) )
    }

    override fun onCancel() {
        Log.e("TAG", "onCancel: Transaction Cancelled")
    }

    fun uriToHashMap(uri: Uri?): HashMap<String, String?> {
        val map = HashMap<String, String?>()
        if (uri != null) {
            for (key in uri.queryParameterNames) {
                map[key] = uri.getQueryParameter(key)
            }
        }
        return map
    }
}