package de.wbh.storageboxmanager.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import de.wbh.storageboxmanager.R

class FragmentSettings : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}