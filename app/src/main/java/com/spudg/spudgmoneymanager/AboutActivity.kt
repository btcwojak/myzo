package com.spudg.spudgmoneymanager

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.dialog_privacy_policy.*
import kotlinx.android.synthetic.main.dialog_terms_of_use.*

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        var version = packageManager.getPackageInfo(packageName, 0).versionName
        spudg_money_manager_desc.text = "v$version, made by Spudg Software"

        back_to_trans_from_about.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        /*
        privacy_policy.setOnClickListener {
            privacyPolicy()
        }
        */

        terms_of_use.setOnClickListener {
            termsOfUse()
        }

        email_btn.setOnClickListener {
            email()
        }

    }

    private fun privacyPolicy() {
        val privacyPolicyDialog = Dialog(this, R.style.Theme_Dialog)
        privacyPolicyDialog.setCancelable(false)
        privacyPolicyDialog.setContentView(R.layout.dialog_privacy_policy)
        privacyPolicyDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        privacyPolicyDialog.tvDonePP.setOnClickListener {
            privacyPolicyDialog.dismiss()
        }

        privacyPolicyDialog.show()

    }

    private fun termsOfUse() {
        val termsOfUseDialog = Dialog(this, R.style.Theme_Dialog)
        termsOfUseDialog.setCancelable(false)
        termsOfUseDialog.setContentView(R.layout.dialog_terms_of_use)
        termsOfUseDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        termsOfUseDialog.tvDoneTU.setOnClickListener {
            termsOfUseDialog.dismiss()
        }

        termsOfUseDialog.show()


    }

    private fun email() {

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("softwarespudg@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "SMM - Suggestion / bug report")

        try {
            startActivity(Intent.createChooser(intent, "Send mail..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "There are no email clients installed.",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

}