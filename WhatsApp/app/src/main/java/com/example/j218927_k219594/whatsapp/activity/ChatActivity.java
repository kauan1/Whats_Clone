package com.example.j218927_k219594.whatsapp.activity;


import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.j218927_k219594.whatsapp.adapter.MensagensAdapter;
import com.example.j218927_k219594.whatsapp.config.ConfiguracaoFirebase;
import com.example.j218927_k219594.whatsapp.helper.Base64Custom;
import com.example.j218927_k219594.whatsapp.helper.UsuarioFirebase;
import com.example.j218927_k219594.whatsapp.model.Conversa;
import com.example.j218927_k219594.whatsapp.model.Grupo;
import com.example.j218927_k219594.whatsapp.model.Mensagem;
import com.example.j218927_k219594.whatsapp.model.Usuario;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.j218927_k219594.whatsapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    public String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private static final int SELECAO_CAMERA  = 100;

    private TextView textViewNome;
    private CircleImageView circleImageFoto;
    private Usuario usuarioDestinatario;
    private Usuario usuarioRemetente;
    private Grupo grupo;
    private EditText editMensagem;
    private ImageView imageCamera;
    private List<Mensagem> mensagems = new ArrayList<>();
    private DatabaseReference database;
    private DatabaseReference mensagensRef;
    private StorageReference storage;
    private ChildEventListener childEventListenerMensagens;

    //identificador usuarios remetente e destinatario
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;

    private RecyclerView recyclerMensagens;
    private MensagensAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Configurações toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        //Configurações iniciais
        textViewNome      = findViewById(R.id.textViewNomeChat);
        circleImageFoto   = findViewById(R.id.circleImageFotoChat);
        editMensagem      = findViewById(R.id.editMensagem);
        recyclerMensagens = findViewById(R.id.recyclerMensagens);
        imageCamera       = findViewById(R.id.imageCamera);

        //recuperar dados do usuario remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
        usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();

        //Recuperar dados usuario destinatario
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            if(bundle.containsKey("chatGrupo")){

                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                textViewNome.setText(grupo.getNome());
                idUsuarioDestinatario = grupo.getId();

                if(grupo.getFoto() != null){

                    Uri uri = Uri.parse(grupo.getFoto());
                    Glide.with(ChatActivity.this)
                            .load(uri)
                            .into(circleImageFoto);

                }else{
                    circleImageFoto.setImageResource(R.drawable.padrao);
                }

            }else{
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                textViewNome.setText(usuarioDestinatario.getNome());


                if(usuarioDestinatario.getFoto() != null){

                    Uri uri = Uri.parse(usuarioDestinatario.getFoto());
                    Glide.with(ChatActivity.this)
                            .load(uri)
                            .into(circleImageFoto);

                }else{
                    circleImageFoto.setImageResource(R.drawable.padrao);
                }

                //recuperar dados usuario destinatario
                idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());
            }

        }

        //Configurar adapter
        adapter = new MensagensAdapter(mensagems, getApplicationContext());

        //Configurar recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagens.setLayoutManager(layoutManager);
        recyclerMensagens.setHasFixedSize(true);
        recyclerMensagens.setAdapter(adapter);

        database = ConfiguracaoFirebase.getFirebaseDatabase();
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        mensagensRef = database.child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);

        //Evento de clique na camera
        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if(i.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(i,SELECAO_CAMERA);

                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Bitmap imagem = null;

            try {

                switch (requestCode) {
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                }
                if (imagem != null){

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70,baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //criar nome da imagem
                    String nomeImage = UUID.randomUUID().toString();

                    //configurar referencia firebase
                    final StorageReference imagemRef = storage.child("imagens")
                                                        .child("fotos")
                                                        .child(idUsuarioRemetente)
                                                        .child(nomeImage);

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Erro", "Erro ao fazer upload");
                            Toast.makeText(ChatActivity.this,
                                    "Erro ao fazer upload da imagem",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            if(usuarioDestinatario != null){

                                imagemRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Mensagem mensagem = new Mensagem();
                                        mensagem.setIdUsuario(idUsuarioRemetente);
                                        mensagem.setMensagem("imagem.jpeg");
                                        mensagem.setImagem(uri.toString());

                                        //salvar imagem remetente
                                        salvarMensagem(idUsuarioRemetente,idUsuarioDestinatario,mensagem);
                                        //salvar imagem destinatario
                                        salvarMensagem(idUsuarioDestinatario,idUsuarioRemetente,mensagem);

                                    }
                                });

                            }else{

                                    imagemRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            for( Usuario membro : grupo.getMembros()) {

                                                String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                                                String idUsuairoLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();
                                                Mensagem mensagem = new Mensagem();
                                                mensagem.setIdUsuario(idUsuairoLogadoGrupo);
                                                mensagem.setMensagem("imagem.jpeg");
                                                mensagem.setNome(usuarioRemetente.getNome());
                                                mensagem.setImagem(uri.toString());


                                                //salvar mensagem para o membro
                                                salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                                                //Salva conversa
                                                salvarConversa(idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem, true);
                                            }
                                        }
                                    });




                            }

                            Toast.makeText(ChatActivity.this,
                                    "Sucesso ao enviar da imagem",
                                    Toast.LENGTH_SHORT).show();

                        }
                    });

                }
            }catch (Exception e){
                e.printStackTrace();
            }
            }

    }

    public void enviarMensagem(View view){

        String textoMensagem = editMensagem.getText().toString();

        if(!textoMensagem.isEmpty()){

            if(usuarioDestinatario != null){

                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioRemetente);
                mensagem.setMensagem(textoMensagem);

                //Salvar mensagem remetente
                salvarMensagem(idUsuarioRemetente,idUsuarioDestinatario,mensagem);
                //Salvar mensagem destinatario
                salvarMensagem(idUsuarioDestinatario,idUsuarioRemetente,mensagem);

                //Salva conversa Remetente
                salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, usuarioDestinatario, mensagem,false);
                //Salva conversa Destinatario
                salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente, mensagem,false);

            }else{

                for(Usuario membro : grupo.getMembros()){


                    String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                    String idUsuairoLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuairoLogadoGrupo);
                    mensagem.setMensagem(textoMensagem);
                    mensagem.setNome(usuarioRemetente.getNome());

                    //salvar mensagem para o membro
                    salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                    //Salva conversa
                    salvarConversa(idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem,true);

                }

            }



        }else{
            Toast.makeText(this,
                    "Digite uma mensagem para enviar!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void salvarMensagem(String idRemetente, String idDestinatario, Mensagem mensagem){
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference menDatabaseRef = database.child("mensagens");
        menDatabaseRef.child(idRemetente)
                        .child(idDestinatario)
                        .push()
                        .setValue(mensagem);
        editMensagem.setText("");
    }

    private void salvarConversa(String idRemetente, String idDestinatario, Usuario usuarioExibicao, Mensagem msg, boolean isGroup){

        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente(idRemetente);
        conversaRemetente.setIdDestinatario(idDestinatario);
        conversaRemetente.setUltimaMensagem(msg.getMensagem());

        if(isGroup){//conversa de grupo

            conversaRemetente.setGrupo(grupo);
            conversaRemetente.setIsGroup("true");

        }else {//conversa privada

            conversaRemetente.setUsuarioExibicao(usuarioExibicao);
            conversaRemetente.setIsGroup("false");

        }

        conversaRemetente.salvar();


    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListenerMensagens);

    }

    private void recuperarMensagens(){
        mensagems.clear();

        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Mensagem mensagem = dataSnapshot.getValue(Mensagem.class);
                mensagems.add(mensagem);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
