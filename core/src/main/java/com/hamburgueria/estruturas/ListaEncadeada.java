package com.hamburgueria.estruturas;

/**
 * Lista encadeada simples genérica, implementada do zero (sem java.util).
 * Guarda referência para o início e para o fim, então adicionar no fim é O(1).
 */
public class ListaEncadeada<T> {

    private No<T> inicio;
    private No<T> fim;
    private int tamanho;

    public ListaEncadeada() {
        this.inicio = null;
        this.fim = null;
        this.tamanho = 0;
    }

    /** Adiciona no final da lista. */
    public void adicionar(T dado) {
        No<T> novo = new No<>(dado);
        if (inicio == null) {
            inicio = novo;
            fim = novo;
        } else {
            fim.setProximo(novo);
            fim = novo;
        }
        tamanho++;
    }

    /** Adiciona no começo da lista. */
    public void adicionarInicio(T dado) {
        No<T> novo = new No<>(dado);
        novo.setProximo(inicio);
        inicio = novo;
        if (fim == null) {
            fim = novo;
        }
        tamanho++;
    }

    /** Retorna o elemento na posição indicada (começando em 0). */
    public T obter(int indice) {
        validarIndice(indice);
        return noEm(indice).getDado();
    }

    /** Substitui o elemento da posição indicada. Útil para algoritmos de ordenação. */
    public void definir(int indice, T dado) {
        validarIndice(indice);
        noEm(indice).setDado(dado);
    }

    /** Remove e retorna o elemento da posição indicada. */
    public T removerPosicao(int indice) {
        validarIndice(indice);
        T removido;
        if (indice == 0) {
            removido = inicio.getDado();
            inicio = inicio.getProximo();
            if (inicio == null) {
                fim = null;
            }
        } else {
            No<T> anterior = noEm(indice - 1);
            No<T> alvo = anterior.getProximo();
            removido = alvo.getDado();
            anterior.setProximo(alvo.getProximo());
            if (alvo == fim) {
                fim = anterior;
            }
        }
        tamanho--;
        return removido;
    }

    /** Remove a primeira ocorrência do dado (usa equals). Retorna true se removeu. */
    public boolean remover(T dado) {
        int indice = indiceDe(dado);
        if (indice == -1) {
            return false;
        }
        removerPosicao(indice);
        return true;
    }

    /** Retorna o índice da primeira ocorrência ou -1 se não encontrar. */
    public int indiceDe(T dado) {
        No<T> atual = inicio;
        int posicao = 0;
        while (atual != null) {
            if (iguais(atual.getDado(), dado)) {
                return posicao;
            }
            atual = atual.getProximo();
            posicao++;
        }
        return -1;
    }

    public boolean contem(T dado) {
        return indiceDe(dado) != -1;
    }

    public int tamanho() {
        return tamanho;
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }

    public void limpar() {
        inicio = null;
        fim = null;
        tamanho = 0;
    }

    /**
     * Primeiro nó da lista. Permite percorrer a lista em O(n) no total:
     * for (No<T> n = lista.getInicio(); n != null; n = n.getProximo())
     */
    public No<T> getInicio() {
        return inicio;
    }

    private No<T> noEm(int indice) {
        No<T> atual = inicio;
        for (int i = 0; i < indice; i++) {
            atual = atual.getProximo();
        }
        return atual;
    }

    private void validarIndice(int indice) {
        if (indice < 0 || indice >= tamanho) {
            throw new IndexOutOfBoundsException("Índice " + indice + " fora da lista (tamanho " + tamanho + ")");
        }
    }

    private boolean iguais(T a, T b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}
