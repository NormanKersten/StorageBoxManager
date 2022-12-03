package de.wbh.storageboxmanager.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.wbh.storageboxmanager.model.Item
import de.wbh.storageboxmanager.R
import de.wbh.storageboxmanager.model.decodeImage

class AdapterRecyclerView(
    private val boxList: ArrayList<Item>,
    private  val clickListener: (Item) -> Unit,
) : RecyclerView.Adapter<AdapterRecyclerView.ItemViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val itemView = ItemViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.view_item,
            parent, false)) {
            // ClickListener in onCreate da effizienter als im onBindViewHolder!
            clickListener(boxList[it])
        }
        return itemView
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        // Decodierung vom String über Base64 zum Bitmap
        if (boxList[position].foto.toString().isNotEmpty()) {
            holder.itemsIV.setImageBitmap(decodeImage(boxList[position].foto.toString()))
        } else {
            holder.itemsIV.setImageResource(R.drawable.ic_menu_gallery)
        }
        holder.itemsNameTV.text = boxList[position].benennung
        holder.itemsDescrTV.text = boxList[position].beschreibung
    }

    override fun getItemCount(): Int {
        return boxList.size
    }

    class ItemViewHolder(itemView: View, clickAtPosition: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {
        // Initialisierung der Views
        val itemsIV: ImageView = itemView.findViewById(R.id.idIVitem)
        val itemsNameTV: TextView = itemView.findViewById(R.id.idTVitemName)
        val itemsDescrTV: TextView = itemView.findViewById(R.id.idTVitemDescr)
        // Position für OnClickListener ermitteln
        init {
            itemView.setOnClickListener{
                clickAtPosition(adapterPosition)
            }
        }
    }
}
