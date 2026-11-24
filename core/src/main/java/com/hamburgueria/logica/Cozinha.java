package com.hamburgueria.logica;

import com.hamburgueria.estruturas.Pilha;
import com.hamburgueria.modelo.Ingrediente;

/**
 * A bancada onde o lanche é montado, camada por camada, numa Pilha.
 * O último ingrediente colocado é o primeiro a poder ser desfeito.
 */
public class Cozinha {

    /** Número máximo de camadas, para o lanche não crescer sem limite. */
    public static final int ALTURA_MAXIMA = 8;

    private final Pilha<Ingrediente> montagem = new Pilha<>();

    /** Coloca um ingrediente no topo. Retorna false se já atingiu a altura máxima. */
    public boolean adicionar(Ingrediente ingrediente) {
        if (montagem.tamanho() >= ALTURA_MAXIMA) {
            return false;
        }
        montagem.empilhar(ingrediente);
        return true;
    }

    /** Tira o ingrediente do topo. Retorna false se não havia nada. */
    public boolean desfazer() {
        if (montagem.estaVazia()) {
            return false;
        }
        montagem.desempilhar();
        return true;
    }

    public void limpar() {
        montagem.limpar();
    }

    public boolean estaVazia() {
        return montagem.estaVazia();
    }

    public int tamanho() {
        return montagem.tamanho();
    }

    public Pilha<Ingrediente> getMontagem() {
        return montagem;
    }
}
