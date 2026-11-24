package com.hamburgueria.modelo;

/**
 * Representa o mapa da hamburgueria por uma matriz bidimensional Tile[][].
 * Cada posicao da matriz corresponde a uma celula quadrada do cenario.
 */
public class Cenario {

    public static final int LINHAS = 10;
    public static final int COLUNAS = 15;
    public static final float TAMANHO_TILE = 64f;

    private final Tile[][] mapa;

    public Cenario() {
        mapa = new Tile[LINHAS][COLUNAS];
        preencherChao();
        montarCenario();
    }

    private void preencherChao() {
        for (int linha = 0; linha < LINHAS; linha++) {
            for (int coluna = 0; coluna < COLUNAS; coluna++) {
                mapa[linha][coluna] = Tile.CHAO;
            }
        }
    }

    private void montarCenario() {
        // Balcao separa a cozinha (esquerda) da fila de clientes (direita).
        for (int linha = 0; linha < LINHAS; linha++) {
            mapa[linha][6] = Tile.BALCAO;
        }

        // Estacoes de trabalho da cozinha.
        for (int coluna = 1; coluna <= 3; coluna++) {
            mapa[8][coluna] = Tile.CHAPA;
        }
        for (int coluna = 1; coluna <= 4; coluna++) {
            mapa[9][coluna] = Tile.BANCADA;
        }
    }

    public Tile getTile(int linha, int coluna) {
        if (linha < 0 || linha >= LINHAS || coluna < 0 || coluna >= COLUNAS) {
            throw new IndexOutOfBoundsException("Posicao fora do cenario: " + linha + ", " + coluna);
        }
        return mapa[linha][coluna];
    }

    public Tile[][] getMapa() {
        Tile[][] copia = new Tile[LINHAS][COLUNAS];
        for (int linha = 0; linha < LINHAS; linha++) {
            for (int coluna = 0; coluna < COLUNAS; coluna++) {
                copia[linha][coluna] = mapa[linha][coluna];
            }
        }
        return copia;
    }
}
