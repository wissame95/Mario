package engine;

import game.Game;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static dataMapping.Data.*;
import static org.lwjgl.opengl.GL11.*;

public class Launcher {

	public static final String TITLE = "GAME FACTORY";
	public static final int MILLISECONDS = (int) Math.pow(10, 3);
	public static final int NANOSECONDS = (int) Math.pow(10, 9);
	public static final double UPDATE_RATE = NANOSECONDS / 60.0;

	public static boolean running = false;
	public static int width = 824;
	public static int height = 900;

	public Game game;

    public static ExecutorService poolThread =  Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    static Future stateOfSoundsReading;

    public Launcher() {
        display();
        generateFonts().run();
        generateSkins().run();
        generateTextures().run();
        do{
            //Wait
        }while (!stateOfSoundsReading.isDone());
        System.out.println("LOADED : ");
        game = new Game();
	}

	public static void main(String[] args) {
	    System.out.println("Credential : ");

        stateOfSoundsReading = poolThread.submit(generateSounds());

        switch (System.getProperty("os.name")){
            case "Mac OS X":
                System.setProperty("org.lwjgl.librarypath", new File("natives/macosx").getAbsolutePath());
                break;
            case "Windows 10":
                System.setProperty("org.lwjgl.librarypath", new File("natives/windows").getAbsolutePath());
                break;
        }

		Launcher main = new Launcher();

        main.start();
	}

	/**
	 * Signale l'arrêt du jeu.
	 */
	public static void stop() {
		running = false;
	}

	/**
	 * Signale le début du jeu.
	 */
	public void start() {
		running = true;
		loop();
	}

	/**
	 * Arrête la musique, ferme la fenêtre et quitte le programme.
	 */
	public void exit() {
		Display.destroy();
        AL.destroy();
		System.exit(0);
	}

	/**
	 * Régule la vitesse du jeu en comptant les ticks.
	 * Deux ticks consécutifs sont séparées par une durée fixée.
	 * Les données du jeu, ainsi que l'affichage, sont actualisés à chaque tick.
	 */
	public void loop() {

		double elapsed;
        int frames = 0;
		int ticks = 0;
		long before = System.nanoTime();
		long timer = System.currentTimeMillis();

		while(running) {

			if(Display.isCloseRequested())
				stop();

			Display.update();

			width = Display.getWidth();
			height = Display.getHeight();
			elapsed = System.nanoTime() - before;

			if(elapsed > UPDATE_RATE) {
				before += UPDATE_RATE;
				update();
				ticks++;
			} else {
				render();
				frames++;
			}

			if(System.currentTimeMillis() - timer > MILLISECONDS) {
				timer += MILLISECONDS;
				Display.setTitle(TITLE + " - Ticks : " + ticks + ", FPS : " + frames);
				ticks = 0;
				frames = 0;
			}
		}

		exit();

	}

	/**
	 * Actualise toutes les données du jeu.
	 */
	public void update() {
		game.update();
	}

	/**
	 * Affiche tous les compsosants du jeu.
	 */
	public void render() {
		view2D(width, height);
		glClear(GL_COLOR_BUFFER_BIT);
		game.render();
	}

	/**
	 * Ouvre la fenêtre du programme.
	 */
	public void display() {
		try {
			Display.setDisplayMode(new DisplayMode(width, height));
			Display.setResizable(true);
			Display.setFullscreen(false);
			Display.setTitle(TITLE);
			Display.create(); //Initialisation du context opengl
            //Display.setVSyncEnabled(true);
			view2D(width, height);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Définit les propriétés d'affichage de la fenêtre.
	 * @param width		la largeur de la fenêtre
	 * @param height	la hauteur de la fenêtre
	 */
	private void view2D(int width, int height) {
        glEnable(GL_TEXTURE_2D);

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // enable alpha blending
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glViewport(0, 0, width, height);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		GLU.gluOrtho2D(0, width, height, 0);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}

}
