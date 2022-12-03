package de.wbh.storageboxmanager.ui

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.fragment.app.Fragment
import androidx.print.PrintHelper
import com.google.zxing.WriterException
import de.wbh.storageboxmanager.R
import de.wbh.storageboxmanager.databinding.FragmentTagsBinding
import de.wbh.storageboxmanager.model.spinnerItem

class FragmentTags : Fragment() {

    companion object {
        const val QR_PRE = "Benennung der Box: "
        const val QR_DIMEN = 25 * 25
    }

    private var _binding: FragmentTagsBinding? = null
    private val binding get() = _binding!!

    private lateinit var bitmap: Bitmap
    private lateinit var qrEncoder: QRGEncoder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTagsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // TextView für Box
        val textview: TextView = root.findViewById(R.id.idTVboxTag)
        textview.text = spinnerItem

        val btnNFC: Button = root.findViewById(R.id.idBTNnfc)
        btnNFC.setOnClickListener {
            if (spinnerItem.isNotEmpty()) {
                // TODO: NFC implementieren
            } else {
                Toast.makeText(requireContext(), "Tag beschreiben nicht möglich.\n" +
                        "Keine Box ausgewählt.", Toast.LENGTH_SHORT).show()
            }
        }

        val btnDruckenQR: Button = root.findViewById(R.id.btnDrucken)
        btnDruckenQR.setOnClickListener {
            if (spinnerItem.isNotEmpty()) {
                qrCodePrint()
            } else {
                Toast.makeText(requireContext(), "Drucken nicht möglich.\n" +
                        "Kein QR Code verfügbar.", Toast.LENGTH_SHORT).show()
            }
        }

        // QR-Code generieren und anzeigen, wenn eine Box ausgewählt ist
        if (spinnerItem.isNotEmpty()) {
            val imageViewQR = root.findViewById<ImageView>(R.id.idIVqrCode)
            qrEncoder = QRGEncoder(QR_PRE + spinnerItem, null, QRGContents.Type.TEXT, QR_DIMEN)
            qrEncoder.colorBlack = Color.WHITE
            qrEncoder.colorWhite = Color.DKGRAY
            try {
                bitmap = qrEncoder.getBitmap(1)
                imageViewQR.setImageBitmap(bitmap)
            } catch (e: WriterException) {
                Log.v(TAG, e.toString())
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Nutzung von Android PrintHelper zum drucken vom QR-Code
    private fun qrCodePrint() {
        activity?.also { context ->
            PrintHelper(context).apply {
                scaleMode = PrintHelper.SCALE_MODE_FIT
            }.also { printHelper ->
                printHelper.printBitmap("QR-code von StorageBox", bitmap)
            }
        }
    }
}