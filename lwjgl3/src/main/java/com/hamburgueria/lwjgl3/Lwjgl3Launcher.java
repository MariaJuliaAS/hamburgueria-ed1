package com.hamburgueria.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.hamburgueria.HamburgueriaGame;

/** Inicia o jogo no desktop. Rode esta classe para abrir a janela. */
public class Lwjgl3Launcher {

    public static void main(String[] args) {
        new Lwjgl3Application(new HamburgueriaGame(), criarConfiguracao());
    }

    private static Lwjgl3ApplicationConfiguration criarConfiguracao() {
        Lwjgl3ApplicationConfiguration configuracao = new Lwjgl3ApplicationConfiguration();
        configuracao.setTitle("Hamburgueria Maluca");
        configuracao.useVsync(true);
        configuracao.setForegroundFPS(60);
        configuracao.setWindowedMode(960, 640);
        return configuracao;
    }
}
