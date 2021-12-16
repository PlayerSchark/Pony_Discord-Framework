package io.schark.pony;

import io.schark.pony.core.PonyBot;
import io.schark.pony.core.PonyConfig;
import io.schark.pony.core.PonyManager;
import io.schark.pony.core.PonyManagerType;
import io.schark.pony.exception.PonyStartException;
import io.schark.pony.utils.PonyUtils;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Player_Schark
 */
public class Pony {

	private static Pony INSTANCE;
	private PonyConfig config;
	private PonyBot ponyBot;
	private List<PonyManagerType> managers = new ArrayList<>();

	public static Pony getInstance() {
		return Pony.INSTANCE;
	}

	public static void main(String[] args) throws PonyStartException {
		Pony.INSTANCE = new Pony();
		Pony.INSTANCE.start(args);
		Pony.INSTANCE.getPonyBot().afterStart();

		Runtime.getRuntime().addShutdownHook(Pony.INSTANCE.shutdownPony());
	}

	private void start(String[] args) throws PonyStartException {
		System.out.println("Starting Pony");
		try {
			this.preparePony(args);
		} catch (Exception e) {
			e.printStackTrace();
			throw new PonyStartException("An error occoured while starting Pony");
		}

		this.injectJdaToManager();
		System.out.println("Pony Started");
	}

	private void preparePony(String[] args) throws Exception {
		System.out.println("Loading config");
		this.loadConfig();
		System.out.println("Building Pony");
		this.build();
		System.out.println("Initialize Bot");
		this.ponyBot = this.newPonyBot();
		this.ponyBot.init(args);
		System.out.println("Building JDA");
		JDA jda = this.ponyBot.build();
		jda.awaitReady();
	}

	private void injectJdaToManager() {
		System.out.println("Setting JDA");
		for (PonyManager manager : this.getManagers()) {
			manager.setJda(this.ponyBot.getJda());
		}
	}

	private List<PonyManager> getManagers() {
		List<PonyManager> managers = new ArrayList<>();
		for (PonyManagerType manager : this.managers) {
			managers.add(manager.getManager());
		}
		return managers;
	}

	private void build() {
		System.out.println("Building Pony");
		this.initManager();
	}

	private void initManager() {
		System.out.println("Initialize Managers");
		this.initManager(PonyManagerType.LISTENER);
		this.initManager(PonyManagerType.COMMAND);
	}

	private void initManager(PonyManagerType type) {
		PonyManager manager = type.getManager();
		this.managers.add(type);
		manager.init();
		manager.setEnabled(true);
	}

	@NotNull private Thread shutdownPony() {
		return new Thread(() -> {
			System.out.println("Shutting down Pony");
			this.ponyBot.beforeShutdown();
			System.out.println("Shutting down JDA");
			this.ponyBot.getJda().shutdownNow();
			System.out.println("JDA closed");
			this.ponyBot.afterShutdown();
			System.out.println("Pony closed. Thank you and good bye. :D");
		});
	}

	private void injectToken(PonyBot bot) throws IOException, IllegalAccessException, NoSuchFieldException {
		String path = this.config.getTokenPath();
		InputStream input = Pony.class.getResourceAsStream("/" + path);
		PonyUtils.setValue(bot, "token", PonyUtils.getFileContent(input).trim(), PonyBot.class);
	}


	private void injectId(PonyBot ponyBot) throws NoSuchFieldException, IllegalAccessException, PonyStartException {
		long id;
		try {
			id = this.config.getId();
		} catch (NumberFormatException ex) {
			throw new PonyStartException("Bot id could not be read");
		}
		PonyUtils.setValue(ponyBot, "id", id, PonyBot.class);
	}

	private void loadConfig() throws IOException {
		InputStream input = Pony.class.getResourceAsStream("/config.properties");
		this.config = new PonyConfig();
		this.config.load(input);
	}

	private PonyBot newPonyBot() throws Exception {
		String ponyBotMain = this.config.getPonyBotMain();
		PonyBot ponyBot = (PonyBot) Class.forName(ponyBotMain).getConstructor().newInstance();
		this.injectToken(ponyBot);
		this.injectId(ponyBot);
		return ponyBot;
	}

	public PonyBot getPonyBot() {
		return this.ponyBot;
	}

	public PonyConfig getConfig() {
		return this.config;
	}

	public <M extends PonyManager> M getManager(PonyManagerType<M> type) {
		if (this.managers.contains(type)) {
			return type.getManager();
		}
		return null;
	}
}
