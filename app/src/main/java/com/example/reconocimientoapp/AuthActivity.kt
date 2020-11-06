package com.example.reconocimientoapp

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_auth.*


class AuthActivity : AppCompatActivity() {
    private val GOOGLE_SIGN_IN = 100
    private val callbackManager = CallbackManager.Factory.create()
    private var auth: FirebaseAuth = Firebase.auth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)


        makeregisterBtn.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            // start your next activity
            startActivity(intent)
        }

        setup()
    }
    override fun onStart(){
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser!=null){
            showHome()
        }
    }
    private fun setup (){
        title = getString(R.string.authentication)

        loginBtn.setOnClickListener{
            if ( idEmail.text.isNotEmpty() && idPassword.text.isNotEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(idEmail.text.toString(),idPassword.text.toString()).addOnCompleteListener{
                    if(it.isSuccessful){
                        showHome()
                    }else{
                        showAlert()
                    }
                }
            }
        }


        //GOOGLE
        googleBtn.setOnClickListener{
            //Config
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(this,googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent,GOOGLE_SIGN_IN)
        }

        //FACEBOOK
        facebookBtn.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))
            LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult?) {
                    result?.let{
                        val token = it.accessToken
                        val credential = FacebookAuthProvider.getCredential(token.token)
                        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                            if(it.isSuccessful){
                                Log.d("FacebookSignIn", "signInWithCredential:success")
                                showHome()
                            }else{
                                Log.w("FacebookSignIn", "signInWithCredential:failure", it.exception)
                                showAlert()
                            }
                        }
                    }
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException?) {
                    showAlert()
                    Log.w("FacebookSignIn", "signInWithCredential:failure", error)
                }
            })
        }
    }
    private fun showAlert(err:String ){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        //builder.setMessage("Hubo un error autenticando al usuario.\n Codigo de error: \n $err")
        builder.setMessage(getString(R.string.authenticationError) + "ErrorCode: "+ err)
        //builder.setPositiveButton("aceptar", null)
        builder.setPositiveButton(getString(R.string.ok),null)
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }
    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        //builder.setMessage("Hubo un error autenticando al usuario")
        builder.setMessage(getString(R.string.authenticationError))
        //builder.setPositiveButton("aceptar",null)
        builder.setPositiveButton(getString(R.string.ok),null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
    private fun showHome (){
        val homeIntent = Intent(this,MainActivity::class.java)
        startActivity(homeIntent)
    }


    //Google
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("GoogleSignIn", "signInWithCredential:success")
                    showHome()

                } else {
                    Log.w("GoogleSignIn", "signInWithCredential:failure", task.exception)
                    showAlert(task.result.toString())
                }

            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        callbackManager.onActivityResult(requestCode,resultCode,data)
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!


                Log.d("GoogleSignIn", "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
            }catch (e:ApiException){
                showAlert()
                Log.w("GoogleSignIn", "Google sign in failed", e)
            }

        }
    }
}
