package com.hamburgueria.logica;

import com.hamburgueria.estruturas.Algoritmos;
import com.hamburgueria.estruturas.Comparador;
import com.hamburgueria.estruturas.Fila;
import com.hamburgueria.estruturas.ListaEncadeada;
import com.hamburgueria.estruturas.No;
import com.hamburgueria.modelo.Cliente;
import com.hamburgueria.modelo.Pedido;
import com.hamburgueria.modelo.RegistroRanking;

import java.util.Random;

/**
 * Teste simples (sem JUnit) de ordenação e busca: Algoritmos, ranking e pesquisa na Partida.
 * Rode a main para conferir: cada verificação imprime OK ou FALHOU.
 */
public class TesteAlgoritmos {

    private static int falhas = 0;

    private static final Comparador<Integer> CRESCENTE = (a, b) -> Integer.compare(a, b);

    /** Par usado para conferir a estabilidade: ordena só pela chave, "marca" diferencia os repetidos. */
    private static class Par {
        final int chave;
        final String marca;

        Par(int chave, String marca) {
            this.chave = chave;
            this.marca = marca;
        }
    }

    private static final Comparador<Par> POR_CHAVE = (a, b) -> Integer.compare(a.chave, b.chave);

    public static void main(String[] args) {
        testarInsertionSortVetor();
        testarInsertionSortLista();
        testarBuscaSequencial();
        testarBuscaBinaria();
        testarRanking();
        testarPartida();

        System.out.println();
        if (falhas == 0) {
            System.out.println("Todos os testes passaram.");
        } else {
            System.out.println(falhas + " teste(s) falharam.");
            System.exit(1);
        }
    }

    // ------------------------------------------------------------ ordenação

    private static void testarInsertionSortVetor() {
        System.out.println("== Insertion Sort (vetor) ==");

        Integer[] vazio = new Integer[0];
        Algoritmos.insertionSort(vazio, CRESCENTE);
        verificar(vazio.length == 0, "vetor vazio não quebra");

        Integer[] um = {7};
        Algoritmos.insertionSort(um, CRESCENTE);
        verificar(um[0] == 7, "um elemento continua igual");

        Integer[] desordenado = {5, 2, 9, 1, 7};
        Algoritmos.insertionSort(desordenado, CRESCENTE);
        verificar(texto(desordenado).equals("1 2 5 7 9"), "ordena desordenado: " + texto(desordenado));

        Integer[] ordenado = {1, 2, 3, 4};
        Algoritmos.insertionSort(ordenado, CRESCENTE);
        verificar(texto(ordenado).equals("1 2 3 4"), "já ordenado continua igual");

        Integer[] inverso = {4, 3, 2, 1};
        Algoritmos.insertionSort(inverso, CRESCENTE);
        verificar(texto(inverso).equals("1 2 3 4"), "ordem inversa (pior caso)");

        Integer[] repetidos = {3, 1, 3, 2, 1, 3};
        Algoritmos.insertionSort(repetidos, CRESCENTE);
        verificar(texto(repetidos).equals("1 1 2 3 3 3"), "elementos repetidos: " + texto(repetidos));

        Integer[] parcial = {9, 8, 7, 3, 2, 1};
        Algoritmos.insertionSort(parcial, 3, CRESCENTE);
        verificar(texto(parcial).equals("7 8 9 3 2 1"), "ordena só as primeiras 'quantidade' posições");

        Integer[] decrescente = {1, 5, 3};
        Algoritmos.insertionSort(decrescente, (a, b) -> Integer.compare(b, a));
        verificar(texto(decrescente).equals("5 3 1"), "critério decrescente");

        Par[] pares = {new Par(2, "a"), new Par(1, "b"), new Par(2, "c"), new Par(1, "d")};
        Algoritmos.insertionSort(pares, POR_CHAVE);
        verificar(pares[0].marca.equals("b") && pares[1].marca.equals("d")
                        && pares[2].marca.equals("a") && pares[3].marca.equals("c"),
                "é estável (repetidos mantêm a ordem original)");

        Random sorteio = new Random(42);
        boolean tudoCerto = true;
        for (int rodada = 0; rodada < 200; rodada++) {
            int n = sorteio.nextInt(30);
            Integer[] v = new Integer[n];
            long somaAntes = 0;
            for (int i = 0; i < n; i++) {
                v[i] = sorteio.nextInt(20);
                somaAntes += v[i];
            }
            Algoritmos.insertionSort(v, CRESCENTE);
            long somaDepois = 0;
            for (int i = 0; i < n; i++) {
                somaDepois += v[i];
            }
            if (!Algoritmos.estaOrdenado(v, n, CRESCENTE) || somaAntes != somaDepois) {
                tudoCerto = false;
            }
        }
        verificar(tudoCerto, "200 vetores aleatórios: saem ordenados e sem perder elementos");
    }

    private static void testarInsertionSortLista() {
        System.out.println("== Insertion Sort (lista encadeada) ==");

        ListaEncadeada<Integer> vazia = new ListaEncadeada<>();
        Algoritmos.insertionSort(vazia, CRESCENTE);
        verificar(vazia.estaVazia(), "lista vazia não quebra");

        ListaEncadeada<Integer> uma = lista(4);
        Algoritmos.insertionSort(uma, CRESCENTE);
        verificar(texto(uma).equals("4"), "um elemento continua igual");

        ListaEncadeada<Integer> desordenada = lista(5, 2, 9, 1, 7);
        Algoritmos.insertionSort(desordenada, CRESCENTE);
        verificar(texto(desordenada).equals("1 2 5 7 9"), "ordena desordenada: " + texto(desordenada));
        verificar(desordenada.tamanho() == 5, "tamanho não muda");

        desordenada.adicionar(10);
        verificar(desordenada.obter(5) == 10, "adicionar no fim continua funcionando depois de ordenar");

        ListaEncadeada<Integer> inversa = lista(4, 3, 2, 1);
        Algoritmos.insertionSort(inversa, CRESCENTE);
        verificar(texto(inversa).equals("1 2 3 4"), "ordem inversa");

        ListaEncadeada<Integer> repetidos = lista(3, 1, 3, 2, 1);
        Algoritmos.insertionSort(repetidos, CRESCENTE);
        verificar(texto(repetidos).equals("1 1 2 3 3"), "elementos repetidos");

        ListaEncadeada<Par> pares = new ListaEncadeada<>();
        pares.adicionar(new Par(2, "a"));
        pares.adicionar(new Par(1, "b"));
        pares.adicionar(new Par(2, "c"));
        pares.adicionar(new Par(1, "d"));
        Algoritmos.insertionSort(pares, POR_CHAVE);
        verificar(pares.obter(0).marca.equals("b") && pares.obter(1).marca.equals("d")
                        && pares.obter(2).marca.equals("a") && pares.obter(3).marca.equals("c"),
                "é estável");

        Random sorteio = new Random(7);
        boolean tudoCerto = true;
        for (int rodada = 0; rodada < 200; rodada++) {
            int n = sorteio.nextInt(25);
            ListaEncadeada<Integer> l = new ListaEncadeada<>();
            for (int i = 0; i < n; i++) {
                l.adicionar(sorteio.nextInt(15));
            }
            Algoritmos.insertionSort(l, CRESCENTE);
            for (int i = 1; i < l.tamanho(); i++) {
                if (l.obter(i - 1) > l.obter(i)) {
                    tudoCerto = false;
                }
            }
            if (l.tamanho() != n) {
                tudoCerto = false;
            }
        }
        verificar(tudoCerto, "200 listas aleatórias: saem ordenadas e com o mesmo tamanho");
    }

    // ---------------------------------------------------------------- busca

    private static void testarBuscaSequencial() {
        System.out.println("== Busca sequencial ==");

        ListaEncadeada<Integer> vazia = new ListaEncadeada<>();
        verificar(Algoritmos.indiceSequencial(vazia.getInicio(), x -> x == 1) == -1, "lista vazia retorna -1");
        verificar(Algoritmos.buscaSequencial(vazia.getInicio(), x -> x == 1) == null, "lista vazia retorna null");

        ListaEncadeada<Integer> lista = lista(4, 8, 15, 8);
        verificar(Algoritmos.indiceSequencial(lista.getInicio(), x -> x == 15) == 2, "acha o 15 na posição 2");
        verificar(Algoritmos.indiceSequencial(lista.getInicio(), x -> x == 8) == 1, "repetido: primeira ocorrência");
        verificar(Algoritmos.indiceSequencial(lista.getInicio(), x -> x == 4) == 0, "primeiro elemento");
        verificar(Algoritmos.indiceSequencial(lista.getInicio(), x -> x == 99) == -1, "não encontrado retorna -1");
        verificar(Algoritmos.buscaSequencial(lista.getInicio(), x -> x > 10) == 15, "devolve o próprio elemento");

        Fila<Cliente> fila = new Fila<>();
        verificar(Algoritmos.indiceSequencial(fila.getNoInicio(), c -> true) == -1, "fila vazia retorna -1");
        fila.enfileirar(new Cliente("Ana", 30f));
        fila.enfileirar(new Cliente("Bia", 30f));
        fila.enfileirar(new Cliente("Caio", 30f));
        verificar(Algoritmos.indiceSequencial(fila.getNoInicio(), c -> c.getNome().equals("Caio")) == 2,
                "acha cliente na fila");
        verificar(Algoritmos.indiceSequencial(fila.getNoInicio(), c -> c.getNome().equals("Zé")) == -1,
                "cliente que não está na fila");

        Integer[] vetor = {3, 6, 9};
        verificar(Algoritmos.indiceSequencial(vetor, 3, x -> x == 9) == 2, "busca sequencial em vetor");
        verificar(Algoritmos.indiceSequencial(vetor, 2, x -> x == 9) == -1, "respeita o limite 'quantidade'");
    }

    private static void testarBuscaBinaria() {
        System.out.println("== Busca binária ==");

        Integer[] vazio = new Integer[0];
        verificar(Algoritmos.buscaBinaria(vazio, 0, 5, CRESCENTE) == -1, "vetor vazio retorna -1");

        Integer[] um = {5};
        verificar(Algoritmos.buscaBinaria(um, 1, 5, CRESCENTE) == 0, "um elemento: achado");
        verificar(Algoritmos.buscaBinaria(um, 1, 6, CRESCENTE) == -1, "um elemento: não achado");

        Integer[] v = {1, 3, 5, 7, 9, 11};
        verificar(Algoritmos.buscaBinaria(v, v.length, 1, CRESCENTE) == 0, "primeiro elemento");
        verificar(Algoritmos.buscaBinaria(v, v.length, 11, CRESCENTE) == 5, "último elemento");
        verificar(Algoritmos.buscaBinaria(v, v.length, 7, CRESCENTE) == 3, "elemento do meio");
        verificar(Algoritmos.buscaBinaria(v, v.length, 4, CRESCENTE) == -1, "valor entre dois elementos");
        verificar(Algoritmos.buscaBinaria(v, v.length, 0, CRESCENTE) == -1, "menor que todos");
        verificar(Algoritmos.buscaBinaria(v, v.length, 100, CRESCENTE) == -1, "maior que todos");

        Integer[] repetidos = {1, 2, 2, 2, 2, 3};
        verificar(Algoritmos.buscaBinaria(repetidos, repetidos.length, 2, CRESCENTE) == 1,
                "repetidos: devolve a primeira ocorrência");

        Integer[] parcial = {1, 2, 3, 99, 0};
        verificar(Algoritmos.buscaBinaria(parcial, 3, 99, CRESCENTE) == -1, "respeita o limite 'quantidade'");

        Random sorteio = new Random(3);
        boolean tudoCerto = true;
        for (int rodada = 0; rodada < 200; rodada++) {
            int n = sorteio.nextInt(30);
            Integer[] a = new Integer[n];
            for (int i = 0; i < n; i++) {
                a[i] = sorteio.nextInt(25);
            }
            Algoritmos.insertionSort(a, CRESCENTE);
            for (int procurado = -1; procurado <= 26; procurado++) {
                int esperado = primeiraOcorrencia(a, n, procurado);
                int obtido = Algoritmos.buscaBinaria(a, n, procurado, CRESCENTE);
                if (esperado != obtido) {
                    tudoCerto = false;
                }
            }
        }
        verificar(tudoCerto, "bate com a busca sequencial em 200 vetores aleatórios");
    }

    // -------------------------------------------------------------- ranking

    private static void testarRanking() {
        System.out.println("== Ranking ==");

        verificar(RankingArquivo.interpretar("").length == 0, "arquivo vazio gera ranking vazio");
        verificar(RankingArquivo.interpretar(null).length == 0, "texto nulo gera ranking vazio");

        String conteudo = "Ana|100\nBia|250\n\nlinha ruim\nCaio|abc\nDavi|250\nEva|40\r\nFulano|Silva|300\n|10\nGabi|\n";
        RegistroRanking[] ranking = RankingArquivo.interpretar(conteudo);
        verificar(ranking.length == 6, "ignora linhas vazias e mal formadas (lidos: " + ranking.length + ")");
        verificar(ranking[0].getPontos() == 300 && ranking[0].getNome().equals("Fulano|Silva"),
                "maior pontuação primeiro (nome com | é preservado)");
        verificar(ranking[1].getNome().equals("Bia") && ranking[2].getNome().equals("Davi"),
                "empate mantém a ordem do arquivo");
        verificar(ranking[ranking.length - 1].getPontos() == 10 && ranking[ranking.length - 1].getNome().equals("Jogador"),
                "nome vazio vira Jogador e menor pontuação fica por último");
        verificar(Algoritmos.estaOrdenado(ranking, ranking.length, RegistroRanking.POR_PONTOS_DECRESCENTE),
                "ranking carregado está ordenado");

        RegistroRanking[] top3 = RankingArquivo.melhores(ranking, 3);
        verificar(top3.length == 3 && top3[0].getPontos() == 300, "melhores(3) devolve os 3 primeiros");
        verificar(RankingArquivo.melhores(RankingArquivo.interpretar("Ana|5"), 5).length == 1,
                "melhores com menos registros que o pedido");
        verificar(RankingArquivo.melhores(new RegistroRanking[0], 5).length == 0, "melhores de ranking vazio");

        verificar(RankingArquivo.buscarPontuacao(ranking, 300) == 1, "300 está em 1º lugar");
        verificar(RankingArquivo.buscarPontuacao(ranking, 250) == 2, "250 repetido: devolve o 2º lugar (primeira ocorrência)");
        verificar(RankingArquivo.buscarPontuacao(ranking, 40) == 5, "40 está em 5º lugar");
        verificar(RankingArquivo.buscarPontuacao(ranking, 10) == 6, "10 está em 6º lugar (último)");
        verificar(RankingArquivo.buscarPontuacao(ranking, 123) == -1, "pontuação inexistente retorna -1");
        verificar(RankingArquivo.buscarPontuacao(new RegistroRanking[0], 10) == -1, "ranking vazio retorna -1");

        RegistroRanking[] partidas = RankingArquivo.interpretar("Ana|100\nBia|250\nana|300\nBIA|10\nCaio|250\nAna|50\n");
        RegistroRanking[] melhores = RankingArquivo.melhoresPorJogador(partidas);
        verificar(melhores.length == 3, "um registro por jogador, sem diferenciar maiúsculas (" + melhores.length + ")");
        verificar(melhores[0].getPontos() == 300 && melhores[0].getNome().equals("ana"), "melhor pontuação de Ana");
        verificar(melhores[1].getNome().equals("Bia") && melhores[1].getPontos() == 250, "melhor pontuação de Bia");
        verificar(melhores[2].getNome().equals("Caio"), "Caio por último, mantendo a ordem decrescente");
        verificar(Algoritmos.estaOrdenado(melhores, melhores.length, RegistroRanking.POR_PONTOS_DECRESCENTE),
                "resultado continua ordenado");
        verificar(RankingArquivo.melhoresPorJogador(new RegistroRanking[0]).length == 0, "ranking vazio por jogador");
    }

    // -------------------------------------------------------------- partida

    private static void testarPartida() {
        System.out.println("== Partida: ordenação e pesquisa ==");

        Partida partida = new Partida(Cardapio.padrao(), 123L);

        verificar(partida.pesquisar("Ana").contains("nao encontrado"), "pesquisa em partida vazia");
        verificar(partida.pesquisar("   ").startsWith("Digite"), "pesquisa com texto vazio");
        verificar(partida.pesquisar(null).startsWith("Digite"), "pesquisa com texto nulo");

        partida.getFila().enfileirar(new Cliente("Zeca", 30f));
        partida.getFila().enfileirar(new Cliente("Lia", 30f));
        verificar(partida.posicaoNaFila("lia") == 1, "posição na fila ignora maiúsculas");
        verificar(partida.posicaoNaFila("Nobody") == -1, "cliente fora da fila");
        verificar(partida.pesquisar("Lia").contains("posicao 2"), "pesquisar cliente na fila: " + partida.pesquisar("Lia"));

        // Deixa o jogo rodar até haver 3 pedidos ativos (o primeiro cliente do balcão é atendido na hora).
        int pedidos = 0;
        for (int passo = 0; passo < 3000 && pedidos < 3 && !partida.estaEmFimDeJogo(); passo++) {
            partida.atualizar(0.1f);
            if (partida.atenderCliente() != null) {
                pedidos++;
            }
        }
        verificar(pedidos == 3, "foram criados 3 pedidos para o teste");

        boolean ordenada = true;
        for (No<Pedido> no = partida.getPedidosAtivos().getInicio(); no != null && no.getProximo() != null;
             no = no.getProximo()) {
            if (no.getDado().getCliente().getPacienciaAtual() > no.getProximo().getDado().getCliente().getPacienciaAtual()) {
                ordenada = false;
            }
        }
        verificar(ordenada, "pedidos ativos ficam ordenados pela paciência restante (menor primeiro)");

        // O preparo segue a ordem de chegada (menor id), mesmo com a lista ordenada por urgência.
        Pedido maisAntigo = partida.getPedidosAtivos().getInicio().getDado();
        for (No<Pedido> no = partida.getPedidosAtivos().getInicio(); no != null; no = no.getProximo()) {
            if (no.getDado().getId() < maisAntigo.getId()) {
                maisAntigo = no.getDado();
            }
        }
        partida.adicionarIngrediente(com.hamburgueria.modelo.Ingrediente.PAO);
        verificar(partida.getPedidoEmPreparo() == maisAntigo, "o lanche começa pelo pedido mais antigo, não pelo mais urgente");

        Pedido ultimo = partida.getPedidosAtivos().obter(partida.getPedidosAtivos().tamanho() - 1);
        String achado = partida.pesquisar(ultimo.getCliente().getNome().toUpperCase());
        verificar(achado.startsWith("Pedido #"), "pesquisar cliente com pedido ativo: " + achado);
        verificar(partida.buscarPedidoPorCliente("Ninguem") == null, "pedido de cliente inexistente retorna null");
    }

    // ---------------------------------------------------------------- apoio

    private static int primeiraOcorrencia(Integer[] v, int n, int procurado) {
        for (int i = 0; i < n; i++) {
            if (v[i] == procurado) {
                return i;
            }
        }
        return -1;
    }

    private static ListaEncadeada<Integer> lista(int... valores) {
        ListaEncadeada<Integer> l = new ListaEncadeada<>();
        for (int i = 0; i < valores.length; i++) {
            l.adicionar(valores[i]);
        }
        return l;
    }

    private static String texto(Integer[] v) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < v.length; i++) {
            sb.append(i > 0 ? " " : "").append(v[i]);
        }
        return sb.toString();
    }

    private static String texto(ListaEncadeada<Integer> l) {
        StringBuilder sb = new StringBuilder();
        for (No<Integer> no = l.getInicio(); no != null; no = no.getProximo()) {
            sb.append(no == l.getInicio() ? "" : " ").append(no.getDado());
        }
        return sb.toString();
    }

    private static void verificar(boolean condicao, String descricao) {
        if (condicao) {
            System.out.println("  OK     " + descricao);
        } else {
            System.out.println("  FALHOU " + descricao);
            falhas++;
        }
    }
}
