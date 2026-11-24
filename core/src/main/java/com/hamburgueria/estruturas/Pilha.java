package com.hamburgueria.estruturas;

/**
 * Pilha genérica (LIFO) implementada com nós encadeados.
 * No jogo: montar o hambúrguer camada por camada e desfazer o último ingrediente.
 */
public class Pilha<T> {

    private No<T> noTopo;
    private int tamanho;

    public Pilha() {
        this.noTopo = null;
        this.tamanho = 0;
    }

    public void empilhar(T dado) {
        No<T> novo = new No<>(dado);
        novo.setProximo(noTopo);
        noTopo = novo;
        tamanho++;
    }

    public T desempilhar() {
        if (estaVazia()) {
            throw new IllegalStateException("Pilha vazia");
        }
        T dado = noTopo.getDado();
        noTopo = noTopo.getProximo();
        tamanho--;
        return dado;
    }

    /** Olha o elemento do topo sem remover. */
    public T topo() {
        if (estaVazia()) {
            throw new IllegalStateException("Pilha vazia");
        }
        return noTopo.getDado();
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }

    public int tamanho() {
        return tamanho;
    }

    public void limpar() {
        noTopo = null;
        tamanho = 0;
    }

    /**
     * Nó do topo, para percorrer do topo até a base (por exemplo, ao desenhar o hambúrguer):
     * for (No<T> n = pilha.getNoTopo(); n != null; n = n.getProximo())
     */
    public No<T> getNoTopo() {
        return noTopo;
    }
}
