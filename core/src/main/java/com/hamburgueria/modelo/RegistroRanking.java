package com.hamburgueria.modelo;

import com.hamburgueria.estruturas.Comparador;

/** Uma linha do ranking: nome do jogador e pontuação final. */
public class RegistroRanking {

    /** Maior pontuação primeiro. Usado tanto na ordenação quanto na busca binária do ranking. */
    public static final Comparador<RegistroRanking> POR_PONTOS_DECRESCENTE =
            (a, b) -> Integer.compare(b.pontos, a.pontos);

    private final String nome;
    private final int pontos;

    public RegistroRanking(String nome, int pontos) {
        this.nome = nome;
        this.pontos = pontos;
    }

    public String getNome() {
        return nome;
    }

    public int getPontos() {
        return pontos;
    }

    @Override
    public String toString() {
        return nome + " - " + pontos;
    }
}
