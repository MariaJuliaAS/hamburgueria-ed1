package com.hamburgueria.modelo;

/** Teste simples de terminal para conferir a matriz do cenario. */
public class TesteCenario {

    public static void main(String[] args) {
        Cenario cenario = new Cenario();
        Tile[][] mapa = cenario.getMapa();

        if (mapa.length != Cenario.LINHAS || mapa[0].length != Cenario.COLUNAS) {
            throw new AssertionError("Dimensoes incorretas da matriz.");
        }
        if (cenario.getTile(0, 6) != Tile.BALCAO
                || cenario.getTile(8, 2) != Tile.CHAPA
                || cenario.getTile(9, 2) != Tile.BANCADA) {
            throw new AssertionError("Elementos do cenario em posicoes incorretas.");
        }
        System.out.println("TesteCenario: matriz " + Cenario.LINHAS + "x" + Cenario.COLUNAS + " OK.");
    }
}
