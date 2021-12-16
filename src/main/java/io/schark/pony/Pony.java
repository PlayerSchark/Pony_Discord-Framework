package io.schark.pony;

import io.schark.pony.core.PonyBot;
import io.schark.pony.feat.commands.registry.CommandRegistry;
import io.schark.pony.core.PonyManager;
import io.schark.pony.exception.PonyStartException;
import io.schark.pony.utils.PonyUtils;
import net.dv8tion.jda.api.JDA;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Player_Schark
 */
public class Pony {

	private static Properties CONFIG;
	private static PonyBot PONY_BOT;
	private static PonyManager PONY_MANAGER;


	private static void startPony(String[] args) throws PonyStartException {
		System.out.println("Starting Pony");
		try {
			System.out.println("Loading config");
			Pony.loadConfig();
			System.out.println("Starting Pony");
			Pony.PONY_BOT = Pony.newPonyBot();
			Pony.PONY_BOT.init(args);
			System.out.println("Building JDA");
			JDA jda = Pony.PONY_BOT.build();
			System.out.println("Building Pony");
			Pony.buildPony(jda);
			try {
				jda.awaitReady();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new PonyStartException();
		}
		System.out.println("Pony Started");
	}

	private static void buildPony(JDA jda) {
		CommandRegistry registry = new CommandRegistry();
		Pony.PONY_MANAGER = new PonyManager(jda, registry);
	}

	public static void main(String[] args) throws PonyStartException {
		Pony.startPony(args);
		Pony.PONY_BOT.afterStart();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Shutting down Pony");
			Pony.PONY_BOT.beforeShutdown();
			System.out.println("Shutting down JDA");
			Pony.PONY_BOT.getJda().shutdownNow();
			System.out.println("JDA closed");
			Pony.PONY_BOT.afterShutdown();
			System.out.println("Pony closed. Thank you and good bye. :D");
		}));
	}

	private static void injectToken() throws IOException, IllegalAccessException, NoSuchFieldException {
		String path = Pony.CONFIG.getProperty("token");
		InputStream input = Pony.class.getResourceAsStream("/" + path);
		PonyUtils.setValue(Pony.PONY_BOT, "token", PonyUtils.getFileContent(input));
	}


	private static void injectId() throws NoSuchFieldException, IllegalAccessException {
		String rawId = Pony.CONFIG.getProperty("id");
		long id = Long.parseLong(rawId);
		PonyUtils.setValue(Pony.PONY_BOT, "id", id);
	}

	private static void loadConfig() throws IOException {
			InputStream input = Pony.class.getResourceAsStream("/config.properties");
			Pony.CONFIG = new Properties();
			Pony.CONFIG.load(input);
	}

	private static PonyBot newPonyBot() throws Exception {
			String ponyBotMain = Pony.CONFIG.getProperty("main");
			PonyBot ponyBot = (PonyBot) Class.forName(ponyBotMain).getConstructor().newInstance();
			Pony.injectToken();
			Pony.injectId();
			return ponyBot;
	}

	public static PonyBot getPonyBot() {
		return Pony.PONY_BOT;
	}
}
