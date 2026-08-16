package uk.na2quiz.kiosk

import android.app.admin.DevicePolicyManager
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Kiosque NA²QUIZ — DOUBLE MODE : EN LIGNE (production) et LOCAL (salle LAN).
 *
 * L'URL de démarrage est configurable et persistée. Par défaut : production.
 * Un opérateur peut, via un GESTE CACHÉ + code superviseur, basculer entre :
 *   • le serveur EN LIGNE  (https://summative.na2quizappschool.uk)
 *   • un serveur LOCAL      (http://IP-du-serveur-salle:PORT)
 *
 * Sécurité : Lock Task Mode, Device Owner, liste blanche de domaines (prod +
 * plages IP privées LAN), FLAG_SECURE, HTTPS forcé en prod (cleartext LAN OK).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminComponent: ComponentName
    private lateinit var prefs: SharedPreferences

    private val PREFS = "na2quiz_kiosk"
    private val CLE_URL = "server_url"

    // ⚠️ À ADAPTER avant déploiement.
    private val URL_EN_LIGNE = "https://summative.na2quizappschool.uk/"
    private val URL_LOCALE_DEFAUT = "http://192.168.1.10:3000/"

    // Code superviseur pour accéder à la configuration (à changer !).
    private val CODE_SUPERVISEUR = "246810"

    private val domainesProd = listOf(
        "summative.na2quizappschool.uk",
        "apisummative.na2quizappschool.uk",
    )

    private var tapsCaches = 0
    private var dernierTap = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, KioskDeviceAdminReceiver::class.java)

        if (dpm.isDeviceOwnerApp(packageName)) {
            dpm.setLockTaskPackages(adminComponent, arrayOf(packageName))
        }

        setContentView(R.layout.activity_main)
        configurerWebView()
        installerGesteCache()
        masquerBarres()

        webView.loadUrl(urlCourante())
    }

    override fun onResume() {
        super.onResume()
        demarrerLockTask()
        masquerBarres()
    }

    private fun urlCourante(): String =
        prefs.getString(CLE_URL, URL_EN_LIGNE) ?: URL_EN_LIGNE

    private fun demarrerLockTask() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (dpm.isLockTaskPermitted(packageName) || dpm.isDeviceOwnerApp(packageName)) {
                    startLockTask()
                }
            } else startLockTask()
        } catch (_: Exception) { }
    }

    @Suppress("SetJavaScriptEnabled")
    private fun configurerWebView() {
        webView = findViewById(R.id.webview)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            allowFileAccess = false
            allowContentAccess = false
            mediaPlaybackRequiresUserGesture = true
            setSupportMultipleWindows(false)
            builtInZoomControls = false
            displayZoomControls = false
        }
        webView.isLongClickable = false
        webView.setOnLongClickListener { true }
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean {
                val hote = request.url.host ?: return true
                return !hoteAutorise(hote)
            }
        }
    }

    private fun hoteAutorise(hote: String): Boolean {
        if (domainesProd.any { hote == it || hote.endsWith(".$it") }) return true
        if (hote == "localhost" || hote == "127.0.0.1") return true
        if (hote.startsWith("10.") || hote.startsWith("192.168.")) return true
        val m = Regex("^172\\.(\\d{1,3})\\.").find(hote)
        if (m != null) {
            val second = m.groupValues[1].toIntOrNull() ?: -1
            if (second in 16..31) return true
        }
        return false
    }

    private fun installerGesteCache() {
        val zone = View(this)
        val taille = (56 * resources.displayMetrics.density).toInt()
        val lp = LinearLayout.LayoutParams(taille, taille)
        addContentView(zone, lp)
        zone.setOnClickListener {
            val maintenant = System.currentTimeMillis()
            tapsCaches = if (maintenant - dernierTap < 600) tapsCaches + 1 else 1
            dernierTap = maintenant
            if (tapsCaches >= 7) {
                tapsCaches = 0
                demanderCodeSuperviseur()
            }
        }
    }

    private fun demanderCodeSuperviseur() {
        val champ = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Code superviseur"
        }
        AlertDialog.Builder(this)
            .setTitle("Accès configuration")
            .setView(champ)
            .setPositiveButton("Valider") { _, _ ->
                if (champ.text.toString() == CODE_SUPERVISEUR) ecranConfiguration()
                else Toast.makeText(this, "Code incorrect", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .setCancelable(false)
            .show()
    }

    private fun ecranConfiguration() {
        val conteneur = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 8)
        }
        val groupe = RadioGroup(this)
        val rbLigne = RadioButton(this).apply { text = "En ligne (production)" }
        val rbLocal = RadioButton(this).apply { text = "Local (salle / LAN)" }
        groupe.addView(rbLigne); groupe.addView(rbLocal)

        val champUrl = EditText(this).apply {
            hint = "http://IP-serveur-salle:port/"
            setText(
                if (urlCourante().startsWith("http://")) urlCourante() else URL_LOCALE_DEFAUT
            )
        }
        if (urlCourante().startsWith("https://")) rbLigne.isChecked = true else rbLocal.isChecked = true

        conteneur.addView(groupe)
        conteneur.addView(champUrl)

        AlertDialog.Builder(this)
            .setTitle("Serveur NA²QUIZ")
            .setView(conteneur)
            .setPositiveButton("Enregistrer") { _, _ ->
                val url = if (rbLigne.isChecked) URL_EN_LIGNE
                          else champUrl.text.toString().trim().ifEmpty { URL_LOCALE_DEFAUT }
                prefs.edit().putString(CLE_URL, url).apply()
                Toast.makeText(this, "Serveur : $url", Toast.LENGTH_LONG).show()
                webView.loadUrl(url)
            }
            .setNegativeButton("Fermer", null)
            .setCancelable(false)
            .show()
    }

    private fun masquerBarres() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_HOME,
            KeyEvent.KEYCODE_APP_SWITCH,
            KeyEvent.KEYCODE_MENU -> true
            else -> super.onKeyDown(keyCode, event)
        }
    }

    @Deprecated("Bloqué en kiosque")
    override fun onBackPressed() { }
}
