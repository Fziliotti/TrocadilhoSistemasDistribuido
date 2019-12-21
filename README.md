# TrocadilhoSistemasDistribuido


[x] - Arquivo da Log Structure Merge Tree
<br>
[x] - Log + Snapshot
<br>
[x] - Testes
<br>
[X] - CHORD
<br>
[X] - nao deixar hardcoded
<br>
[x] - Docker
<br>
[x] - Atomix

# INTRODUÇÃO

Esse trabalho é referente ao projeto https://github.com/marcosbarra/projeto-sistemas-distribuidos, mas sofreu algumas modificações ao longo do projeto, tornando uma espécie de crud de trocadilhos.

## RESUMO DO PROJETO

Será desenvolvido um jogo de trocadilhos reunindo vários jogadores. O jogo será separado em rodadas e cada rodada consiste em, dado algum tema sorteado ou palavra sugerido pelo jogo ou pelos próprios jogadores, um tempo será dado para cada pessoa escrever um trocadilho relacionado com aquele tema. Ao fim de cada rodada, todos os trocadilhos são exibidos e todos os jogadores avaliam. O trocadilho com mais votos vence a rodada e o jogador acumula uma pontuação. Ao fim de alguams rodadas, quem tiver a maior pontuação é o vencedor.

<hr>

## REQUISITOS DO PROJETO

- Java instalado na máquina;
- Versão do JDK igual a 8.0 ou superior;

<hr>

## Funcionamento do jogo:

1. Abra o projeto em sua IDE;
2. Importe-o como um projeto Maven e instale as depêndencias;
2. Configure em constants.txt a quantidade de clusters e a quantidade de servidores em cada cluster;
3. Depois execute o arquivo trocadilhos.grpc.Server.java quantas vezes for necessário de acordo com o passo 3;
4. Após isso, inicie os clientes (Dependendo do número de jogadores no round);

<hr>

## Grupo 

Eduardo Ferreira de Oliveira
<br>
Fabrício Fernandes Ziliotti
<br>
Guilherme Raimondi
<br>
Marcos Victor de Aquino Barra

