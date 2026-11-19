package com.example.projetofinal;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Utilitário para centralizar a configuração do BottomNavigationView e navegação entre telas.
 */
public class NavegacaoMenu {

    /**
     * Configura o menu de navegação inferior na Activity informada.
     */
    public static void configurar(final Activity activity, final int itemAtual) {
        BottomNavigationView nav = activity.findViewById(R.id.bottomNav);
        if (nav == null) {
            return;
        }

        // Define o item selecionado ANTES de registrar o listener para evitar loops de navegação
        nav.setSelectedItemId(itemAtual);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Se for o mesmo item da tela atual, consome o evento sem recriar
            if (id == itemAtual) {
                return true;
            }

            if (id == R.id.nav_livros) {
                irPara(activity, new Intent(activity, ListaLivrosActivity.class));
                return true;
            } else if (id == R.id.nav_novo) {
                irPara(activity, new Intent(activity, CadastroLivroActivity.class));
                return true;
            } else if (id == R.id.nav_resumo) {
                irPara(activity, new Intent(activity, ResumoActivity.class));
                return true;
            }

            return false;
        });
    }

    /**
     * Navega para a nova tela sem animação e finaliza a Activity de origem.
     */
    public static void irPara(Activity origem, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        origem.startActivity(intent);
        origem.finish();
    }
}
