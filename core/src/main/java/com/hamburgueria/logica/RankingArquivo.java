package com.hamburgueria.logica;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.hamburgueria.estruturas.Algoritmos;
import com.hamburgueria.modelo.RegistroRanking;

/**
 * Persistência do ranking em ranking.txt (uma linha nome|pontos por partida) e consultas sobre ele.
 * O arquivo só recebe linhas novas; a ordenação (Insertion Sort) acontece ao carregar.
 */
public final class RankingArquivo {

    private static final String ARQUIVO = "ranking.txt";

    private RankingArquivo() {
    }

    /** Adiciona uma linha no formato nome|pontos ao arquivo local ranking.txt. */
    public static void salvar(String nome, int pontos) {
        String nomeSeguro = nome == null ? "Jogador" : nome.trim();
        if (nomeSeguro.isEmpty()) {
            nomeSeguro = "Jogador";
        }
        // Evita que o caractere separador quebre o formato do arquivo.
        nomeSeguro = nomeSeguro.replace('|', ' ').replace('\n', ' ').replace('\r', ' ');

        try {
            FileHandle arquivo = Gdx.files.local(ARQUIVO);
            arquivo.writeString(nomeSeguro + "|" + pontos + "\n", true, "UTF-8");
            Gdx.app.log("Ranking", "Resultado salvo em " + arquivo.file().getAbsolutePath());
        } catch (Exception erro) {
            Gdx.app.error("Ranking", "Nao foi possivel salvar o ranking.", erro);
        }
    }

    /** Lê o ranking.txt e devolve os registros já ordenados (maior pontuação primeiro). Vetor vazio se não existir. */
    public static RegistroRanking[] carregar() {
        try {
            FileHandle arquivo = Gdx.files.local(ARQUIVO);
            if (!arquivo.exists()) {
                return new RegistroRanking[0];
            }
            return interpretar(arquivo.readString("UTF-8"));
        } catch (Exception erro) {
            Gdx.app.error("Ranking", "Nao foi possivel ler o ranking.", erro);
            return new RegistroRanking[0];
        }
    }

    /**
     * Transforma o texto do arquivo em registros ordenados por pontuação (decrescente).
     * Linhas vazias ou mal formadas são ignoradas. Não depende do LibGDX, então dá para testar no terminal.
     */
    public static RegistroRanking[] interpretar(String conteudo) {
        if (conteudo == null || conteudo.isEmpty()) {
            return new RegistroRanking[0];
        }
        String[] linhas = conteudo.split("\\r?\\n");
        RegistroRanking[] lidos = new RegistroRanking[linhas.length];
        int quantidade = 0;

        for (int i = 0; i < linhas.length; i++) {
            RegistroRanking registro = interpretarLinha(linhas[i]);
            if (registro != null) {
                lidos[quantidade] = registro;
                quantidade++;
            }
        }

        RegistroRanking[] ranking = new RegistroRanking[quantidade];
        for (int i = 0; i < quantidade; i++) {
            ranking[i] = lidos[i];
        }
        Algoritmos.insertionSort(ranking, RegistroRanking.POR_PONTOS_DECRESCENTE);
        return ranking;
    }

    private static RegistroRanking interpretarLinha(String linha) {
        String limpa = linha.trim();
        int separador = limpa.lastIndexOf('|');
        if (separador < 0 || separador == limpa.length() - 1) {
            return null;
        }
        try {
            int pontos = Integer.parseInt(limpa.substring(separador + 1).trim());
            String nome = limpa.substring(0, separador).trim();
            return new RegistroRanking(nome.isEmpty() ? "Jogador" : nome, pontos);
        } catch (NumberFormatException erro) {
            return null;
        }
    }

    /** Os {@code n} primeiros do ranking já ordenado (ou todos, se houver menos que n). */
    public static RegistroRanking[] melhores(RegistroRanking[] ranking, int n) {
        int tamanho = Math.min(n, ranking.length);
        RegistroRanking[] topo = new RegistroRanking[tamanho];
        for (int i = 0; i < tamanho; i++) {
            topo[i] = ranking[i];
        }
        return topo;
    }

    /**
     * Busca binária de uma pontuação no ranking ordenado.
     * Retorna a posição no ranking (1 = primeiro lugar) ou -1 se ninguém fez essa pontuação.
     */
    public static int buscarPontuacao(RegistroRanking[] rankingOrdenado, int pontos) {
        RegistroRanking procurado = new RegistroRanking("", pontos);
        int indice = Algoritmos.buscaBinaria(rankingOrdenado, rankingOrdenado.length, procurado,
                RegistroRanking.POR_PONTOS_DECRESCENTE);
        return indice == -1 ? -1 : indice + 1;
    }
}
