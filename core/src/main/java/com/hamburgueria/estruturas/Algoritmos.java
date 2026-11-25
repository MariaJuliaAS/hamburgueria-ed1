package com.hamburgueria.estruturas;

/**
 * Algoritmos de ordenação e busca, genéricos e implementados do zero (sem java.util).
 *
 * No jogo:
 *  - Insertion Sort em vetor: ordena o ranking por pontuação (RankingArquivo).
 *  - Insertion Sort em lista encadeada: mantém os pedidos ativos ordenados pela paciência restante (Partida).
 *  - Busca sequencial: acha um pedido ou um cliente da fila pelo nome (Partida.pesquisar).
 *  - Busca binária: acha uma pontuação no ranking já ordenado (RankingArquivo.buscarPontuacao).
 */
public final class Algoritmos {

    private Algoritmos() {
    }

    // ------------------------------------------------------------ ordenação

    /**
     * Insertion Sort sobre as primeiras {@code quantidade} posições de um vetor.
     * Estável (elementos equivalentes mantêm a ordem original). O(n²) no pior caso, O(n) se já ordenado.
     */
    public static <T> void insertionSort(T[] vetor, int quantidade, Comparador<T> criterio) {
        for (int i = 1; i < quantidade; i++) {
            T chave = vetor[i];
            int j = i - 1;
            while (j >= 0 && criterio.comparar(vetor[j], chave) > 0) {
                vetor[j + 1] = vetor[j];
                j--;
            }
            vetor[j + 1] = chave;
        }
    }

    /** Insertion Sort sobre o vetor inteiro. */
    public static <T> void insertionSort(T[] vetor, Comparador<T> criterio) {
        insertionSort(vetor, vetor.length, criterio);
    }

    /**
     * Insertion Sort direto nos nós da lista encadeada: para cada nó, procura onde o valor deve entrar
     * entre os nós anteriores (já ordenados) e desloca os valores uma casa para frente.
     * Os nós em si não mudam, só o dado que cada um guarda. Estável.
     */
    public static <T> void insertionSort(ListaEncadeada<T> lista, Comparador<T> criterio) {
        No<T> inicio = lista.getInicio();
        if (inicio == null) {
            return;
        }
        for (No<T> atual = inicio.getProximo(); atual != null; atual = atual.getProximo()) {
            T chave = atual.getDado();

            // Primeiro nó (antes de "atual") cujo dado deve vir depois da chave.
            No<T> destino = inicio;
            while (destino != atual && criterio.comparar(destino.getDado(), chave) <= 0) {
                destino = destino.getProximo();
            }
            if (destino == atual) {
                continue; // já está na posição certa
            }

            // Desloca destino..atual uma casa e coloca a chave em "destino".
            T carregado = chave;
            for (No<T> no = destino; no != atual.getProximo(); no = no.getProximo()) {
                T anterior = no.getDado();
                no.setDado(carregado);
                carregado = anterior;
            }
        }
    }

    /** Verifica se o vetor (primeiras posições) está ordenado segundo o critério. */
    public static <T> boolean estaOrdenado(T[] vetor, int quantidade, Comparador<T> criterio) {
        for (int i = 1; i < quantidade; i++) {
            if (criterio.comparar(vetor[i - 1], vetor[i]) > 0) {
                return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------------------- busca

    /**
     * Busca sequencial a partir de um nó: serve para ListaEncadeada (getInicio) e para Fila (getNoInicio).
     * Retorna a posição (começando em 0) do primeiro elemento que satisfaz a condição, ou -1.
     */
    public static <T> int indiceSequencial(No<T> inicio, Condicao<T> condicao) {
        int posicao = 0;
        for (No<T> no = inicio; no != null; no = no.getProximo()) {
            if (condicao.satisfaz(no.getDado())) {
                return posicao;
            }
            posicao++;
        }
        return -1;
    }

    /** Busca sequencial que devolve o próprio elemento encontrado, ou null. */
    public static <T> T buscaSequencial(No<T> inicio, Condicao<T> condicao) {
        for (No<T> no = inicio; no != null; no = no.getProximo()) {
            if (condicao.satisfaz(no.getDado())) {
                return no.getDado();
            }
        }
        return null;
    }

    /** Busca sequencial em vetor. Retorna o índice da primeira ocorrência ou -1. */
    public static <T> int indiceSequencial(T[] vetor, int quantidade, Condicao<T> condicao) {
        for (int i = 0; i < quantidade; i++) {
            if (condicao.satisfaz(vetor[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Busca binária em vetor JÁ ORDENADO segundo o mesmo critério. O(log n).
     * Se houver elementos repetidos, devolve o índice da PRIMEIRA ocorrência. Retorna -1 se não achar.
     */
    public static <T> int buscaBinaria(T[] vetor, int quantidade, T procurado, Comparador<T> criterio) {
        int esquerda = 0;
        int direita = quantidade - 1;
        int achado = -1;
        while (esquerda <= direita) {
            int meio = esquerda + (direita - esquerda) / 2;
            int cmp = criterio.comparar(vetor[meio], procurado);
            if (cmp == 0) {
                achado = meio;
                direita = meio - 1; // continua à esquerda, para pegar a primeira ocorrência
            } else if (cmp < 0) {
                esquerda = meio + 1;
            } else {
                direita = meio - 1;
            }
        }
        return achado;
    }
}
