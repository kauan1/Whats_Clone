package com.example.j218927_k219594.whatsapp.model;

import com.example.j218927_k219594.whatsapp.config.ConfiguracaoFirebase;
import com.example.j218927_k219594.whatsapp.helper.UsuarioFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Usuario implements Serializable {

    private String idUsuario;
    private String nome;
    private String email;
    private String senha;
    private String foto;

    public Usuario() {
    }

    @Exclude
    public String getIdUsuario() {
        return idUsuario;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void salvar(){
        DatabaseReference firebase = ConfiguracaoFirebase.getFirebaseDatabase();
        firebase.child("usuarios")
                .child(this.idUsuario)
                .setValue(this);
    }

    public void atualizar(){

        String idUsuario = UsuarioFirebase.getIdentificadorUsuario();
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();

        DatabaseReference usuarioRef = database.child("usuarios")
                                    .child(idUsuario);

        Map<String,Object> valorUsuario = converterParaMap();

        usuarioRef.updateChildren(valorUsuario);

    }

    @Exclude
    public Map<String, Object> converterParaMap(){
        HashMap<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("email", getEmail());
        usuarioMap.put("nome",getNome());
        usuarioMap.put("foto", getFoto());

        return usuarioMap;
    }

}
