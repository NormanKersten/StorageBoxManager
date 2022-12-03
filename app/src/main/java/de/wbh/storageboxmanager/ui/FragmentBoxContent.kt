package de.wbh.storageboxmanager.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import de.wbh.storageboxmanager.R
import de.wbh.storageboxmanager.databinding.FragmentBoxcontentBinding
import de.wbh.storageboxmanager.model.*

class FragmentBoxContent : Fragment(), AdapterView.OnItemSelectedListener {

    companion object {
        const val NEW_ITEM = "newItem"
        const val MOD_ITEM = "modItem"
        const val ITEM_LIST = "itemList"
        const val BOX_ID = "boxID"
        const val BOX_LIST = "boxList"
        const val BOX_SELECT = "boxSelect"
        const val ITEM_FOTO = "item_foto"
        const val NFC_READ_COMMAND = "de.wbh.storageboxmanager"
    }

    private var _binding: FragmentBoxcontentBinding? = null
    private lateinit var boxcontentViewModel: ViewModelBoxContent
    private val binding get() = _binding!!

    private lateinit var boxList: ArrayList<String>
    lateinit var spinner: Spinner

    lateinit var nfcBoxName: String
    lateinit var nfcBoxID: String

    // RecyclerView
    lateinit var itemsRV: RecyclerView
    lateinit var itemsRVAdapter: AdapterRecyclerView
    lateinit var itemsList: ArrayList<Item>
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // Shared Preferences Listener
    private val prefsChangedListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> }

    // Launch ActivityNewItem für neues Item und callback Methode
    private val getResultNewItem =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val bundle = it.data?.extras
                val newItem = bundle?.getParcelable<ItemPost>(NEW_ITEM)
                val foto = bundle?.getString(ITEM_FOTO, "")
                if (newItem != null) {
                    boxcontentViewModel.addItem(newItem, getBoxIdFromSpinner(spinnerItem)!!)
                }
            }
        }
    // Launch ActivityModItem und callback Methode
    private val getResultModItem =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val bundle = it.data?.extras
                val modItem = bundle?.getParcelable<Item>(MOD_ITEM)
                val boxID = bundle?.getString(BOX_ID)
                if (modItem != null) {
                    boxcontentViewModel.modItem(getBoxIdFromSpinner(boxID!!)!!, modItem)
                    spinner.setSelection(bundle.getLong(BOX_SELECT, 0).toInt())
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setHasOptionsMenu(true)

        boxcontentViewModel = ViewModelProvider(this)[ViewModelBoxContent::class.java]
        _binding = FragmentBoxcontentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // PreferenceManager für Einstellungen
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefs.registerOnSharedPreferenceChangeListener(prefsChangedListener)

        // Statusmeldungen per Toast, wenn Einstellung aktiviert ist
        if (prefs.getBoolean("notifications", true)) {
            boxcontentViewModel.status.observe(viewLifecycleOwner, Observer {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            })
        }
/******* Spinner, Adapter und Observer für Adapter ***********************************************/
        spinner = root.findViewById(R.id.idSPboxes)
        // Observer für Box-Benennungen welche vom Server geladen werden
        boxcontentViewModel.listBoxes.observe(viewLifecycleOwner, Observer {
            // Liste für Adapter wird angepasst und neuer Adapter für Spinner erstellt
            boxList = it.mapTo(arrayListOf()) {it.benennung}
            val adapter = ArrayAdapter(requireContext(), R.layout.spinner_list, boxList)
            adapter.setDropDownViewResource(R.layout.spinner_list)
            adapter.setDropDownViewResource(R.layout.spinner_list)
            spinner.adapter = adapter
            spinner.onItemSelectedListener = this
        })

/******* Swipe-to-refresh, Recycler View, Adapter und Observer für Adapter ***********************/
        itemsRV = root.findViewById(R.id.idRVCourses)
        // Observer
        boxcontentViewModel.listItems.observe(viewLifecycleOwner, Observer { it ->
            itemsList = it.mapTo(arrayListOf()) { it }
            Log.d("missedItem", "arraylist " + itemsList.size.toString())
            itemsRVAdapter = AdapterRecyclerView(itemsList){
                // Aufruf ActivityModItem durch onClickListener
                val intent = Intent(context, ActivityModItem::class.java)
                intent.putExtra(ITEM_LIST, it)
                intent.putExtra(BOX_ID, getBoxIdFromSpinner(spinnerItem))
                intent.putExtra(BOX_LIST, boxList)
                intent.putExtra(BOX_SELECT, spinner.selectedItemId)
                getResultModItem.launch(intent)
            }
            itemsRV.adapter = itemsRVAdapter
        })

        // Swipe-to-refresh Funktionalität
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            boxcontentViewModel.getItemsInBox(getBoxIdFromSpinner(spinnerItem)!!)
        }

        // Swipe-to-delete Funktionalität
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // Mögliche Implementierung für Move des Items
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Item Position holen
                val deletedItem: Item = itemsList[viewHolder.adapterPosition]
                val position = viewHolder.adapterPosition
                val boxIDfromSpinner = getBoxIdFromSpinner(spinnerItem)
                // Item aus Liste und DB löschen
                itemsList.removeAt(viewHolder.adapterPosition)
                itemsRVAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                if (boxIDfromSpinner != null)
                    boxcontentViewModel.deleteItem(deletedItem.id, boxIDfromSpinner)

                Snackbar.make(itemsRV, "Gelöscht " + deletedItem.benennung, Snackbar.LENGTH_LONG)
                    .setAction(
                        "Rückgängig",
                        View.OnClickListener {
                            // Rückholaktion
                            itemsList.add(position, deletedItem)
                            val reItem = ItemPost(deletedItem.benennung, deletedItem.beschreibung,
                                deletedItem.foto.toString(), deletedItem.anzahl)
                            boxcontentViewModel.addItem(reItem, boxIDfromSpinner!!)
                            itemsRVAdapter.notifyItemInserted(position)
                        }).show()
            }
        }).attachToRecyclerView(itemsRV)

/******* Floating Button und Event Handling ******************************************************/
        val fab: View = root.findViewById(R.id.fab)
        fab.setOnClickListener {
            if (!spinnerItem.isEmpty()) {
                val intent = Intent(requireContext(), ActivityNewItem::class.java)
                intent.putExtra("BOX_NAME", spinnerItem)
                intent.putExtra("BOX_ID", getBoxIdFromSpinner(spinnerItem))
                getResultNewItem.launch(intent)
            }
        }
        return root
    }

/******* Ende onCreateView ************************************************************************
 **************************************************************************************************/
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

/******* Event Handling für Spinner Adapter ******************************************************/
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        if (parent != null) {
            spinnerItem = parent.selectedItem.toString()
            if (getBoxIdFromSpinner(spinnerItem) != null)
                boxcontentViewModel.getItemsInBox(getBoxIdFromSpinner(spinnerItem)!!)
        }
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Nichts zu tun
    }

/******* Box-ID von aktuellem Spinner Item holen**************************************************/
    fun getBoxIdFromSpinner(spinnerText: String): Long? {
        for (box in boxcontentViewModel.listBoxes.value!!) {
            if (spinnerText == box.benennung) {
                return box.id
            }
        }
        return null
    }

/******* Eigenes Fragment-Menu *******************************************************************/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_bar_frag_box, menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search ->
                    with(AlertDialog.Builder(requireContext())) {
                        val inflater = layoutInflater
                        val dialogLayout = inflater.inflate(R.layout.alert_dialog_new_box, null)
                        setTitle("Artikel in Box suchen")
                        setView(dialogLayout)
                        val editTextName = dialogLayout.findViewById<EditText>(R.id.idETnewBoxName)
                        val editTextDes = dialogLayout.findViewById<EditText>(R.id.idETnewBoxDes)
                        setNegativeButton("Abbrechen", DialogInterface.OnClickListener { dialog, id -> })
                        setPositiveButton("Suchen", DialogInterface.OnClickListener { dialog, id ->
                        boxcontentViewModel.searchItemsInBox(getBoxIdFromSpinner(spinnerItem)!!,
                                    editTextName.text.toString(), editTextDes.text.toString()) })
                        if (spinnerItem.isNotEmpty()) {
                            show()
                        } else {
                            Toast.makeText(
                                requireContext(), "Keine Box gewählt, in welcher gesucht werden kann.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        true
                    }

            R.id.box_descr ->
                with(AlertDialog.Builder(requireContext())) {
                    val inflater = layoutInflater
                    val dialogLayout = inflater.inflate(R.layout.alert_dialog_box_info, null)
                    setTitle("Box Beschreibung")
                    setView(dialogLayout)
                    val textview = dialogLayout.findViewById<TextView>(R.id.TVinfoBox)
                    if (spinnerItem.isNotEmpty()) {
                        for (box in boxcontentViewModel.listBoxes.value!!) {
                            if (spinnerItem == box.benennung) {
                                textview.text = box.beschreibung
                            }
                        }
                        setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id -> })
                        show()
                    }
                    else Toast.makeText(requireContext(), "Keine Box ausgewählt.", Toast.LENGTH_SHORT).show()
                    true
                }

            R.id.mod_box ->
                with(AlertDialog.Builder(requireContext())) {
                    val inflater = layoutInflater
                    val dialogLayout = inflater.inflate(R.layout.alert_dialog_rename_box, null)
                    setTitle("Box umbenennen:")
                    setView(dialogLayout)
                    val editTextName = dialogLayout.findViewById<EditText>(R.id.idETrenameBoxName)
                    val editTextDes = dialogLayout.findViewById<EditText>(R.id.idETrenameBoxDes)
                    setNegativeButton("Abbrechen", DialogInterface.OnClickListener { dialog, id -> })
                    if (!spinnerItem.isEmpty()) {
                        var oldBox: StorageBox? = null
                        for (item in boxcontentViewModel.listBoxes.value!!) {
                            if (spinnerItem == item.benennung) {
                                oldBox = item
                                break
                            }
                        }
                        editTextName.setText(spinnerItem)
                        editTextDes.setText(oldBox!!.beschreibung)
                        setPositiveButton("UMBENENNEN", DialogInterface.OnClickListener { dialog, id ->
                            val modBox = StorageBoxPost(editTextName.text.toString(), editTextDes.text.toString())
                            boxcontentViewModel.modBox(oldBox.id, modBox)
                        })
                        show()
                    }
                    else Toast.makeText(requireContext(), "Keine Box ausgewählt.", Toast.LENGTH_SHORT).show()
                    true
                }

            R.id.delete_box ->
                with(AlertDialog.Builder(requireContext())) {
                    val inflater = layoutInflater
                    val dialogLayout = inflater.inflate(R.layout.alert_dialog_delete_box, null)
                    setTitle("Box wirklich löschen?")
                    setView(dialogLayout)
                    val textview = dialogLayout.findViewById<TextView>(R.id.TVdelBox)
                    textview.text = spinnerItem
                    setNegativeButton("Abbrechen", DialogInterface.OnClickListener { _, _ -> })
                    setPositiveButton("Löschen", DialogInterface.OnClickListener { _, _ ->
                        boxcontentViewModel.deleteBox(getBoxIdFromSpinner(spinnerItem)!!)
                        // TODO: Könnte optimiert werden
                        boxcontentViewModel.getItemsInBox(0)
                    })
                    if (spinnerItem.isNotEmpty())
                        show()
                    else Toast.makeText(requireContext(), "Keine Box ausgewählt.", Toast.LENGTH_SHORT).show()
                    true
                }

            R.id.new_box ->
                with(AlertDialog.Builder(requireContext())) {
                    val inflater = layoutInflater
                    val dialogLayout = inflater.inflate(R.layout.alert_dialog_new_box, null)
                    setTitle("Box neu anlegen")
                    setView(dialogLayout)
                    val editTextName = dialogLayout.findViewById<EditText>(R.id.idETnewBoxName)
                    val editTextDes = dialogLayout.findViewById<EditText>(R.id.idETnewBoxDes)
                    setNegativeButton("Abbrechen", DialogInterface.OnClickListener { dialog, id -> })
                    setPositiveButton("Hinzufügen", DialogInterface.OnClickListener { dialog, id ->
                        val newBox = StorageBoxPost(editTextName.text.toString(), editTextDes.text.toString())
                        if (!editTextName.text.toString().isEmpty()) {
                            boxcontentViewModel.addBox(newBox)
                            //Toast.makeText(requireContext(), "Box hinzugefügt", Toast.LENGTH_SHORT).show()
                        } else
                            Toast.makeText(requireContext(), "Bitte Benennung eingeben!", Toast.LENGTH_SHORT).show()
                    })
                    show()
                true
                }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /*** Funktion zum Setzen vom Spinner entsprechend NFC Tag und abrufen der Inhalte
     *** Funktion ist noch nicht vollständig lauffähig!
     */
    private fun setSpinnerNfc(boxName: String, boxID: String) {
        boxcontentViewModel.getItemsInBox(boxID.toLong())
        Log.d("myNFC", "$boxName $boxID")
        //spinner.setSelection()
    }
}




