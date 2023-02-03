package cn.com.verificationinputview

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import cn.com.cys.widget.VerificationInputView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val verifyViewRect = findViewById<VerificationInputView>(R.id.verify_view_rect)
        verifyViewRect.setOnVerifyCompletionListener(object : VerificationInputView.OnVerifyCompletionListener{
            override fun onVerify(text: String): Boolean {
                if("1234" != text){
                    return true
                }
                Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
                return false
            }
        })

        val verifyViewRound = findViewById<VerificationInputView>(R.id.verify_view_round)
        verifyViewRound.setOnVerifyCompletionListener(object : VerificationInputView.OnVerifyCompletionListener{
            override fun onVerify(text: String): Boolean {
                if("1234" != text){
                    return true
                }
                Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
                return false
            }
        })

        val verifyViewLine = findViewById<VerificationInputView>(R.id.verify_view_line)
        verifyViewLine.setOnVerifyCompletionListener(object : VerificationInputView.OnVerifyCompletionListener{
            override fun onVerify(text: String): Boolean {
                if("1234" != text){
                    return true
                }
                Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
                return false
            }
        })
    }
}