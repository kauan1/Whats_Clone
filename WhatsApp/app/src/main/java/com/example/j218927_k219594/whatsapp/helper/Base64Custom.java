package com.example.j218927_k219594.whatsapp.helper;

import android.util.Base64;

public class Base64Custom {

    public static String codificarBase64(String text){
        return Base64.encodeToString(text.getBytes(), Base64.DEFAULT).replaceAll("(\\n|\\r)","");
    }

    public static String decodificarBase64(String textDecodificado){
        return new String(Base64.decode(textDecodificado, Base64.DEFAULT));
    }

}
