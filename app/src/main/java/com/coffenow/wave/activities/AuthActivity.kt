package com.coffenow.wave.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.coffenow.wave.R
import com.coffenow.wave.databinding.ActivityAuthBinding
import com.coffenow.wave.ui.user.login
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {
    private lateinit var  nAuth : FirebaseAuth
    private val Google_SIGN_IN = 100
    private lateinit var binding : ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        nAuth = FirebaseAuth.getInstance()
        authUser()
    }

    private fun authUser(){
        binding.googleSignIn.setOnClickListener(){
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(this, googleConf)
            val signInItent = googleClient.signInIntent
            startActivityForResult(signInItent,Google_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==Google_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account = task.getResult(ApiException::class.java)!!
                if(account!=null){
                    Log.d("Tag", "firebasegoogleid $account.id")
                    firebaseAuthWithGoogle(account.idToken!!)
                } else{
                    Toast.makeText(this, "correo no existe", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException){
                Log.d("Tag", "google sign in failed $e")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        nAuth.signInWithCredential(credential)
            .addOnCompleteListener(this){task ->
                if (task.isSuccessful){
                    Log.d("Tag", "Sign in Success")
                    val user = nAuth.currentUser?.email.toString()
                    login(user)
                }
            }

    }
}