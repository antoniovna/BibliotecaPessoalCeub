package com.example.projetofinal;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projetofinal.persistencia.ILivroDAO;
import com.example.projetofinal.persistencia.LivroDAO;

/**
 * Activity que exibe o resumo com totais de livros cadastrados, lidos e não lidos.
 */
public class ResumoActivity extends AppCompatActivity {

    private TextView txtTotal;
    private TextView txtLidos;
    private TextView txtNaoLidos;
    private ILivroDAO dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resumo);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        NavegacaoMenu.configurar(this, R.id.nav_resumo);

        txtTotal = findViewById(R.id.txtTotal);
        txtLidos = findViewById(R.id.txtLidos);
        txtNaoLidos = findViewById(R.id.txtNaoLidos);

        dao = new LivroDAO(this);

        carregarContadores();
    }

    /**
     * Carrega as estatísticas do banco de dados utilizando COUNT(*) via DAO.
     */
    private void carregarContadores() {
        txtTotal.setText(getString(R.string.resumo_total, dao.contar(null)));
        txtLidos.setText(getString(R.string.resumo_lidos, dao.contar(true)));
        txtNaoLidos.setText(getString(R.string.resumo_nao_lidos, dao.contar(false)));
    }
}
