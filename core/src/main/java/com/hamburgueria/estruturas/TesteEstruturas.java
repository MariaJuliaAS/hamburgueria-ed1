package com.hamburgueria.estruturas;

/**
 * Teste simples (sem JUnit) das estruturas genéricas.
 * Rode a main para conferir: cada verificação imprime OK ou FALHOU.
 */
public class TesteEstruturas {

    private static int falhas = 0;

    public static void main(String[] args) {
        testarLista();
        testarPilha();
        testarFila();

        System.out.println();
        if (falhas == 0) {
            System.out.println("Todos os testes passaram.");
        } else {
            System.out.println(falhas + " teste(s) falharam.");
            System.exit(1);
        }
    }

    private static void testarLista() {
        System.out.println("== ListaEncadeada ==");
        ListaEncadeada<String> lista = new ListaEncadeada<>();

        verificar(lista.estaVazia(), "lista nova está vazia");

        lista.adicionar("B");
        lista.adicionar("C");
        lista.adicionarInicio("A");
        verificar(lista.tamanho() == 3, "tamanho 3 após 3 inserções");
        verificar(lista.obter(0).equals("A"), "posição 0 é A");
        verificar(lista.obter(1).equals("B"), "posição 1 é B");
        verificar(lista.obter(2).equals("C"), "posição 2 é C");

        verificar(lista.contem("B"), "contem B");
        verificar(!lista.contem("Z"), "não contem Z");
        verificar(lista.indiceDe("C") == 2, "indiceDe C é 2");
        verificar(lista.indiceDe("Z") == -1, "indiceDe Z é -1");

        lista.definir(1, "X");
        verificar(lista.obter(1).equals("X"), "definir altera a posição 1");

        verificar(lista.remover("X"), "remover X retorna true");
        verificar(!lista.remover("X"), "remover X de novo retorna false");
        verificar(lista.tamanho() == 2, "tamanho 2 após remover");

        verificar(lista.removerPosicao(1).equals("C"), "removerPosicao(1) devolve C (o fim)");
        lista.adicionar("D");
        verificar(lista.obter(1).equals("D"), "adicionar após remover o fim continua correto");

        verificar(lista.removerPosicao(0).equals("A"), "removerPosicao(0) devolve A");
        verificar(lista.removerPosicao(0).equals("D"), "remove o último elemento restante");
        verificar(lista.estaVazia(), "lista vazia após remover tudo");
        lista.adicionar("E");
        verificar(lista.obter(0).equals("E") && lista.tamanho() == 1, "reutiliza a lista após esvaziar");

        boolean lancou = false;
        try {
            lista.obter(5);
        } catch (IndexOutOfBoundsException e) {
            lancou = true;
        }
        verificar(lancou, "obter fora do intervalo lança exceção");

        lista.adicionar("F");
        lista.adicionar("G");
        int contagem = 0;
        for (No<String> n = lista.getInicio(); n != null; n = n.getProximo()) {
            contagem++;
        }
        verificar(contagem == 3, "percorrer com getInicio visita 3 nós");

        lista.limpar();
        verificar(lista.estaVazia(), "limpar esvazia a lista");
    }

    private static void testarPilha() {
        System.out.println("== Pilha ==");
        Pilha<Integer> pilha = new Pilha<>();

        verificar(pilha.estaVazia(), "pilha nova está vazia");

        pilha.empilhar(1);
        pilha.empilhar(2);
        pilha.empilhar(3);
        verificar(pilha.tamanho() == 3, "tamanho 3");
        verificar(pilha.topo() == 3, "topo é 3");
        verificar(pilha.tamanho() == 3, "topo não remove");

        verificar(pilha.desempilhar() == 3, "desempilha 3");
        verificar(pilha.desempilhar() == 2, "desempilha 2");
        verificar(pilha.topo() == 1, "topo agora é 1");

        pilha.empilhar(9);
        int soma = 0;
        for (No<Integer> n = pilha.getNoTopo(); n != null; n = n.getProximo()) {
            soma += n.getDado();
        }
        verificar(soma == 10, "percorrer do topo à base soma 9 + 1");

        pilha.desempilhar();
        pilha.desempilhar();
        verificar(pilha.estaVazia(), "pilha vazia após desempilhar tudo");

        boolean lancou = false;
        try {
            pilha.desempilhar();
        } catch (IllegalStateException e) {
            lancou = true;
        }
        verificar(lancou, "desempilhar pilha vazia lança exceção");
    }

    private static void testarFila() {
        System.out.println("== Fila ==");
        Fila<String> fila = new Fila<>();

        verificar(fila.estaVazia(), "fila nova está vazia");

        fila.enfileirar("Ana");
        fila.enfileirar("Bia");
        fila.enfileirar("Caio");
        verificar(fila.tamanho() == 3, "tamanho 3");
        verificar(fila.primeiro().equals("Ana"), "primeiro é Ana");

        verificar(fila.desenfileirar().equals("Ana"), "sai Ana primeiro (FIFO)");
        verificar(fila.desenfileirar().equals("Bia"), "sai Bia depois");

        fila.enfileirar("Davi");
        int contagem = 0;
        for (No<String> n = fila.getNoInicio(); n != null; n = n.getProximo()) {
            contagem++;
        }
        verificar(contagem == 2, "percorrer a fila visita 2 nós (Caio e Davi)");

        verificar(fila.desenfileirar().equals("Caio"), "sai Caio");
        verificar(fila.desenfileirar().equals("Davi"), "sai Davi");
        verificar(fila.estaVazia(), "fila vazia após esvaziar");

        fila.enfileirar("Eva");
        verificar(fila.primeiro().equals("Eva") && fila.tamanho() == 1, "reutiliza a fila após esvaziar");

        boolean lancou = false;
        try {
            fila.desenfileirar();
            fila.desenfileirar();
        } catch (IllegalStateException e) {
            lancou = true;
        }
        verificar(lancou, "desenfileirar fila vazia lança exceção");
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
