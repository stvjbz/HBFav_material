package me.rei_m.hbfavmaterial.fragment

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import com.jakewharton.rxbinding.widget.RxTextView
import me.rei_m.hbfavmaterial.R
import me.rei_m.hbfavmaterial.activity.BaseActivity
import me.rei_m.hbfavmaterial.extension.adjustScreenWidth
import me.rei_m.hbfavmaterial.extension.showSnackbarNetworkError
import me.rei_m.hbfavmaterial.extension.toggle
import me.rei_m.hbfavmaterial.fragment.presenter.EditUserIdDialogContact
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

class EditUserIdDialogFragment() : DialogFragment(),
        EditUserIdDialogContact.View,
        ProgressDialogController {

    companion object {

        val TAG: String = EditUserIdDialogFragment::class.java.simpleName

        fun newInstance(): EditUserIdDialogFragment = EditUserIdDialogFragment()
    }

    @Inject
    lateinit var presenter: EditUserIdDialogContact.Actions

    private var listener: DialogInterface? = null

    private var subscription: CompositeSubscription? = null

    override var progressDialog: ProgressDialog? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window.requestFeature(Window.FEATURE_NO_TITLE)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val listener = targetFragment
        if (listener is DialogInterface) {
            this.listener = listener
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (activity as BaseActivity).component
        component.inject(this)
        presenter.onCreate(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        subscription = CompositeSubscription()

        val view = inflater.inflate(R.layout.dialog_fragment_edit_user_id, container, false)

        with(view.findViewById(R.id.dialog_fragment_edit_user_id_text_title)) {
            this as AppCompatTextView
            text = getString(R.string.dialog_title_set_user)
        }

        val editUserId = view.findViewById(R.id.dialog_fragment_edit_user_id_edit_user_id) as EditText

        val buttonCancel = view.findViewById(R.id.dialog_fragment_edit_user_id_button_cancel) as AppCompatButton
        buttonCancel.setOnClickListener { v ->
            dismiss()
        }

        val buttonOk = view.findViewById(R.id.dialog_fragment_edit_user_id_button_ok) as AppCompatButton
        buttonOk.setOnClickListener { v ->
            val inputtedUserId = editUserId.editableText.toString()
            presenter.onClickButtonOk(inputtedUserId)
        }

        subscription?.add(RxTextView.textChanges(editUserId)
                .map { v -> 0 < v.length }
                .subscribe { isEnabled -> buttonOk.toggle(isEnabled) })

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.onViewCreated()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subscription?.unsubscribe()
        subscription = null
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adjustScreenWidth()
    }

    override fun setEditUserId(userId: String) {
        view?.findViewById(R.id.dialog_fragment_edit_user_id_edit_user_id)?.let {
            it as EditText
            it.setText(userId)
        }
    }

    override fun showNetworkErrorMessage() {
        (activity as AppCompatActivity).showSnackbarNetworkError(view)
    }

    override fun showProgress() {
        showProgressDialog(activity)
    }

    override fun hideProgress() {
        closeProgressDialog()
    }

    override fun displayInvalidUserIdMessage() {
        view?.findViewById(R.id.dialog_fragment_edit_user_id_layout_edit_user)?.let {
            it as TextInputLayout
            it.error = getString(R.string.message_error_input_user_id)
        }
    }

    override fun dismissDialog() {
        view?.findViewById(R.id.dialog_fragment_edit_user_id_layout_edit_user)?.let {
            it as TextInputLayout
            it.isErrorEnabled = false
        }
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        this.listener?.dismiss()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        this.listener?.cancel()
    }
}

