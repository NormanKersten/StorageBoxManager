package de.wbh.storageboxmanager.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.wbh.storageboxmanager.retrofit.Api
import kotlinx.coroutines.launch

class ViewModelBoxContent : ViewModel() {

    // MutableLiveData intern zum Speichern der Daten
    private var _status = MutableLiveData<String>()
    private var _listBoxes = MutableLiveData<List<StorageBox>>()
    private var _listItems = MutableLiveData<List<Item>>()

    // MutableLiveData extern zum Speichern der Daten
    val status: LiveData<String> = _status
    val listBoxes: LiveData<List<StorageBox>> = _listBoxes
    val listItems: LiveData<List<Item>> = _listItems

    // Call getBoxes() on init sodass Boxes schnell verfügbar sind
    init {
         getBoxes()
    }

/******* Funktionen für Zugriff auf den Webservice per ApiService-Klasse *************************/

private fun getBoxes() {
        viewModelScope.launch {
            try {
                _listBoxes.value = Api.retrofitService.getBoxes()
                _status.value = "${_listBoxes.value!!.size} Boxen gefunden"
            } catch (e: Exception) {
                _status.value = "Fehler: ${e.message}"
            }
        }
    }

    fun addBox(box: StorageBoxPost) {
        viewModelScope.launch {
            try {
                Api.retrofitService.addBox(box)
                _status.value = "Box hinzugefügt"
                _listBoxes.value = Api.retrofitService.getBoxes()
                _status.value = "${_listBoxes.value!!.size} Boxen gefunden"
            } catch (e: Exception) {
                _status.value = "Fehler: ${e.message}"
            }
        }
    }

    fun deleteBox(boxID: Long) {
        viewModelScope.launch {
            try {
                Api.retrofitService.deleteBox(boxID)
                _status.value = "Box gelöscht"
                _listBoxes.value = Api.retrofitService.getBoxes()
                _status.value = "${_listBoxes.value!!.size} Boxen gefunden"
            } catch (e: Exception) {
                _status.value = "Fehler: ${e.message}"
            }
        }
    }

    fun modBox(boxID: Long, box: StorageBoxPost) {
        viewModelScope.launch {
            try {
                Api.retrofitService.modBox(boxID, box)
                _status.value = "Box geändert"
                _listBoxes.value = Api.retrofitService.getBoxes()
            } catch (e: Exception) {
                _status.value = "Fehler: ${e.message}"
            }
        }
    }

    fun addItem(item: ItemPost, boxID: Long) {
        viewModelScope.launch {
            try {
                Api.retrofitService.addItem(boxID, item)
                _status.value = "Inhalt hinzugefügt"
                _listItems.value = Api.retrofitService.getItemsInBox(boxID)
                _status.value = "${_listItems.value!!.size} Artikel in der Box"
            } catch (e: Exception) {
                _status.value = "Fehler: ${e.message}"
            }
        }
    }

    fun modItem(boxID: Long, item: Item) {
        viewModelScope.launch {
            try {
                Api.retrofitService.modItem(boxID, item)
                _status.value = "Inhalt geändert"
                _listItems.value = Api.retrofitService.getItemsInBox(boxID)
            } catch (e: Exception) {
                _status.value = "Fehler: ${e.message}"
            }
        }
    }

    fun getItemsInBox(boxID: Long) {
        viewModelScope.launch {
            try {
                _listItems.value = Api.retrofitService.getItemsInBox(boxID)
                _status.value = "${_listItems.value!!.size} Artikel in der Box"
            } catch (e: Exception) {
                _status.value = "Fehler: ${e.message}"
            }
        }
    }

    fun deleteItem(itemID: Long, boxID: Long) {
        viewModelScope.launch {
            try {
                Api.retrofitService.deleteItem(itemID)
                _status.value = "Inhalt gelöscht"
                _listItems.value = Api.retrofitService.getItemsInBox(boxID)
                _status.value = "${_listItems.value!!.size} Artikel in der Box"
            } catch (e: Exception) {
                _status.value = "Fehler: ${e.message}"
            }
        }
    }

    fun searchItemsInBox(boxID: Long, name: String, desc: String) {
        viewModelScope.launch {
            try {
                _listItems.value = Api.retrofitService.searchItemsInBox(boxID, name, desc)
                _status.value = "${_listItems.value!!.size} Artikel in der Box gefunden"
            } catch (e: Exception) {
                _status.value = "Fehler: ${e.message}"
            }
        }
    }
}