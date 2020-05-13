package com.example.j218927_k219594.whatsapp.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfiguracaoFirebase {

    private static FirebaseAuth autentificacao;
    private static DatabaseReference firebase;
    private static StorageReference storage;

    //retorna a instancia do FirebaseDatabase
    public static DatabaseReference getFirebaseDatabase(){
        if(firebase == null){
            firebase = FirebaseDatabase.getInstance().getReference();
        }
        return firebase;
    }

    //retorna a instacia do FirebaseAuth
    public static FirebaseAuth getFirebaseAutenticacao(){
        if(autentificacao == null) {
            autentificacao = FirebaseAuth.getInstance();
        }
        return autentificacao;
    }

    public static StorageReference getFirebaseStorage(){
        if(storage == null) {
            storage = FirebaseStorage.getInstance().getReference();
        }
        return storage;

    }

}
