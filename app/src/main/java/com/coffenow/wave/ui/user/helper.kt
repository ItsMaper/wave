package com.coffenow.wave.ui.user

import android.content.Context
import android.content.Intent
import com.coffenow.wave.activities.AuthActivity
import com.coffenow.wave.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth

fun Context.login(email:String){
    val intent= Intent(this, MainActivity::class.java).apply{
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
}

fun Context.logout(){
    FirebaseAuth.getInstance().signOut()
    val intent = Intent(this, AuthActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
}