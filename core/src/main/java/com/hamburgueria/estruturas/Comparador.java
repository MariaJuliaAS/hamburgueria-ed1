package com.hamburgueria.estruturas;

/**
 * Critério de comparação genérico, usado pelos algoritmos de ordenação e de busca binária.
 * (Interface própria para não depender de java.util.)
 */
public interface Comparador<T> {

    /** Negativo se a vem antes de b, zero se são equivalentes, positivo se a vem depois de b. */
    int comparar(T a, T b);
}
