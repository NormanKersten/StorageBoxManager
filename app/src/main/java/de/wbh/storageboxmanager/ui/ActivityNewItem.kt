package de.wbh.storageboxmanager.ui

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import android.Manifest
import de.wbh.storageboxmanager.*
import de.wbh.storageboxmanager.model.ItemPost
import de.wbh.storageboxmanager.model.encodeImage
import de.wbh.storageboxmanager.model.showSnackbar


class ActivityNewItem : AppCompatActivity() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val THUMBNAIL = "thumbnail"
    }

    private var bitmapEncode: String = ""
    private var imageBitmap: Bitmap? = null

    // Registrierung vom Permission callback
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Direkt zur Kamera, wenn Berechtigung erteilt ist
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                else
                    Toast.makeText(this, "Keine geeignete Anwendung installiert", Toast.LENGTH_LONG).show()
            } else {
                // nichts zu tun
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_item)

        // Bild bei Drehung wiederherstellen
        imageBitmap = (savedInstanceState?.getParcelable(THUMBNAIL) as Bitmap?)
        if (imageBitmap != null) {
            Log.d("TestBitmap", "onCreate: restore bitmap")
            findViewById<ImageView>(R.id.iVnewItem).setImageBitmap(imageBitmap)
            findViewById<ImageView>(R.id.iVnewItem).setBackgroundColor(resources.getColor(R.color.white))
        }
        // TextView für Box Benennung
        val textview: TextView = findViewById(R.id.idTVboxNewItem)
        textview.text = intent.getStringExtra("BOX_NAME").toString()
    }

    fun onBtnCameraClick(view: View) {

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // TODO: Evtl. noch umbauen auf neue Methode
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
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

    fun onBtnAbbruchClick(view: View) {
        with(AlertDialog.Builder(this)) {
            setTitle("Eingaben abbrechen")
            setNegativeButton("Ja", DialogInterface.OnClickListener { dialog, id ->
                finish()
            })
            setPositiveButton("Nein", DialogInterface.OnClickListener { dialog, id -> })
            show()
        }
    }

    // Übergabe des neuen Item an FragmentBoxContent per Intent
    fun onBtnHinzuClick(view: View) {
        if (!findViewById<TextView>(R.id.ETnewItemBenennung).text.isEmpty()) {
            if (imageBitmap != null) {
                bitmapEncode = encodeImage(imageBitmap!!)!!
            }
            val newItem = ItemPost(findViewById<TextView>(R.id.ETnewItemBenennung).text.toString(),
                            findViewById<TextView>(R.id.ETnewItemBeschreibung).text.toString(), bitmapEncode,
                        Integer.parseInt(findViewById<TextView>(R.id.idETnumber).text.toString()))
            val intent = Intent(this, ActivityMain::class.java)
            intent.putExtra(FragmentBoxContent.NEW_ITEM, newItem)
            setResult(RESULT_OK, intent)
            finish()
        } else Toast.makeText(this,"Bitte eine Benennung eingeben", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageBitmap = data?.extras?.get("data") as Bitmap
            findViewById<ImageView>(R.id.iVnewItem).setImageBitmap(imageBitmap)
            findViewById<ImageView>(R.id.iVnewItem).setBackgroundColor(resources.getColor(R.color.white))
        }
    }

    // Sodass Bild auch nach dem Drehen vom Bildschirm wieder da ist
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val drawable = (findViewById<ImageView>(R.id.iVnewItem)).drawable
        if (drawable != null) {
            outState.putParcelable(THUMBNAIL, (drawable as BitmapDrawable).bitmap)
        }
    }
}
