package io.github.turskyi.expandedradiobuttons

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    companion object {
        const val LOG_TAG = "===>"
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<DropdownRadioButtons>(R.id.drb).apply {
            setHtmlTitle(        resources.getString(
                R.string.energy_how_to_title, "2nd"
            ))
            setRadioButtonsSize(4)
            getContent().setOnCheckedChangeListener(contentListener)
        }

        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    private val contentListener: RadioGroup.OnCheckedChangeListener =  RadioGroup.OnCheckedChangeListener { rg: RadioGroup, radioId: Int ->
        val radioButton: RadioButton = rg.findViewById(radioId)
        Log.d(LOG_TAG,"Your choice: " + radioButton.text)
        Toast.makeText(
            requireContext(), "Selected Radio Button: " + radioButton.text,
            Toast.LENGTH_SHORT
        ).show()
    }
}