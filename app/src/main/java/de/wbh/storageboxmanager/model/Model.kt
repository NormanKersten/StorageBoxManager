package de.wbh.storageboxmanager.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/******* Datenmodell mit gemeinsam genutzten Daten-Klassen ***************************************/

data class StorageBox (

    var id: Long,
    var benennung: String,
    var beschreibung: String?, )

data class StorageBoxPost (

    // Ohne id, da diese von der Datenbank automatisch erzeugt wird
    var benennung: String,
    var beschreibung: String? = "", )

@Parcelize
data class Item (

    var id: Long,
    var benennung: String,
    var beschreibung: String?,
    var foto: String?,
    var anzahl: Int, ) : Parcelable

@Parcelize
data class ItemPost (

    // Ohne id, da diese von der Datenbank automatisch erzeugt wird
    var benennung: String,
    var beschreibung: String?,
    var foto: String?,
    var anzahl: Int, ) : Parcelable