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

- `ENTER`: iniciar/jogar novamente no menu, atender o primeiro cliente ou começar o próximo nível na loja.
- `1` a `9`: adicionar ingrediente (1 pão, 2 carne, 3 queijo, 4 alface, 5 tomate; 6 cebola, 7 picles, 8 bacon, 9 ovo depois de comprados na loja).
- `BACKSPACE`: desfazer o último ingrediente.
- `ESPAÇO`: entregar o hambúrguer.
- `1` a `4`: (na loja) comprar o ingrediente da linha correspondente.
- `F`: (na partida) pesquisar um cliente pelo nome nos pedidos ativos e na fila. O jogo fica pausado enquanto você digita.
- `N`: (no menu) trocar o nome do jogador. O nome é pedido na primeira vez que o menu abre e é usado ao salvar a pontuação no ranking. A digitação acontece numa caixa de texto desenhada no próprio jogo (`telas/CampoTexto.java`): ENTER confirma e ESC cancela.
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
- O menu mostra o top 5 com a melhor pontuação de cada jogador (`RankingArquivo.melhoresPorJogador`, que usa busca sequencial para saber se o nome já entrou). A busca binária (`B`) usa todas as partidas. Linhas inválidas no `ranking.txt` são ignoradas.
- O último nome usado fica em `jogador.txt` e é carregado ao abrir o jogo.
- Para testar, execute `com.hamburgueria.logica.TesteAlgoritmos` (lista vazia, repetidos, não encontrado, estabilidade, 200 casos aleatórios por algoritmo, ranking e pesquisa na partida).

## Níveis, loja e receitas

- Cada nível tem uma meta de entregas (`Partida.getMetaEntregas`: 4 no nível 1, +2 por nível). Ao cumprir a meta o balcão é limpo, o jogador ganha +50 pontos e moedas de bônus (`LojaScreen` abre em seguida).
- Cada entrega rende moedas: 10% do valor da receita mais uma gorjeta proporcional à paciência que sobrou (`Pedido.calcularGorjeta`).
- `logica/Loja.java`: lista os ingredientes ainda não comprados, do mais barato ao mais caro (Insertion Sort por preço), e cobra a compra (`ResultadoCompra`). O desbloqueio fica num vetor `boolean[]` da `Partida` indexado pelo ordinal do ingrediente.
- Ingredientes novos: cebola (25), picles (30), bacon (40), ovo (50). Eles só entram em pedidos depois de comprados.
- Quanto maior o nível, mais difícil: clientes chegam mais rápido, têm menos paciência, até 4 pedidos ao mesmo tempo (3 no nível 1) e receitas com até `3 + nível` ingredientes (máximo 8). `Partida.sortearReceita` só escolhe receitas permitidas pelo nível e cujos ingredientes já estão liberados.
- Os clientes andam até o lugar deles (fila ou ponto de espera) com uma animação simples (`Cliente.setPosicaoNaTela`, atualizada em `JogoScreen.animarClientes`).
- `TestePartida` cobre níveis, gorjeta, loja e filtro de receitas.

## Cenário em pixel art

A tela principal usa os props em `assets/sprites/props/` e o piso quadriculado `assets/tiles/chao_xadrez_bege.png`.
Os sprites foram organizados individualmente para facilitar ajustes de escala e posição em `JogoScreen`.
Os nomes dos arquivos descrevem os objetos (geladeira, fogão, pia, bancada, prateleiras, plantas, luminária etc.).

## Atualização visual do cenário

A tela de jogo foi reorganizada para apresentar a cozinha à esquerda e o salão à direita, com piso bege quadriculado, parede e molduras de madeira, divisória central, janelas, porta, luminárias e plantas. Os equipamentos existentes são desenhados como sprites individuais e alinhados na parede da cozinha; a ilha de montagem e o balcão ficam em áreas separadas. O salão não inclui mesas para clientes.

A atualização visual está concentrada em `JogoScreen.java` e não altera as classes de lógica (`Partida`, `Cozinha`, pedidos, fila, pontuação, vidas ou loja). A escala de mundo continua em 960 x 640 e usa `FitViewport`.

## Sprites de ingredientes

Os ingredientes do hambúrguer agora são exibidos por sprites pixel art individuais, tanto na pilha de montagem quanto na legenda de ingredientes desbloqueados. Os arquivos ficam em `assets/sprites/ingredientes/` e incluem pão, carne, queijo, alface, tomate, cebola, picles, bacon e ovo, além de uma spritesheet combinada.

## Dificuldade progressiva por dia

- As receitas sorteadas ficam progressivamente mais complexas conforme o dia, respeitando ingredientes desbloqueados na loja.
- O intervalo de chegada começa em 9,5 segundos e diminui 1,4 segundo por dia, até o mínimo de 2,5 segundos.
- A paciência inicial diminui em 6 segundos por dia, até o mínimo de 20 segundos (mais uma variação aleatória de até 5 segundos).
- Todos os clientes da fila perdem paciência desde a chegada, não apenas o primeiro; pedidos ativos também perdem paciência mais rápido nos dias avançados.
- A velocidade de perda de paciência aumenta 12% por dia após o primeiro.

