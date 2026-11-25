# Hamburgueria Maluca

Jogo desenvolvido em Java com LibGDX para a disciplina de Estrutura de Dados I.

## Como executar

Abra o projeto no IntelliJ IDEA e execute a classe:

`com.hamburgueria.lwjgl3.Lwjgl3Launcher`

Também é possível executar pelo Gradle com `./gradlew lwjgl3:run` (Linux/macOS) ou `gradlew.bat lwjgl3:run` (Windows).

## Telas

- `HamburgueriaGame`: controla a navegação entre as telas.
- `MenuScreen`: menu inicial.
- `JogoScreen`: partida e controles.
- `GameOverScreen`: resultado da partida e opção de jogar novamente.

## Controles

- `ENTER`: iniciar/jogar novamente no menu ou atender o primeiro cliente.
- `1` a `5`: adicionar pão, carne, queijo, alface e tomate.
- `BACKSPACE`: desfazer o último ingrediente.
- `ESPAÇO`: entregar o hambúrguer.
- `F`: (na partida) pesquisar um cliente pelo nome nos pedidos ativos e na fila. O jogo fica pausado enquanto você digita.
- `B`: (no menu) pesquisar uma pontuação no ranking.
- `ESC`: voltar ao menu durante a partida ou sair no menu.

A lógica da partida permanece na classe `Partida`; as telas cuidam da entrada, desenho e navegação.

## Matriz do cenário

- `modelo/Cenario.java` cria o mapa usando a matriz `Tile[][]` (10 linhas x 15 colunas).
- `modelo/Tile.java` define os tipos de célula: chão, balcão, chapa e bancada.
- `JogoScreen` percorre a matriz e desenha cada célula na tela.
- Para testar a matriz no terminal, execute `com.hamburgueria.modelo.TesteCenario`.

## Arquivos

- `assets/receitas.txt`: cardápio carregado no início de cada partida. Cada linha segue o formato `nome|pontos|INGREDIENTE,INGREDIENTE,...`. Linhas iniciadas por `#` são comentários.
- Se `receitas.txt` estiver ausente ou inválido, o jogo utiliza `Cardapio.padrao()` como reserva.
- `ranking.txt`: criado no diretório local do usuário pelo LibGDX quando a partida termina. O jogo solicita o nome do jogador e acrescenta uma linha no formato `nome|pontos`.

## Algoritmos de ordenação e busca

Tudo está em `estruturas/Algoritmos.java`, genérico e sem `java.util` (critérios via as interfaces `Comparador<T>` e `Condicao<T>`).

| Algoritmo | Onde é usado no jogo |
|---|---|
| Insertion Sort em vetor | `RankingArquivo.interpretar`: ordena o ranking por pontuação (maior primeiro) ao carregar o `ranking.txt`. |
| Insertion Sort em lista encadeada | `Partida.ordenarPedidosPorUrgencia`: a cada frame, mantém `pedidosAtivos` ordenada pela paciência restante. Serve para mostrar quem está mais perto de desistir; o preparo segue a ordem de chegada (quem pediu primeiro). |
| Busca sequencial | `Partida.pesquisar` (tecla `F`): procura o cliente em `pedidosAtivos` (lista) e na `Fila`, usando o mesmo método a partir do primeiro nó. |
| Busca binária | `RankingArquivo.buscarPontuacao` (tecla `B` no menu): procura a pontuação no ranking já ordenado e devolve a primeira ocorrência. |

- `modelo/RegistroRanking.java`: nome e pontuação de uma linha do ranking, com o comparador `POR_PONTOS_DECRESCENTE`.
- O menu mostra o top 5 (`RankingArquivo.melhores`). Linhas inválidas no `ranking.txt` são ignoradas.
- Para testar, execute `com.hamburgueria.logica.TesteAlgoritmos` (lista vazia, repetidos, não encontrado, estabilidade, 200 casos aleatórios por algoritmo, ranking e pesquisa na partida).
