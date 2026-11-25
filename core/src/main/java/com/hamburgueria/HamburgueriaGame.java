package com.hamburgueria;

import com.badlogic.gdx.Game;
import com.hamburgueria.logica.Cardapio;
import com.hamburgueria.logica.RankingArquivo;
import com.hamburgueria.logica.Partida;
import com.hamburgueria.telas.GameOverScreen;
import com.hamburgueria.telas.JogoScreen;
import com.hamburgueria.telas.LojaScreen;
import com.hamburgueria.telas.MenuScreen;

/** Classe principal: controla a troca entre as telas do jogo. */
public class HamburgueriaGame extends Game {

    private static final int TAMANHO_MAXIMO_NOME = 12;

    /** Nome do jogador atual, escolhido no menu e usado ao salvar no ranking. Null até ser informado. */
    private String nomeJogador;

    public String getNomeJogador() {
        return nomeJogador;
    }

    public void setNomeJogador(String nome) {
        String limpo = nome == null ? "" : nome.replace('|', ' ').replace('\n', ' ').replace('\r', ' ').trim();
        if (limpo.isEmpty()) {
            limpo = "Jogador";
        }
        if (limpo.length() > TAMANHO_MAXIMO_NOME) {
            limpo = limpo.substring(0, TAMANHO_MAXIMO_NOME);
        }
        this.nomeJogador = limpo;
        RankingArquivo.salvarUltimoNome(limpo);
    }

    @Override
    public void create() {
        String ultimoNome = RankingArquivo.carregarUltimoNome();
        if (ultimoNome != null) {
            setNomeJogador(ultimoNome); // o jogo já abre com o último jogador
        }
        mostrarMenu();
    }

    public void mostrarMenu() {
        setScreen(new MenuScreen(this));
    }

    public void iniciarPartida() {
        setScreen(new JogoScreen(this, new Partida(Cardapio.carregar())));
    }

    /** Nível concluído: abre a loja com a mesma partida. */
    public void mostrarLoja(Partida partida) {
        setScreen(new LojaScreen(this, partida));
    }

    /** Sai da loja e continua a mesma partida no nível seguinte. */
    public void continuarPartida(Partida partida) {
        partida.iniciarProximoNivel();
        setScreen(new JogoScreen(this, partida));
    }

    public void mostrarGameOver(Partida partida) {
        setScreen(new GameOverScreen(this, partida.getPontos(), partida.getEntregues(), partida.getTempo()));
    }
}
