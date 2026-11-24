package com.hamburgueria.logica;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.hamburgueria.modelo.Ingrediente;
import com.hamburgueria.modelo.Receita;

/** Cardapio padrao e carregamento das receitas a partir de assets/receitas.txt. */
public final class Cardapio {

    private Cardapio() {
    }

    public static Receita[] padrao() {
        return new Receita[]{
                new Receita("Simples", new Ingrediente[]{
                        Ingrediente.PAO, Ingrediente.CARNE, Ingrediente.PAO}, 70),
                new Receita("X-Burger", new Ingrediente[]{
                        Ingrediente.PAO, Ingrediente.CARNE, Ingrediente.QUEIJO, Ingrediente.PAO}, 100),
                new Receita("Vegetariano", new Ingrediente[]{
                        Ingrediente.PAO, Ingrediente.QUEIJO, Ingrediente.ALFACE, Ingrediente.TOMATE,
                        Ingrediente.PAO}, 90),
                new Receita("X-Salada", new Ingrediente[]{
                        Ingrediente.PAO, Ingrediente.CARNE, Ingrediente.ALFACE, Ingrediente.TOMATE,
                        Ingrediente.PAO}, 130),
                new Receita("X-Tudo", new Ingrediente[]{
                        Ingrediente.PAO, Ingrediente.CARNE, Ingrediente.QUEIJO, Ingrediente.ALFACE,
                        Ingrediente.TOMATE, Ingrediente.PAO}, 160)
        };
    }

    /**
     * Formato de cada linha: nome|pontos|INGREDIENTE,INGREDIENTE,...
     * Linhas vazias e comentarios iniciados por # sao ignorados.
     * Se o arquivo estiver ausente ou invalido, utiliza o cardapio padrao.
     */
    public static Receita[] carregar() {
        try {
            FileHandle arquivo = Gdx.files.internal("receitas.txt");
            if (!arquivo.exists()) {
                Gdx.app.log("Cardapio", "receitas.txt nao encontrado; usando cardapio padrao.");
                return padrao();
            }

            String[] linhas = arquivo.readString("UTF-8").split("\\r?\\n");
            Receita[] receitas = new Receita[linhas.length];
            int quantidade = 0;

            for (int i = 0; i < linhas.length; i++) {
                String linha = linhas[i].trim();
                if (linha.isEmpty() || linha.startsWith("#")) {
                    continue;
                }

                String[] campos = linha.split("\\|");
                if (campos.length != 3) {
                    throw new IllegalArgumentException("Formato invalido na linha " + (i + 1));
                }

                String nome = campos[0].trim();
                int pontos = Integer.parseInt(campos[1].trim());
                String[] nomesIngredientes = campos[2].split(",");
                Ingrediente[] ingredientes = new Ingrediente[nomesIngredientes.length];

                for (int j = 0; j < nomesIngredientes.length; j++) {
                    ingredientes[j] = Ingrediente.valueOf(nomesIngredientes[j].trim().toUpperCase());
                }

                receitas[quantidade] = new Receita(nome, ingredientes, pontos);
                quantidade++;
            }

            if (quantidade == 0) {
                throw new IllegalArgumentException("O arquivo nao possui receitas.");
            }

            Receita[] resultado = new Receita[quantidade];
            for (int i = 0; i < quantidade; i++) {
                resultado[i] = receitas[i];
            }
            Gdx.app.log("Cardapio", quantidade + " receitas carregadas de receitas.txt.");
            return resultado;
        } catch (Exception erro) {
            Gdx.app.error("Cardapio", "Falha ao carregar receitas.txt; usando cardapio padrao.", erro);
            return padrao();
        }
    }
}
