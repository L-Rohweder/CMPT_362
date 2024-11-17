package com.example.beacon.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.beacon.R
import com.example.beacon.activities.ProfileActivity
import com.example.beacon.databinding.FragmentSettingsBinding
import com.example.beacon.utils.Constants

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var profileButton: TextView
    private lateinit var unitsRadioGroup: RadioGroup
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        unitsRadioGroup = root.findViewById(R.id.unitsRadioGroup)
        prefs = requireActivity().getSharedPreferences(Constants.SP_KEY, Context.MODE_PRIVATE)
        unitsRadioGroup.setOnCheckedChangeListener { radioGroup, id ->
            val checkedButton = root.findViewById<TextView>(id)
            prefs.edit().putString(Constants.SP_UNITS, checkedButton.text.toString()).apply()
        }
        profileButton = root.findViewById<TextView>(R.id.openProfileText)
        profileButton.setOnClickListener(){
            val intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}