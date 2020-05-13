package com.example.j218927_k219594.whatsapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.j218927_k219594.whatsapp.R;
import com.example.j218927_k219594.whatsapp.model.Conversa;
import com.example.j218927_k219594.whatsapp.model.Grupo;
import com.example.j218927_k219594.whatsapp.model.Usuario;

import java.util.List;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversasAdapter extends RecyclerView.Adapter<ConversasAdapter.MyViewHolder> {

    private List<Conversa> conversas;
    private Context context;

    public ConversasAdapter(List<Conversa> listaConversas, Context c) {
        this.conversas = listaConversas;
        this.context = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_contato_adapter, parent, false);
        return new MyViewHolder(itemLista);
    }

    public List<Conversa> getConversas(){
        return this.conversas;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Conversa conversa = conversas.get(position);


        if(conversa.getIsGroup().equals("true")){

            Grupo grupo = conversa.getGrupo();
            holder.nome.setText(grupo.getNome());

            if (grupo.getFoto() != null) {
                Uri uri = Uri.parse(grupo.getFoto());
                Glide.with(context).load(uri).into(holder.foto);
            } else {
                holder.foto.setImageResource(R.drawable.padrao);
            }

        }else {
            Usuario usuario = conversa.getUsuarioExibicao();
            holder.nome.setText(usuario.getNome());

            if(usuario !=null){


                if (usuario.getFoto() != null) {
                    Uri uri = Uri.parse(usuario.getFoto());
                    Glide.with(context).load(uri).into(holder.foto);
                } else {
                    holder.foto.setImageResource(R.drawable.padrao);
                }
            }


        }

        holder.ultimaMensagem.setText(conversa.getUltimaMensagem());

    }

    @Override
    public int getItemCount() {
        return conversas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome,ultimaMensagem;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            foto = itemView.findViewById(R.id.circleImageViewFotoContato);
            nome = itemView.findViewById(R.id.textViewNomeContato);
            ultimaMensagem = itemView.findViewById(R.id.textViewEmailContato);

        }
    }

}
