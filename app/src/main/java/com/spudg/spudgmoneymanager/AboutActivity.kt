package com.spudg.spudgmoneymanager

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.dialog_terms_of_use.*

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        var version = packageManager.getPackageInfo(packageName, 0).versionName
        spudg_money_manager_desc.text = "v$version, made by Spudg Studios"

        back_to_trans_from_about.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        privacy_policy.setOnClickListener {
            privacyPolicy()
        }

        terms_of_use.setOnClickListener {
            termsOfUse()
        }

        rate_btn.setOnClickListener {
            rate()
        }

        email_btn.setOnClickListener {
            email()
        }

    }

    private fun privacyPolicy() {
        val url =
            "https://docs.google.com/document/d/1ZUlUR293yLqaEppmmCGrM5JYRNksaFOU2ky7tAU7htE/edit?usp=sharing"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
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
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("spudgstudios@gmail.com"))
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

    private fun rate() {
        val url =
            "https://play.google.com/store/apps/details?id=com.spudg.spudgmoneymanager"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

}