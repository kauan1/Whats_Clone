package com.example.j218927_k219594.whatsapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.j218927_k219594.whatsapp.R;
import com.example.j218927_k219594.whatsapp.activity.ChatActivity;
import com.example.j218927_k219594.whatsapp.activity.GrupoActivity;
import com.example.j218927_k219594.whatsapp.adapter.ContatosAdapter;
import com.example.j218927_k219594.whatsapp.adapter.ConversasAdapter;
import com.example.j218927_k219594.whatsapp.config.ConfiguracaoFirebase;
import com.example.j218927_k219594.whatsapp.helper.RecyclerItemClickListener;
import com.example.j218927_k219594.whatsapp.helper.UsuarioFirebase;
import com.example.j218927_k219594.whatsapp.model.Conversa;
import com.example.j218927_k219594.whatsapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContatosFragment extends Fragment {

    private RecyclerView recyclerView;
    private ContatosAdapter adapter;
    private ArrayList<Usuario> listaContatos = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private ValueEventListener valueEventListenerContatos;
    private FirebaseUser usuarioAtual;


    public ContatosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_contatos,container,false);

        //Configurações iniciais
        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        recyclerView = view.findViewById(R.id.recyclerViewListaContatos);
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        //Configurar Adapter
        adapter = new ContatosAdapter(listaContatos,getActivity());

        //configurar Recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize( true );
        recyclerView.setAdapter(adapter);

        //Configurar evento de clique no Recyclerview
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerView,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Usuario> listaContatosAtualizada = adapter.getContatos();

                                Usuario usuarioSelecionado = listaContatosAtualizada.get(position);
                                boolean cabecalho = usuarioSelecionado.getEmail().isEmpty();

                                if(cabecalho){
                                    Intent i = new Intent(getActivity(), GrupoActivity.class);
                                    startActivity(i);
                                }else {
                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatContato", usuarioSelecionado);
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
                )
        );



        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener(valueEventListenerContatos);
        listaContatos.clear();
    }

    public void recuperarContatos(){

        adicionarMenuNovoGrupo();

        valueEventListenerContatos = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                limparListaContatos();

                for(DataSnapshot data : dataSnapshot.getChildren()){

                    Usuario usuario = data.getValue(Usuario.class);

                    if(!usuarioAtual.getEmail().equals(usuario.getEmail())) {
                        listaContatos.add(usuario);
                    }
                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void adicionarMenuNovoGrupo() {
        Usuario itemGrupo = new Usuario();
        itemGrupo.setNome("Novo grupo");
        itemGrupo.setEmail("");

        listaContatos.add(itemGrupo);
    }

    public void limparListaContatos(){

        listaContatos.clear();
        adicionarMenuNovoGrupo();

    }


    public void pesquisarContatos(String texto){

        List<Usuario> listaContatosBusca = new ArrayList<>();

        for(Usuario usuario : listaContatos){


                String nome = usuario.getNome().toLowerCase();
                if(nome.contains(texto) ){
                    listaContatosBusca.add(usuario);
                }



        }

        adapter = new ContatosAdapter(listaContatosBusca,getActivity());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void recarregarContatos(){
        adapter = new ContatosAdapter(listaContatos,getActivity());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}
