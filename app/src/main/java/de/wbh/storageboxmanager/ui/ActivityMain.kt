package de.wbh.storageboxmanager.ui

import android.content.Intent
import android.content.SharedPreferences
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.wbh.storageboxmanager.R
import de.wbh.storageboxmanager.databinding.ActivityMainBinding
import java.util.*

class ActivityMain : AppCompatActivity() {

    companion object {
        const val NFC_BOX_NAME = "nfc_box_name"
        const val NFC_BOX_ID = "nfc_box_id"
    }

    private val prefsChangedListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        // Benachrichtigung, dass eine Einstellung geändert wurde
        Toast.makeText(this@ActivityMain, "Einstellung geändert", Toast.LENGTH_SHORT).show()
    }

    private lateinit var binding: ActivityMainBinding
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null) {
            // Zielt auf die Behandlung vom NFC-Tag ab
            processIntent(intent)
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        prefs.registerOnSharedPreferenceChangeListener(prefsChangedListener)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // NFC Adapter
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        val navView: BottomNavigationView = binding.navView
        // Nach der Umstellung auf die FragmentContainerView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        // Top Level Menus
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_boxcontent, R.id.navigation_tags, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_app_bar, menu)
        return true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            // Zielt auf die Behandlung vom NFC-Tag ab
            processIntent(intent)
        }
    }

    // Funktion zur Bearbeitung des NFC-Intent.
    // Aktuell ist diese Funktionalität noch nicht ausprogrammiert!
    private fun processIntent(checkIntent: Intent) {
        // Check ob NFC tag
        if (checkIntent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            val rawMessages = checkIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            // Message holen
            val ndefMsg = rawMessages?.get(0) as NdefMessage
            // Erster Datensatz -> URI
            val ndefRecord = ndefMsg.records[0]
            // Zweiter Datensatz -> Boxname
            val boxName = ndefMsg.records[1]
            // Dritter Datensatz -> Box-ID
            val boxID = ndefMsg.records[2]
            // Datensätze lesen und zu String decodieren
            val payloadName = boxName.payload
            val payloadID = boxID.payload
            val boxNameBa: ByteArray = Arrays.copyOfRange(payloadName, payloadName.first().toInt() + 1,
                                        payloadName.size)
            val boxNameString = boxNameBa.decodeToString()
            val boxIDBa: ByteArray = Arrays.copyOfRange(payloadID, payloadID.first().toInt() + 1,
                                        payloadID.size)
            val boxIDString = boxIDBa.decodeToString()
            // Die Daten dem Fragment in einem Bundle übergeben
            val fragment = FragmentBoxContent()
            val bundle = Bundle()
            bundle.putString(NFC_BOX_NAME, boxNameString)
            bundle.putString(NFC_BOX_ID, boxIDString)
            fragment.arguments = bundle
            // TODO: NFC Handling im Fragment und Anzeige der entsprechenden Box und Inhalte
        }
    }
}