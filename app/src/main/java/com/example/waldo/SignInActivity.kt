package com.example.waldo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignInActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val TAG = "GoogleSignIn"
    }

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        // Inicializando FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Configuración de Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Inicializando el ActivityResultLauncher para Google Sign-In
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }

        // Elementos de layout
        val emailEt = findViewById<EditText>(R.id.emailEt)
        val passEt = findViewById<EditText>(R.id.passET)
        val signInButton = findViewById<Button>(R.id.button)
        val signUpTextView = findViewById<TextView>(R.id.textView)
        val errorTextView = findViewById<TextView>(R.id.errorTextView)
        val googleSignInButton = findViewById<com.google.android.gms.common.SignInButton>(R.id.googleSignInButton)

        // Redirigir al SignUpActivity si no tiene cuenta
        signUpTextView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Lógica del botón de Sign In con email y contraseña
        signInButton.setOnClickListener {
            val email = emailEt.text.toString()
            val pass = passEt.text.toString()

            errorTextView.text = ""
            errorTextView.visibility = TextView.GONE

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        errorTextView.text = "Error: ${it.exception?.message}"
                        errorTextView.visibility = TextView.VISIBLE
                    }
                }
            } else {
                errorTextView.text = "All fields must be filled"
                errorTextView.visibility = TextView.VISIBLE
            }
        }

        // Lógica del botón de Google Sign-In
        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    // Iniciar el proceso de Google Sign-In usando ActivityResultLauncher
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    // Manejar el resultado del Sign-In con Google
    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle: ${account.id}")
            val idToken = account.idToken
            if (idToken != null) {
                Log.d(TAG, "Received Google ID Token (JWT): $idToken")
                firebaseAuthWithGoogle(idToken)
            } else {
                Log.w(TAG, "Google sign in failed: ID Token is null")
            }
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed", e)
        }
    }

    // Autenticarse en Firebase con el token de Google
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "signInWithCredential:success")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Log.w(TAG, "signInWithCredential:failure", task.exception)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Si el usuario ya está autenticado, redirigir a MainActivity
        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
