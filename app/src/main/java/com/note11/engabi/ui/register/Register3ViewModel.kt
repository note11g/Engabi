package com.note11.engabi.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.note11.engabi.model.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Register3ViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val userDB = db.collection("users")

    fun registerUser(user: UserModel, callback: (Exception?) -> Unit) {
        userDB.document(user.uid).set(user)
            .addOnSuccessListener { callback(null) }
            .addOnFailureListener { callback(it) }
    }

}