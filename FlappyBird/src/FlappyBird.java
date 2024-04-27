import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;


public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    // Variabel boolean untuk menandai apakah permainan sudah berakhir
    private boolean gameOver;


    // Variabel untuk skor
    private int score;

    // Konstanta gravitasi
    private final int gravity = 1;

    // Konstanta lebar dan tinggi frame
    private final int frameWidth = 360;
    private final int frameHeight = 640;

    // Image attributes
    private Image backgroundImage;
    private Image birdImage;
    private Image lowerPipeImage;
    private Image upperPipeImage;

    // Player attributes
    private final int playerStartPosX = frameWidth / 8;
    private final int playerStartPosY = frameHeight / 2;
    private final int playerWidth = 34;
    private final int playerHeight = 24;
    private Player player;

    // Pipe attributes
    private final int pipeStartPosX = frameWidth;
    private final int pipeStartPosY = 0;
    private final int pipeWidth = 64;
    private final int pipeHeight = 512;
    private ArrayList<Pipe> pipes;
    private Timer pipesCooldown;
    private Timer gameLoop;

    // Skor attributes
    private JLabel scoreLabel;

    public FlappyBird() {
        setPreferredSize(new Dimension(frameWidth, frameHeight));
        setFocusable(true);
        addKeyListener(this);

        backgroundImage = new ImageIcon(getClass().getResource("assets/background.png")).getImage();
        birdImage = new ImageIcon(getClass().getResource("assets/bird.png")).getImage();
        lowerPipeImage = new ImageIcon(getClass().getResource("assets/lowerPipe.png")).getImage();
        upperPipeImage = new ImageIcon(getClass().getResource("assets/upperPipe.png")).getImage();

        player = new Player(playerStartPosX, playerStartPosY, playerWidth, playerHeight, birdImage);
        pipes = new ArrayList<>();

        pipesCooldown = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                placePipes();
            }
        });

        pipesCooldown.start();
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();

        // Inisialisasi skor
        score = 0;
        scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setBounds(frameWidth - 100, 20, 100, 20); // Menentukan posisi dan ukuran label skor
        add(scoreLabel);

        // Inisialisasi status permainan
        gameOver = false;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, frameWidth, frameHeight, null);
        g.drawImage(player.getImage(), player.getPosX(), player.getPosY(), player.getWidth(), player.getHeight(),  null);

        for(int i = 0; i < pipes.size(); i++){
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.getImage(), pipe.getPosX(), pipe.getPosY(), pipe.getWidth(), pipe.getHeight(), null);
        }
    }

    public class Player {
        private int posX;
        private int posY;
        private int width;
        private int height;
        private Image image;
        private int velocityY;
        public Player(int posX, int posY, int width, int height, Image image) {
            this.posX = posX;
            this.posY = posY;
            this.width = width;
            this.height = height;
            this.image = image;

            this.velocityY = -0;
        }

        public int getPosX() {
            return posX;
        }

        public void setPosX(int posX) {
            this.posX = posX;
        }

        public int getPosY() {
            return posY;
        }

        public void setPosY(int posY) {
            this.posY = posY;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public Image getImage() {
            return image;
        }

        public void setImage(Image image) {
            this.image = image;
        }

        public int getVelocityY() {
            return velocityY;
        }

        public void setVelocityY(int velocityY) {
            this.velocityY = velocityY;
        }
    }

    public void move(){
        if (!gameOver) {
            // Pergerakan pemain
            player.setVelocityY(player.getVelocityY() + gravity);
            player.setPosY(player.getPosY() + player.getVelocityY());
            player.setPosY(Math.max(player.getPosY(), 0));

            // Pergerakan pipa
            for (int i = 0; i < pipes.size(); i++) {
                Pipe pipe = pipes.get(i);
                pipe.setPosX(pipe.getPosX() + pipe.getVelocityX());

                // Deteksi tabrakan antara pemain dan pipa
                if (player.getPosX() + player.getWidth() > pipe.getPosX() && player.getPosX() < pipe.getPosX() + pipe.getWidth()) {
                    if (player.getPosY() < pipe.getPosY() + pipe.getHeight() && player.getPosY() + player.getHeight() > pipe.getPosY()) {
                        // Cek jika pipa sudah dilewati, jika belum, maka akhirkan permainan
                        if (!pipe.isPassed()) {
                            gameOver = true;
                        }
                    }
                }

                // Deteksi jika pemain jatuh ke bawah
                if (player.getPosY() + player.getHeight() >= frameHeight) {
                    gameOver = true;
                }

                // Deteksi jika pemain melewati sepasang pipa atas dan bawah
                if (!pipe.isPassed() && pipe.getPosX() + pipe.getWidth() < player.getPosX()) {
                    pipe.setPassed(true);
                    // Periksa apakah ini sepasang pipa atas dan bawah
                    if (i % 2 == 0) {
                        score++; // Tambah skor
                        scoreLabel.setText("Score: " + score); // Perbarui tampilan label skor
                        System.out.println("Skor bertambah: " + score); // Debug
                    }
                }


            }

            // Jika permainan berakhir, hentikan Timer
            if (gameOver) {
                gameLoop.stop();
                pipesCooldown.stop();
            }
        }
    }

    public void placePipes(){
        if (!gameOver) {
            int randomPosY = (int) (pipeStartPosY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
            int openingSpace = frameHeight / 4;

            Pipe upperPipe = new Pipe(pipeStartPosX, randomPosY, pipeWidth, pipeHeight, upperPipeImage);
            pipes.add(upperPipe);

            Pipe lowerPipe = new Pipe(pipeStartPosX, (randomPosY + openingSpace + pipeHeight), pipeWidth, pipeHeight, lowerPipeImage);
            pipes.add(lowerPipe);
        }
    }
    @Override
    public void actionPerformed(ActionEvent e){
        move();
        repaint();
    }
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameOver) {
            if(e.getKeyCode() == KeyEvent.VK_SPACE){
                player.setVelocityY(-10);
            }
        } else {
            // Jika permainan berakhir, dan tombol "R" ditekan, restart permainan
            if (e.getKeyCode() == KeyEvent.VK_R) {
                restartGame();
            }
        }
    }
    private void restartGame() {
        // Reset variabel dan status permainan
        gameOver = false;
        score = 0;
        scoreLabel.setText("Score: " + score); // Perbarui tampilan label skor
        pipes.clear();
        player.setPosY(playerStartPosY);
        player.setVelocityY(0); // Atur kembali kecepatan vertikal pemain ke 0
        gameLoop.start();
        pipesCooldown.start();
    }



    @Override
    public void keyReleased(KeyEvent e) {

    }
}




