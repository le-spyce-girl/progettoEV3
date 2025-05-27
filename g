<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="UTF-8">
  <title>Gioco Checkpoint, Bombe e Vite Bonus</title>
  <style>
    body {
      margin: 0;
      height: 100vh;
      background-color: #f0f0f0;
      display: flex;
      justify-content: center;
      align-items: center;
      flex-direction: column;
      font-family: Arial, sans-serif;
    }

    #container {
      position: relative;
      width: 600px;
      height: 400px;
      background-color: #ffffff;
      border: 2px solid #333;
    }

    #object {
      position: absolute;
      width: 20px;
      height: 20px;
      background-color: red;
      top: 190px;
      left: 290px;
      transition: top 0.1s, left 0.1s;
    }

    .checkpoint, .bomb, .bonus-life {
      position: absolute;
      width: 20px;
      height: 20px;
      border-radius: 50%;
    }

    .checkpoint { background-color: green; }
    .bomb { background-color: red; }
    .bonus-life { background-color: dodgerblue; }

    #score, #lives {
      font-size: 20px;
      margin: 5px;
    }

    #gameOver {
      display: none;
      font-size: 24px;
      color: red;
      margin-top: 10px;
    }

    #nameInput {
      display: none;
      margin-top: 10px;
    }

    #showScores {
      margin-top: 10px;
    }
  </style>
</head>
<body>

  <div id="score">Punteggio: 0</div>
  <div id="lives">Vite: 3</div>
  <div id="gameOver">Game Over!</div>
  <div id="container">
    <div id="object"></div>
  </div>
  <div id="nameInput">
    <label for="playerName">Inserisci il tuo nome: </label>
    <input type="text" id="playerName" />
    <button onclick="saveHighScore()">Salva</button>
  </div>
  <button id="showScores" onclick="showHighScores()">Mostra Classifica</button>

  <script>
    const obj = document.getElementById('object');
    const container = document.getElementById('container');
    const scoreDisplay = document.getElementById('score');
    const livesDisplay = document.getElementById('lives');
    const gameOverDisplay = document.getElementById('gameOver');
    const nameInput = document.getElementById('nameInput');
    const playerNameInput = document.getElementById('playerName');

    let topPos = 190;
    let leftPos = 290;
    let score = 0;
    let lives = 3;
    const maxLives = 5;

    let checkpoint = null;
    let bomb = null;
    let bonusLife = null;

    function getRandomPosition() {
      const x = Math.floor(Math.random() * (container.offsetWidth - 20));
      const y = Math.floor(Math.random() * (container.offsetHeight - 20));
      return { x, y };
    }

    function createCheckpoint() {
      if (checkpoint) checkpoint.remove();
      const { x, y } = getRandomPosition();
      checkpoint = document.createElement('div');
      checkpoint.classList.add('checkpoint');
      checkpoint.style.left = `${x}px`;
      checkpoint.style.top = `${y}px`;
      container.appendChild(checkpoint);
    }

    function createBomb() {
      if (bomb) bomb.remove();
      const { x, y } = getRandomPosition();
      bomb = document.createElement('div');
      bomb.classList.add('bomb');
      bomb.style.left = `${x}px`;
      bomb.style.top = `${y}px`;
      container.appendChild(bomb);
    }

    function createBonusLife() {
      if (bonusLife) bonusLife.remove();
      const { x, y } = getRandomPosition();
      bonusLife = document.createElement('div');
      bonusLife.classList.add('bonus-life');
      bonusLife.style.left = `${x}px`;
      bonusLife.style.top = `${y}px`;
      container.appendChild(bonusLife);

      // Rimuovi dopo 10 secondi se non presa
      setTimeout(() => {
        if (bonusLife) {
          bonusLife.remove();
          bonusLife = null;
        }
      }, 10000);
    }

    function randomBonusLifeSpawner() {
      const delay = Math.floor(Math.random() * 10000) + 10000; // ogni 10-20 secondi
      setTimeout(() => {
        createBonusLife();
        randomBonusLifeSpawner(); // ricorsione per il prossimo spawn
      }, delay);
    }

    function move(direction) {
      const step = 10;
      if (direction === 'up') topPos -= step;
      else if (direction === 'down') topPos += step;
      else if (direction === 'left') leftPos -= step;
      else if (direction === 'right') leftPos += step;

      topPos = Math.max(0, Math.min(topPos, container.offsetHeight - 20));
      leftPos = Math.max(0, Math.min(leftPos, container.offsetWidth - 20));

      obj.style.top = `${topPos}px`;
      obj.style.left = `${leftPos}px`;

      checkCollision();
    }

    function checkCollision() {
      const objRect = obj.getBoundingClientRect();

      if (checkpoint) {
        const cpRect = checkpoint.getBoundingClientRect();
        if (isColliding(objRect, cpRect)) {
          score++;
          scoreDisplay.textContent = `Punteggio: ${score}`;
          createCheckpoint();
          createBomb();
        }
      }

      if (bomb) {
        const bombRect = bomb.getBoundingClientRect();
        if (isColliding(objRect, bombRect)) {
          lives--;
          livesDisplay.textContent = `Vite: ${lives}`;
          createBomb();
          if (lives <= 0) {
            gameOver();
          }
        }
      }

      if (bonusLife) {
        const lifeRect = bonusLife.getBoundingClientRect();
        if (isColliding(objRect, lifeRect)) {
          if (lives < maxLives) {
            lives++;
            livesDisplay.textContent = `Vite: ${lives}`;
          }
          bonusLife.remove();
          bonusLife = null;
        }
      }
    }

    function isColliding(r1, r2) {
      return !(
        r1.right < r2.left ||
        r1.left > r2.right ||
        r1.bottom < r2.top ||
        r1.top > r2.bottom
      );
    }

    function gameOver() {
      gameOverDisplay.style.display = 'block';
      nameInput.style.display = 'block';
      document.removeEventListener('keydown', handleKeydown);
    }

    function saveHighScore() {
      const name = playerNameInput.value.trim();
      if (name) {
        const scores = JSON.parse(localStorage.getItem('highScores') || '[]');
        scores.push({ name, score });
        scores.sort((a, b) => b.score - a.score);
        localStorage.setItem('highScores', JSON.stringify(scores));
        alert("Punteggio salvato!");
        location.reload();
      }
    }

    function showHighScores() {
      const scores = JSON.parse(localStorage.getItem('highScores') || '[]');
      if (scores.length === 0) {
        alert("Nessuna classifica salvata.");
        return;
      }
      const list = scores
        .map((entry, index) => `${index + 1}. ${entry.name}: ${entry.score}`)
        .join("\n");
      alert("Classifica:\n" + list);
    }

    function handleKeydown(event) {
      const key = event.key.toLowerCase();
      if (key === 'w' || event.key === 'arrowup') move('up');
      else if (key === 'a' || event.key === 'arrowleft') move('left');
      else if (key === 's' || event.key === 'arrowdown') move('down');
      else if (key === 'd' || event.key === 'arrowright') move('right');
    }

    document.addEventListener('keydown', handleKeydown);

    // Avvio gioco
    createCheckpoint();
    randomBonusLifeSpawner();
  </script>
</body>
</html>
