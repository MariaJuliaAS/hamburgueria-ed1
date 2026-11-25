package com.hamburgueria.logica;

import com.hamburgueria.estruturas.Algoritmos;
import com.hamburgueria.estruturas.Comparador;
import com.hamburgueria.modelo.Ingrediente;

/**
 * Loja aberta entre os níveis: o jogador gasta moedas para liberar ingredientes novos,
 * que passam a poder ser usados nos lanches e a aparecer em receitas.
 */
public final class Loja {

    private static final Comparador<Ingrediente> POR_PRECO =
            (a, b) -> Integer.compare(a.getPreco(), b.getPreco());

    private Loja() {
    }

    /** Ingredientes que ainda não foram comprados, do mais barato para o mais caro (Insertion Sort). */
    public static Ingrediente[] itensAVenda(Partida partida) {
        Ingrediente[] todos = Ingrediente.values();
        Ingrediente[] aVenda = new Ingrediente[todos.length];
        int quantidade = 0;
        for (int i = 0; i < todos.length; i++) {
            if (!partida.isDesbloqueado(todos[i])) {
                aVenda[quantidade] = todos[i];
                quantidade++;
            }
        }
        Algoritmos.insertionSort(aVenda, quantidade, POR_PRECO);

        Ingrediente[] resultado = new Ingrediente[quantidade];
        for (int i = 0; i < quantidade; i++) {
            resultado[i] = aVenda[i];
        }
        return resultado;
    }

    /** Compra um ingrediente: cobra as moedas e libera o uso. */
    public static ResultadoCompra comprar(Partida partida, Ingrediente ingrediente) {
        if (partida.isDesbloqueado(ingrediente)) {
            return ResultadoCompra.JA_DESBLOQUEADO;
        }
        if (!partida.gastarMoedas(ingrediente.getPreco())) {
            return ResultadoCompra.SEM_MOEDAS;
        }
        partida.desbloquear(ingrediente);
        return ResultadoCompra.COMPRADO;
    }
}
