package com.example.projetofinal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetofinal.R;
import com.example.projetofinal.model.Livro;

import java.util.List;

/**
 * Adapter para exibir a lista de livros no RecyclerView.
 */
public class AdapterLivro extends RecyclerView.Adapter<AdapterLivro.MyViewHolder> {

    private List<Livro> listaLivros;

    /**
     * Construtor que recebe a lista inicial de livros.
     */
    public AdapterLivro(List<Livro> listaLivros) {
        this.listaLivros = listaLivros;
    }

    /**
     * Infla o layout de cada item da lista.
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_livro, parent, false);
        return new MyViewHolder(itemLista);
    }

    /**
     * Vincula os dados do livro aos componentes visuais do item.
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Livro livro = listaLivros.get(position);
        Context ctx = holder.itemView.getContext();

        holder.txtTitulo.setText(livro.getTitulo());
        holder.txtAutorAno.setText(ctx.getString(R.string.formato_autor_ano, livro.getAutor(), livro.getAno()));

        if (livro.isLido()) {
            holder.txtStatus.setText(R.string.status_lido);
            holder.txtStatus.setBackgroundColor(ContextCompat.getColor(ctx, R.color.cor_status_lido));
        } else {
            holder.txtStatus.setText(R.string.status_nao_lido);
            holder.txtStatus.setBackgroundColor(ContextCompat.getColor(ctx, R.color.cor_status_nao_lido));
        }
    }

    /**
     * Retorna a quantidade de itens na lista.
     */
    @Override
    public int getItemCount() {
        return listaLivros != null ? listaLivros.size() : 0;
    }

    /**
     * Substitui a lista de livros e atualiza o RecyclerView.
     */
    public void atualizarLista(List<Livro> novaLista) {
        this.listaLivros = novaLista;
        notifyDataSetChanged();
    }

    /**
     * Retorna o livro na posição indicada.
     */
    public Livro getLivro(int position) {
        return listaLivros.get(position);
    }

    /**
     * ViewHolder que mantém as referências das views de cada item.
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitulo;
        TextView txtAutorAno;
        TextView txtStatus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtAutorAno = itemView.findViewById(R.id.txtAutorAno);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}
