package com.hamburgueria.logica;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/** Persistencia simples do ranking no diretorio local do usuario. */
public final class RankingArquivo {

    private RankingArquivo() {
    }

    /** Adiciona uma linha no formato nome|pontos ao arquivo local ranking.txt. */
    public static void salvar(String nome, int pontos) {
        String nomeSeguro = nome == null ? "Jogador" : nome.trim();
        if (nomeSeguro.isEmpty()) {
            nomeSeguro = "Jogador";
        }
        // Evita que o caractere separador quebre o formato do arquivo.
        nomeSeguro = nomeSeguro.replace('|', ' ').replace('\n', ' ').replace('\r', ' ');

        try {
            FileHandle arquivo = Gdx.files.local("ranking.txt");
            arquivo.writeString(nomeSeguro + "|" + pontos + "\n", true, "UTF-8");
            Gdx.app.log("Ranking", "Resultado salvo em " + arquivo.file().getAbsolutePath());
        } catch (Exception erro) {
            Gdx.app.error("Ranking", "Nao foi possivel salvar o ranking.", erro);
        }
    }
}
