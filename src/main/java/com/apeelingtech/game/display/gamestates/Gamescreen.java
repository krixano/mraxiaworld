package com.apeelingtech.game.display.gamestates;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JOptionPane;

import com.apeelingtech.game.Game;
import com.apeelingtech.game.display.Display;
import com.apeelingtech.game.display.gui.GameGUI;
import com.apeelingtech.game.entities.Player;
import com.apeelingtech.game.entities.PlayerMP;
import com.apeelingtech.game.level.Level;
import com.apeelingtech.game.net.GameClient;
import com.apeelingtech.game.net.GameServer;
import com.apeelingtech.game.net.packets.Packet00Login;

public class Gamescreen extends GameState {
	
	public Player player;
	public Level level;
	
	public GameClient socketClient;
	public GameServer socketServer;
	
	private SetupState.CharacterColor shirtColor = SetupState.CharacterColor.DEFAULT;
	private SetupState.CharacterSColor skinColor = SetupState.CharacterSColor.DEFAULT;
	private byte characterType = 1;
	
	public Gamescreen(Game game, GameGUI gui, Display display, SetupState.CharacterColor shirtColor, SetupState.CharacterSColor skinColor, byte characterType) {
		super(game, Color.GREEN, gui, display);
		this.shirtColor = shirtColor;
		this.skinColor = skinColor;
		this.characterType = characterType;
		gui.addGameScreen(this);
		init();
		// display.changeCurrentGameState(1); // Change State to loadingState!
	}
	
	public void init() {
		if (!game.isApplet) {
			if (JOptionPane.showConfirmDialog(game, "Do you want to run the server?") == 0) {
				socketServer = new GameServer(this);
				socketServer.start();
				
				socketClient = new GameClient(this, "localhost");
			} else {
				if (JOptionPane.showConfirmDialog(game, "Do you want to connect to a server?") == 0) {
					socketClient = new GameClient(this, JOptionPane.showInputDialog("What IP do you want to connect to?", "localhost"));
				} else {
					socketClient = new GameClient(this, "localhost");
				}
			}
			socketClient.start();
		}
		
		level = new Level("/levels/water_test_level.png");
		
		player = new PlayerMP(level, 100, 100, (GameGUI) gui, JOptionPane.showInputDialog("Please enter a username", ""), null, -1, shirtColor.getColor(), skinColor.getSkinColor(), characterType);
		level.addEntity(player);
		if (!game.isApplet) {
			Packet00Login loginPacket = new Packet00Login(player.getUsername(), player.x, player.y, player.getShirtColor(), player.getSkinColor(), characterType);
			if (socketServer != null) {
				socketServer.addConnection((PlayerMP) player, loginPacket);
			}
			loginPacket.writeData(socketClient);
		}
	}
	
	@Override
	public void tick() {
		// game.level.tick((int) game.sX, (int) game.sY, (Game.GAME_SIZE.width / Tile.tileSize) + 2, (Game.GAME_SIZE.height / Tile.tileSize) + 2);
		// charactermp.tick();
		// game.inventory.tick();
		// gui.tick();
		
		level.tick();
	}
	
	@Override
	public void render(Graphics2D g) {
		
		int xOffset = player.x - (game.screen.width / 2);
		int yOffset = player.y - (game.screen.height / 2);
		
		level.renderTiles(game.screen, xOffset, yOffset);
		level.renderEntities(game.screen);
		
		for (int y = 0; y < game.screen.height; y++) {
			for (int x = 0; x < game.screen.width; x++) {
				int colourCode = game.screen.pixels[x + y * game.screen.width];
				if (colourCode < 255)
					game.pixels[x + y * Game.WIDTH] = game.colours[colourCode];
			}
		}
	}
	
	@Override
	public GameGUI getGUI() {
		return (GameGUI) gui;
	}
	
}