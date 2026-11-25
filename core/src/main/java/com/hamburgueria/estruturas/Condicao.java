package com.hamburgueria.estruturas;

/** Condição genérica usada pela busca sequencial: diz se um elemento é o que estamos procurando. */
public interface Condicao<T> {

    boolean satisfaz(T elemento);
}
