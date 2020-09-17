package com.cbs.demospike

import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.View

/**
 * This class demonstrates how to extend [androidx.leanback.app.ErrorFragment].
 */
class ErrorFragment : androidx.leanback.app.ErrorSupportFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = resources.getString(R.string.app_name)
    }

    internal fun setErrorContent() {
        activity?.apply {
            imageDrawable = ContextCompat.getDrawable(this, R.drawable.lb_ic_sad_cloud)
            message = resources.getString(R.string.error_fragment_message)
            setDefaultBackground(TRANSLUCENT)

            buttonText = resources.getString(R.string.dismiss_error)
            buttonClickListener = View.OnClickListener {
                this.supportFragmentManager.beginTransaction().remove(this@ErrorFragment).commit()
            }
        }
    }

    companion object {
        private val TRANSLUCENT = true
    }
}
