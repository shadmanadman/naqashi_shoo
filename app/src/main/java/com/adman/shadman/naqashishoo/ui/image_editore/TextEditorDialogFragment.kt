package com.adman.shadman.naqashishoo.ui.image_editore

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adman.shadman.naqashishoo.R
import com.adman.shadman.naqashishoo.adapter.ColorPickerAdapter
import com.adman.shadman.naqashishoo.adapter.ColorPickerAdapter.OnColorPickerClickListener


class TextEditorDialogFragment : DialogFragment() {
    private var mAddTextEditText: EditText? = null
    private var mAddTextDoneTextView: TextView? = null
    private var mInputMethodManager: InputMethodManager? = null
    private var mColorCode = 0
    private var mTextEditor: TextEditor? = null

    interface TextEditor {
        fun onDone(inputText: String?, colorCode: Int)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog = getDialog()!!
        //Make dialog full screen with transparent background
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.getWindow()!!.setLayout(width, height)
            dialog.getWindow()!!
                .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.add_text_dialog, container, false)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAddTextEditText = view.findViewById(R.id.add_text_edit_text)
        mInputMethodManager =
            context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mAddTextDoneTextView = view.findViewById(R.id.add_text_done_tv)

        //Setup the color picker for text color
        val addTextColorPickerRecyclerView: RecyclerView =
            view.findViewById(R.id.add_text_color_picker_recycler_view)
        val layoutManager =
            LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false)
        addTextColorPickerRecyclerView.layoutManager = layoutManager
        addTextColorPickerRecyclerView.setHasFixedSize(true)
        val colorPickerAdapter = ColorPickerAdapter(context!!)
        //This listener will change the text color when clicked on any color from picker
        colorPickerAdapter.setOnColorPickerClickListener(object : OnColorPickerClickListener {
            override fun onColorPickerClickListener(colorCode: Int) {
                mColorCode = colorCode
                mAddTextEditText!!.setTextColor(colorCode)
            }
        })
        addTextColorPickerRecyclerView.adapter = colorPickerAdapter
        mAddTextEditText!!.setText(EXTRA_INPUT_TEXT)
        mColorCode = getArguments()!!.getInt(EXTRA_COLOR_CODE)
        mAddTextEditText!!.setTextColor(mColorCode)
        mInputMethodManager!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        //Make a callback on activity when user is done with text editing
        mAddTextDoneTextView!!.setOnClickListener{
                mInputMethodManager!!.hideSoftInputFromWindow(view.getWindowToken(), 0)
                dismiss()
                val inputText = mAddTextEditText!!.text.toString()
                if (!TextUtils.isEmpty(inputText) && mTextEditor != null) {
                    mTextEditor!!.onDone(inputText, mColorCode)
                }

        }
    }

    //Callback to listener if user is done with text editing
    fun setOnTextEditorListener(textEditor: TextEditor?) {
        mTextEditor = textEditor
    }

    companion object {
        val TAG = TextEditorDialogFragment::class.java.simpleName
        const val EXTRA_INPUT_TEXT = "متن نمونه"
        const val EXTRA_COLOR_CODE = "رنگ نمونه"

        //Show dialog with provide text and text color
        //Show dialog with default text input as empty and text color white
        @JvmOverloads
        fun show(
            appCompatActivity: AppCompatActivity,
            inputText: String =
                "",
            @ColorInt colorCode: Int = ContextCompat.getColor(appCompatActivity, R.color.white)
        ): TextEditorDialogFragment {
            val args = Bundle()
            args.putString(EXTRA_INPUT_TEXT, inputText)
            args.putInt(EXTRA_COLOR_CODE, colorCode)
            val fragment = TextEditorDialogFragment()
            fragment.setArguments(args)
            fragment.show(
                appCompatActivity.supportFragmentManager,
                TAG
            )
            return fragment
        }
    }
}