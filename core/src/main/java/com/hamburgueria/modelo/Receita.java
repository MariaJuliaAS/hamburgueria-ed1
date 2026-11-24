package com.hamburgueria.modelo;

import com.hamburgueria.estruturas.No;
import com.hamburgueria.estruturas.Pilha;

/**
 * Receita de um lanche: nome, valor em pontos e a sequência de ingredientes.
 * Os ingredientes ficam num vetor (item "Vetor" do checklist), da base até o topo.
 * Exemplo: X-Burger = PAO, CARNE, QUEIJO, PAO.
 */
public class Receita {

    private final String nome;
    private final Ingrediente[] ingredientes;
    private final int pontos;

    public Receita(String nome, Ingrediente[] ingredientes, int pontos) {
        if (nome == null || nome.isEmpty()) {
            throw new IllegalArgumentException("A receita precisa de um nome");
        }
        if (ingredientes == null || ingredientes.length == 0) {
            throw new IllegalArgumentException("A receita precisa de pelo menos um ingrediente");
        }
        this.nome = nome;
        this.pontos = pontos;
        // Cópia manual, para que alterar o vetor original depois não mude a receita.
        this.ingredientes = new Ingrediente[ingredientes.length];
        for (int i = 0; i < ingredientes.length; i++) {
            if (ingredientes[i] == null) {
                throw new IllegalArgumentException("Ingrediente nulo na posição " + i);
            }
            this.ingredientes[i] = ingredientes[i];
        }
    }

    public String getNome() {
        return nome;
    }

    public int getPontos() {
        return pontos;
    }

    public int getQuantidade() {
        return ingredientes.length;
    }

    /** Ingrediente na posição indicada, onde 0 é a base do lanche. */
    public Ingrediente getIngrediente(int posicao) {
        if (posicao < 0 || posicao >= ingredientes.length) {
            throw new IndexOutOfBoundsException("Posição " + posicao + " fora da receita");
        }
        return ingredientes[posicao];
    }

    /** Soma do tempo de preparo de todos os ingredientes, em segundos. */
    public float getTempoPreparoTotal() {
        float total = 0f;
        for (int i = 0; i < ingredientes.length; i++) {
            total += ingredientes[i].getTempoPreparo();
        }
        return total;
    }

    /**
     * Confere se o lanche montado na pilha é exatamente esta receita:
     * mesma quantidade de camadas e mesma ordem, da base ao topo.
     * A pilha é percorrida do topo para a base, então comparamos de trás para frente.
     */
    public boolean confere(Pilha<Ingrediente> montado) {
        if (montado.tamanho() != ingredientes.length) {
            return false;
        }
        int posicao = ingredientes.length - 1;
        for (No<Ingrediente> no = montado.getNoTopo(); no != null; no = no.getProximo()) {
            if (no.getDado() != ingredientes[posicao]) {
                return false;
            }
            posicao--;
        }
        return true;
    }

    /** Descrição em texto, por exemplo "X-Burger: Pão, Carne, Queijo, Pão". */
    @Override
    public String toString() {
        StringBuilder texto = new StringBuilder(nome).append(": ");
        for (int i = 0; i < ingredientes.length; i++) {
            if (i > 0) {
                texto.append(", ");
            }
            texto.append(ingredientes[i].getNome());
        }
        return texto.toString();
    }
}
