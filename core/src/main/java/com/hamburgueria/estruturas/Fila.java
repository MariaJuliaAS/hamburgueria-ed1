package com.hamburgueria.estruturas;

/**
 * Fila genérica (FIFO) implementada com nós encadeados.
 * Enfileirar e desenfileirar são O(1) porque guardamos início e fim.
 * No jogo: clientes esperando atendimento.
 */
public class Fila<T> {

    private No<T> noInicio;
    private No<T> noFim;
    private int tamanho;

    public Fila() {
        this.noInicio = null;
        this.noFim = null;
        this.tamanho = 0;
    }

    public void enfileirar(T dado) {
        No<T> novo = new No<>(dado);
        if (noFim == null) {
            noInicio = novo;
            noFim = novo;
        } else {
            noFim.setProximo(novo);
            noFim = novo;
        }
        tamanho++;
    }

    public T desenfileirar() {
        if (estaVazia()) {
            throw new IllegalStateException("Fila vazia");
        }
        T dado = noInicio.getDado();
        noInicio = noInicio.getProximo();
        if (noInicio == null) {
            noFim = null;
        }
        tamanho--;
        return dado;
    }

    /** Olha o primeiro da fila sem remover. */
    public T primeiro() {
        if (estaVazia()) {
            throw new IllegalStateException("Fila vazia");
        }
        return noInicio.getDado();
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }

    public int tamanho() {
        return tamanho;
    }

    public void limpar() {
        noInicio = null;
        noFim = null;
        tamanho = 0;
    }

    /**
     * Nó do início, para percorrer a fila sem removê-la (por exemplo, ao desenhar os clientes):
     * for (No<T> n = fila.getNoInicio(); n != null; n = n.getProximo())
     */
    public No<T> getNoInicio() {
        return noInicio;
    }
}
