package com.example.j218927_k219594.whatsapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.j218927_k219594.whatsapp.R;
import com.example.j218927_k219594.whatsapp.activity.ChatActivity;
import com.example.j218927_k219594.whatsapp.adapter.ContatosAdapter;
import com.example.j218927_k219594.whatsapp.adapter.ConversasAdapter;
import com.example.j218927_k219594.whatsapp.config.ConfiguracaoFirebase;
import com.example.j218927_k219594.whatsapp.helper.RecyclerItemClickListener;
import com.example.j218927_k219594.whatsapp.helper.UsuarioFirebase;
import com.example.j218927_k219594.whatsapp.model.Conversa;
import com.example.j218927_k219594.whatsapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversasFragment extends Fragment {

    private RecyclerView recyclerView;
    private ConversasAdapter conversasAdapter;
    private ArrayList<Conversa> listaConversa = new ArrayList<>();
    private DatabaseReference conversaRef;
    private ChildEventListener valueEventListenerConversas;
    private FirebaseUser usuarioAtual;

    public ConversasFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_conversas, container, false);

        //Configurações Iniciais
        recyclerView = view.findViewById(R.id.recyclerViewListaConversas);
        conversaRef = ConfiguracaoFirebase.getFirebaseDatabase().child("conversas").child(UsuarioFirebase.getIdentificadorUsuario());
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        //Configurar adapter
        conversasAdapter = new ConversasAdapter(listaConversa,getActivity());

        //Configurar recycler
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(conversasAdapter);

        //Configurar evento de clique
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerView,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Conversa> listaConversaAtualizada = conversasAdapter.getConversas();
                                Conversa conversaSelecionada = listaConversaAtualizada.get(position);

                                if(conversaSelecionada.getIsGroup().equals("true")){
                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatGrupo", conversaSelecionada.getGrupo());
                                    startActivity(i);
                                }else {
                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatContato", conversaSelecionada.getUsuarioExibicao());
                                    startActivity(i);
                                }
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                ));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarConversas();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversaRef.removeEventListener(valueEventListenerConversas);
        listaConversa.clear();
    }

    public void pesquisarConversa(String texto){

        List<Conversa> listaConversaBusca = new ArrayList<>();

        for(Conversa conversa : listaConversa){

            if(conversa.getUsuarioExibicao() != null){
                String nome = conversa.getUsuarioExibicao().getNome().toLowerCase();
                String mensagem = conversa.getUltimaMensagem().toLowerCase();
                if(nome.contains(texto) || mensagem.contains(texto)){
                    listaConversaBusca.add(conversa);
                }
            }else{
                String nome = conversa.getGrupo().getNome().toLowerCase();
                String mensagem = conversa.getUltimaMensagem().toLowerCase();
                if(nome.contains(texto) || mensagem.contains(texto)){
                    listaConversaBusca.add(conversa);
                }
            }


        }

        conversasAdapter = new ConversasAdapter(listaConversaBusca,getActivity());
        recyclerView.setAdapter(conversasAdapter);
        conversasAdapter.notifyDataSetChanged();

    }

    public void recarregarConversas(){
        conversasAdapter = new ConversasAdapter(listaConversa,getActivity());
        recyclerView.setAdapter(conversasAdapter);
        conversasAdapter.notifyDataSetChanged();
    }

    public void recuperarConversas(){
        listaConversa.clear();

        valueEventListenerConversas = conversaRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Conversa conversa = dataSnapshot.getValue(Conversa.class);
                listaConversa.add(conversa);

                conversasAdapter.notifyDataSetChanged();
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
