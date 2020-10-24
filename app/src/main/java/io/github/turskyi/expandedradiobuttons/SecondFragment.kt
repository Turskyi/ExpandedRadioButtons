package io.github.turskyi.expandedradiobuttons

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import io.github.turskyi.expandedradiobuttons.FirstFragment.Companion.LOG_TAG


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment(), RadioGroup.OnCheckedChangeListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.view_dropdown_radiobuttons, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
                val size = 10 // total number of radioButtons to add
        val root = view.findViewById<RadioGroup>(R.id.content)
        root.setOnCheckedChangeListener(this)
        val radioButtons = arrayOfNulls<RadioButton>(size) // create an empty array;
        val ltInflater = layoutInflater
        val nullParent: ViewGroup? = null

        for (i in 1 until size) {
            // create a new textview
            val radioButton = ltInflater.inflate(R.layout.radio_button, nullParent, false)

            // set some properties of rowRadioButton or something
            (radioButton as RadioButton).text = "This is row #$i"

            // add the radio button to the radio group
            root.addView(radioButton)

            // save a reference to the radio button for later
            radioButtons[i] = radioButton
        }

        Log.d(LOG_TAG, "text 2 : ${radioButtons[2]?.text}")


//        view.findViewById<Button>(R.id.button_second).setOnClickListener {
//            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
//        }
    }

    override fun onCheckedChanged(rg: RadioGroup, radioId: Int) {
//        val radioId = radioGroup.checkedRadioButtonId
       val radioButton: RadioButton = rg.findViewById(radioId)
        Log.d(LOG_TAG,"Your choice: " + radioButton.text)
        Toast.makeText(
            requireContext(), "Selected Radio Button: " + radioButton.text,
            Toast.LENGTH_SHORT
        ).show()
    }
}