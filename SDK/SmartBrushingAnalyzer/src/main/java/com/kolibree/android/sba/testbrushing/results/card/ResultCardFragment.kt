package com.kolibree.android.sba.testbrushing.results.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.kolibree.android.app.extensions.setPartUnderlineText
import com.kolibree.android.sba.R

internal abstract class ResultCardFragment : Fragment() {

    private lateinit var title: TextView
    private lateinit var body: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_result_card, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = view.findViewById(R.id.result_card_title)
        title.setText(titleRes())

        body = view.findViewById(R.id.result_card_body)

        if (hintRes() == 0) {
            body.text = body()
        } else {
            val bodyText = body()
            val hintText = getString(hintRes())
            val fullText = "$bodyText $hintText"
            body.setPartUnderlineText(fullText, hintText, R.color.colorPrimaryDark) {
                onHintClick()
            }
        }
    }

    @StringRes
    abstract fun titleRes(): Int

    abstract fun body(): String

    open fun hintRes() = 0

    open fun onHintClick() {}
}
