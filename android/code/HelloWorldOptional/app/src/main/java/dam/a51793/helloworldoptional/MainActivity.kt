package dam.a51793.helloworldoptional

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    @Suppress("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val tv = findViewById<TextView>(R.id.editTextTextMultiLine)
        tv.text = "Manufacturer: ${Build.MANUFACTURER}\n" +
                "Model: ${Build.MODEL}\n" +
                "Brand: ${Build.BRAND}\n" +
                "Type: ${Build.TYPE}\n" +
                "User: ${Build.USER}\n" +
                "Base: ${Build.VERSION.BASE_OS.ifEmpty { "1" }}\n" + //Se a string estiver vazia, fica a "1"
                "Incremental: ${Build.VERSION.INCREMENTAL}\n" +
                "SDK: ${Build.VERSION.SDK_INT}\n" +
                "Version Code: ${Build.VERSION.RELEASE}\n" +
                "Display: ${Build.DISPLAY}\n"

    }
}