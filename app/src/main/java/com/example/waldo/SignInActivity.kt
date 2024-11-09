package com.example.waldo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.waldo.API.REST
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Models.User
import com.example.waldo.Repository.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var userRepository: UserRepository

    companion object {
        private const val TAG = "GoogleSignIn"
        private const val PREFS_NAME = "com.example.waldo"
        private const val FIRST_RUN_KEY = "first_run"
    }

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        if (isFirstRun()) {
            signOutFromGoogle()
        }

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }

        val googleSignInButton = findViewById<com.google.android.gms.common.SignInButton>(R.id.googleSignInButton)
        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        userRepository = UserRepository(REST.getRestEngine().create(ApiService::class.java), this)
    }

    private fun isFirstRun(): Boolean {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean(FIRST_RUN_KEY, true)
        if (isFirstRun) {
            sharedPreferences.edit().putBoolean(FIRST_RUN_KEY, false).apply()
        }
        return isFirstRun
    }

    private fun signInWithGoogle() {
        // Revoca el acceso antes de mostrar el flujo de inicio de sesión
        googleSignInClient.revokeAccess().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun signOutFromGoogle() {
        firebaseAuth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInClient.revokeAccess().addOnCompleteListener {
                Log.d("SignInActivity", "Logged out and access revoked for Google account")
            }
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle: ${firebaseAuth.currentUser?.uid}")
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d(TAG, "JWT Token recibido: ${account.idToken}")
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java)
                    .putExtra("idUser", firebaseAuth.currentUser?.uid.toString())

                Log.d(TAG, "UID usado ${firebaseAuth.currentUser?.uid.toString()}")

                userRepository.createUser(
                    User(
                        firebaseAuth.currentUser?.uid.toString(),
                        account.familyName.toString(),
                        account.givenName.toString(),
                        account.email.toString(),
                        "parent"
                    )
                ).enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            val token = response.body()?.token
                            if (token != null) {
                                userRepository.saveToken(token)
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Log.e(TAG, "Error en la respuesta: datos no enviados")
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        Log.e(TAG, "Error al crear el usuario", t)
                    }
                })
            } else {
                Log.w(TAG, "signInWithCredential:failure", task.exception)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
