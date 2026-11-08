import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner; 

public class App {

    private static final int WIDTH = 20;
    private static final int HEIGHT = 10;
    private static final char WALL_CHAR = '#';
    private static final char SNAKE_HEAD_CHAR = 'O';
    private static final char SNAKE_BODY_CHAR = 'o';
    private static final char FOOD_CHAR = '*';
    private static final char EMPTY_CHAR = ' ';
    public static void main(String[] args)
    {
        ArrayList<Point> initialBody = new ArrayList<>();
        initialBody.add(new Point (5, 5));
        initialBody.add(new Point(4,5));
        initialBody.add(new Point(3, 5));
        Snake snake = new Snake(initialBody, 1, 0);
        GameBoard board = new GameBoard(WIDTH, HEIGHT);
        board.spawnFood(snake);
        
        GameEngine engine = new GameEngine(board, snake);
        engine.startGameLoop();
    }

    public static class Point
    {
        int x;
        int y;

        Point (int x, int y)
        {
            this.x = x;
            this.y = y;
        }
        @Override 
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj instanceof Point other)
            {
                return this.x == other.x && this.y == other.y;
            }
            return false;
        }
    }

    public static class Snake
    {
        List<Point> body = new ArrayList<>();
        int dirX = 1;
        int dirY = 0;

        public Snake(ArrayList<Point> body, int dirX, int dirY)
        {
            this.body = body;
            this.dirX = dirX;
            this.dirY = dirY;
        }

        public Point getHead()
        {
            return body.get(0);
        }

        public void changeDirection(int newDirX, int newDirY)
        {
           if (this.dirX != 0 && newDirX == 0)
            {
                this.dirX = newDirX;
                this.dirY = newDirY;
            }
            else if (this.dirY != 0 && newDirY == 0)
            {
                this.dirX = newDirX;
                this.dirY = newDirY;
            }
        }

        public void move()
        {
            Point newHead = new Point(getHead().x + dirX, getHead().y + dirY);
            body.add(0, newHead);
        }

        public void trimTail()
        {
            body.remove(body.size() - 1);
        }

        public boolean checkCollision()
        {
            Point head = getHead();
            if (head.x < 0 || head.x >= WIDTH || head.y < 0 || head.y >= HEIGHT)
            {
                return true;
            }
            for (int i = 1; i < body.size(); i++)
            {
                if (head.equals(body.get(i)))
                {
                    return true;
                }
            }
            return false;
        }
    }

    public static class GameBoard
    {
        private int width;
        private int height;
        private Point food;

        public GameBoard(int w, int h)
        {
            width = w;
            height = h;
        }

        public void spawnFood(Snake snake)
        {
            Random rand = new Random();
            int x = rand.nextInt(width - 2) + 1;
            int y = rand.nextInt(height - 2) + 1;
            for (Point p : snake.body)
            {
                if(p.x == x && p.y == y)
                {
                    spawnFood(snake);
                    return;
                }
            }
            food = new Point(x,y);
        }

        public boolean outOfBoundsCheck(Snake snake)
        {
            Point head = snake.getHead();
            return (head.x <= 0 || head.x >= width - 1 || head.y <= 0 || head.y >= height - 1);
        }

        public void drawScreen(Snake snake)
        {
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    if (x == 0 || x == width - 1 || y == 0 || y == height - 1)
                    {
                        System.out.print(WALL_CHAR);
                    }
                    else if (x == snake.getHead().x && y == snake.getHead().y)
                    {
                        System.out.print(SNAKE_HEAD_CHAR);
                    }
                    else if (x == food.x && y == food.y)
                    {
                        System.out.print(FOOD_CHAR);
                    }
                    else 
                    {
                        boolean isBodyPart = false;
                        for (int i = 1; i < snake.body.size(); i++)
                        {
                            if (x == snake.body.get(i).x && y == snake.body.get(i).y)
                            {
                                isBodyPart = true;
                                break;
                            }
                        }
                        if (isBodyPart)
                        {
                            System.out.print(SNAKE_BODY_CHAR);
                        }
                        else 
                        {
                            System.out.print(EMPTY_CHAR);
                        }
                    }
                }
                System.out.println();
            }
        }
    }

    public static class GameEngine
    {
        private GameBoard board;
        private Snake snake;
        private int score;
        private boolean isOver;

        public GameEngine(GameBoard board, Snake snake)
        {
            this.board = board; 
            this.snake = snake;
            this.score = 0;
            this.isOver = false;
        }

        public void startGameLoop()
        {
            Thread inputThread = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (!isOver)
                {
                    if (scanner.hasNextLine())
                    {
                        String input = scanner.nextLine().trim().toLowerCase();
                        if (input.isEmpty())
                        {
                            continue;
                        }
                        char key = input.charAt(0);
                        if (key == 'w')
                        {
                            snake.changeDirection(0, -1);
                        }
                        else if (key == 's')
                        {
                            snake.changeDirection(0, 1);
                        }
                        else if (key == 'a')
                        {
                            snake.changeDirection(-1, 0);
                        }
                        else if (key == 'd')
                        {
                            snake.changeDirection(1, 0);
                        }
                        else if (key == 'q')
                        {
                            System.out.println("Quitting game...");
                            System.exit(0);
                        }
                    }
                }
                scanner.close();
            });
            inputThread.setDaemon(true);
            inputThread.start();

            while (!isOver)
            {
                System.out.print("\033[H\033[2J");
                System.out.flush();
                snake.move();
                if (snake.getHead().equals(board.food))
                {
                    score += 10;
                    board.spawnFood(snake);
                }
                else 
                {
                    snake.trimTail();
                }
                if (snake.checkCollision() || board.outOfBoundsCheck(snake))
                {
                    isOver = true;
                    break;
                }
                board.drawScreen(snake);
                System.out.println("Score: " + score + " [Controls: W/A/S/D + Enter]");

                try {
                    Thread.sleep(200);
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.print("\033[H\033[2J");
            System.out.flush();
            System.out.println("===========================================");
            System.out.println("      GAME OVER: HIT A WALL/TAIL      ");
            System.out.println("      Your Final Score: " + score);
            
        }
    }
}



