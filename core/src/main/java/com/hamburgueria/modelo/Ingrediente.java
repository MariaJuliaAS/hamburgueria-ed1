package com.hamburgueria.modelo;

/**
 * Ingredientes que podem ser empilhados num hambúrguer.
 * O tempo de preparo (em segundos) será usado na cozinha: a carne demora mais que os outros.
 */
public enum Ingrediente {

    PAO("Pão", 1.0f),
    CARNE("Carne", 3.0f),
    QUEIJO("Queijo", 1.0f),
    ALFACE("Alface", 1.0f),
    TOMATE("Tomate", 1.0f);

    private final String nome;
    private final float tempoPreparo;

    Ingrediente(String nome, float tempoPreparo) {
        this.nome = nome;
        this.tempoPreparo = tempoPreparo;
    }

    public String getNome() {
        return nome;
    }

    public float getTempoPreparo() {
        return tempoPreparo;
    }
}
