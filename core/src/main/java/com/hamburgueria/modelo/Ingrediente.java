package com.hamburgueria.modelo;

/**
 * Ingredientes que podem ser empilhados num hambúrguer.
 * O tempo de preparo (em segundos) será usado na cozinha: a carne demora mais que os outros.
 * Os cinco primeiros custam 0 e já começam liberados; os demais são comprados na loja entre os níveis.
 */
public enum Ingrediente {

    PAO("Pão", 1.0f, 0),
    CARNE("Carne", 3.0f, 0),
    QUEIJO("Queijo", 1.0f, 0),
    ALFACE("Alface", 1.0f, 0),
    TOMATE("Tomate", 1.0f, 0),
    CEBOLA("Cebola", 1.0f, 25),
    PICLES("Picles", 1.0f, 30),
    BACON("Bacon", 2.0f, 40),
    OVO("Ovo", 2.0f, 50);

    private final String nome;
    private final float tempoPreparo;
    private final int preco;

    Ingrediente(String nome, float tempoPreparo, int preco) {
        this.nome = nome;
        this.tempoPreparo = tempoPreparo;
        this.preco = preco;
    }

    public String getNome() {
        return nome;
    }

    public float getTempoPreparo() {
        return tempoPreparo;
    }

    /** Preço em moedas na loja. Zero significa que já começa liberado. */
    public int getPreco() {
        return preco;
    }
}
