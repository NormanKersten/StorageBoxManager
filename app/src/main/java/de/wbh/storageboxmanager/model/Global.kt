package de.wbh.storageboxmanager.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

/******* Globale Variablen und Funktionen ********************************************************/

// Im Spinner gewählte Box muss in allen Activities und Fragments bekannt sein
var spinnerItem: String = ""

// Codieren von Bitmap aus String mithilfe von Base64
fun encodeImage(bm: Bitmap): String? {
    val stream = ByteArrayOutputStream()
    bm.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    val ba = stream.toByteArray()
    return Base64.encodeToString(ba, Base64.DEFAULT)
}

// Decodieren von String zu Bitmap mithilfe von Base64
fun decodeImage(str: String): Bitmap? {
    ByteArrayOutputStream()
    val imageBytes: ByteArray = Base64.decode(str, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}
// Funktion zum Zeigen von einer Snackbar
fun View.showSnackbar(
    view: View,
    msg: String,
    length: Int,
    actionMessage: CharSequence?,
    action: (View) -> Unit
) {
    val snackbar = Snackbar.make(view, msg, length)
    if (actionMessage != null) {
        snackbar.setAction(actionMessage) {
            action(this)
        }.show()
    } else {
        snackbar.show()
    }
}