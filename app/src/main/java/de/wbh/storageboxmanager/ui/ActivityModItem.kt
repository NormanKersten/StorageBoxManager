package de.wbh.storageboxmanager.ui

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import de.wbh.storageboxmanager.*
import de.wbh.storageboxmanager.model.Item
import de.wbh.storageboxmanager.model.decodeImage
import de.wbh.storageboxmanager.model.encodeImage
import de.wbh.storageboxmanager.model.showSnackbar

class ActivityModItem : AppCompatActivity() {

    lateinit var switch: Switch
    private lateinit var spinner: Spinner
    private lateinit var textviewBenennung: EditText
    private lateinit var textviewBeschreibung: EditText
    private lateinit var textviewAnzahl: EditText
    private lateinit var imageView: ImageView
    private lateinit var item: Item

    var bitmapEncode: String = ""
    private var imageBitmap: Bitmap? = null

    // Preference Manager Listener für Einstellungen
    private val prefsChangedListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == "modification") {
            if (prefs != null) {
                switch.isChecked = prefs.getBoolean("modification", false)
            }
        }
    }

    // Registrierung vom Permission callback
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Direkt zur Kamera, wenn Berechtigung erteilt ist
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null)
                    startActivityForResult(takePictureIntent, ActivityNewItem.REQUEST_IMAGE_CAPTURE)
                else
                    Toast.makeText(this, "Keine geeignete Anwendung installiert", Toast.LENGTH_LONG).show()
            } else {
                // nichts zu tun
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_item)

        // Bild bei Drehung wiederherstellen
        imageBitmap = (savedInstanceState?.getParcelable(ActivityNewItem.THUMBNAIL) as Bitmap?)
        if (imageBitmap != null) {
            findViewById<ImageView>(R.id.iVmodItem).setImageBitmap(imageBitmap)
            findViewById<ImageView>(R.id.iVmodItem).setBackgroundColor(resources.getColor(R.color.white))
        }

        // PreferenceManager für Einstellungen
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        prefs.registerOnSharedPreferenceChangeListener(prefsChangedListener)

        textviewBenennung = findViewById(R.id.idETbenennung)
        textviewBeschreibung = findViewById(R.id.idETbeschreibung)
        textviewAnzahl = findViewById(R.id.idETnumberMod)
        imageView = findViewById(R.id.iVmodItem)

        // Views mit zu änderndem Objekt initialisieren
        item = intent.getParcelableExtra(FragmentBoxContent.ITEM_LIST)!!
        textviewBenennung.setText(item.benennung)
        textviewBeschreibung.setText(item.beschreibung)
        textviewAnzahl.setText(item.anzahl.toString())
        imageView.setImageBitmap(decodeImage(item.foto.toString()))
        bitmapEncode = item.foto.toString()

        /******* Spinner, Adapter und Observer für Adapter ***************************************/
        spinner = findViewById(R.id.idSPmodItem)
        // Adapter mit Box Liste vom Intent und Pre-Selection der entsprechenden Box
        val adapter = ArrayAdapter(this, R.layout.spinner_list,
                        intent.getStringArrayListExtra(FragmentBoxContent.BOX_LIST)!!)
        adapter.setDropDownViewResource(R.layout.spinner_list)
        spinner.adapter = adapter
        spinner.setSelection(intent.getLongExtra(FragmentBoxContent.BOX_SELECT, 0).toInt())

        // Bild bei Drehung wiederherstellen
        val bitmap = savedInstanceState?.getParcelable(ActivityNewItem.THUMBNAIL) as Bitmap?
        if (bitmap != null) {
            Log.d(javaClass.simpleName, "onCreate: restore bitmap")
            findViewById<ImageView>(R.id.iVmodItem).setImageBitmap(bitmap)
            findViewById<ImageView>(R.id.iVmodItem).setBackgroundColor(resources.getColor(R.color.white))
        }

        // Eigenschaften sind nicht editierbar per Default
        enableModView(false)
        // Editieren nur, wenn Switch "isChecked"
        switch = findViewById(R.id.idSwitchItem)
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableModView(true)
            } else {
                enableModView(false)
            }
        }
        if (prefs.getBoolean("modification", false)) switch.isChecked = true
    }

    fun onBtnCameraClickMod(view: View) {

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null)
                    startActivityForResult(takePictureIntent, ActivityNewItem.REQUEST_IMAGE_CAPTURE)
                else
                    Toast.makeText(this, "Keine geeignete Anwendung installiert", Toast.LENGTH_LONG).show()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> {
                view.showSnackbar(
                    view,
                    getString(R.string.permission_required),
                    Snackbar.LENGTH_LONG,
                    getString(R.string.ok)
                ) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }
            }

            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    fun onBtnZurueckClick(view: View) {
        with(AlertDialog.Builder(this)) {
            setTitle("Eingaben abbrechen")
            setNegativeButton("Ja", DialogInterface.OnClickListener { dialog, id ->
                finish()
            })
            setPositiveButton("Nein", DialogInterface.OnClickListener { dialog, id -> })
            show()
        }
    }

    fun onBtnAendernClick(view: View) {
        if (imageBitmap != null)
            bitmapEncode = encodeImage(imageBitmap!!)!!
        val modItem = Item(item.id, textviewBenennung.text.toString(), textviewBeschreibung.text.toString(),
                            bitmapEncode, Integer.parseInt(textviewAnzahl.text.toString()))
        // callback modItem zum FragmentBoxContent
        val intent = Intent(this, ActivityMain::class.java)
        intent.putExtra(FragmentBoxContent.MOD_ITEM, modItem)
        intent.putExtra(FragmentBoxContent.BOX_ID, spinner.selectedItem.toString())
        // Falls Item in andere Box verschoben wird, soll diese direkt angezeigt werden
        intent.putExtra(FragmentBoxContent.BOX_SELECT, spinner.selectedItemId)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ActivityNewItem.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageBitmap = data?.extras?.get("data") as Bitmap
            findViewById<ImageView>(R.id.iVmodItem).setImageBitmap(imageBitmap)
            findViewById<ImageView>(R.id.iVmodItem).setBackgroundColor(resources.getColor(R.color.white))
        }
    }

    // Sodass Bild auch nach dem Drehen vom Bildschirm wieder da ist
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val drawable = (findViewById<ImageView>(R.id.iVmodItem)).drawable
        if (drawable != null) {
            outState.putParcelable(ActivityNewItem.THUMBNAIL, (drawable as BitmapDrawable).bitmap)
        }
    }

    // Editiermodus ein oder ausschalten
    private fun enableModView(enable: Boolean) {
        if (!enable) {
            val tvEditbenennung: TextView = findViewById(R.id.idETbenennung)
            tvEditbenennung.isEnabled = false
            val tvEditbeschreibung: TextView = findViewById(R.id.idETbeschreibung)
            tvEditbeschreibung.isEnabled = false
            val tvEditanzahl: TextView = findViewById(R.id.idETnumberMod)
            tvEditanzahl.isEnabled = false
            val buttonBild: Button = findViewById(R.id.idBTNBildaendern)
            buttonBild.isEnabled = false
            val button: Button = findViewById(R.id.idtbnAendern)
            button.isEnabled = false
            spinner.isEnabled = false
        } else {
            val tvEditbenennung: TextView = findViewById(R.id.idETbenennung)
            tvEditbenennung.isEnabled = true
            val tvEditbeschreibung: TextView = findViewById(R.id.idETbeschreibung)
            tvEditbeschreibung.isEnabled = true
            val tvEditanzahl: TextView = findViewById(R.id.idETnumberMod)
            tvEditanzahl.isEnabled = true
            val buttonBild: Button = findViewById(R.id.idBTNBildaendern)
            buttonBild.isEnabled = true
            val button: Button = findViewById(R.id.idtbnAendern)
            button.isEnabled = true
            spinner.isEnabled = true
        }
    }
}