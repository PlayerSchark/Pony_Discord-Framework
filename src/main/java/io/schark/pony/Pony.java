package io.schark.pony;

import io.schark.pony.core.*;
import io.schark.pony.core.feat.PonyManagerListener;
import io.schark.pony.exception.PonyStartException;
import io.schark.pony.utils.PonyUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Player_Schark
 */
public final class Pony {

	private final static Pony INSTANCE = new Pony();
	private PonyConfig config;
	private PonyBot ponyBot;
	private List<PonyManagerType<? extends PonyManager>> managers = new ArrayList<>();
	private boolean notReady = true;
	private PonyInjector injector;

	public static Pony getInstance() {
		return Pony.INSTANCE;
	}

	public static void main(String[] args) throws PonyStartException {
		Runtime.getRuntime().addShutdownHook(Pony.INSTANCE.shutdownPony());
		Pony.INSTANCE.start(args);
		Pony.INSTANCE.getPonyBot().afterStart();
	}

	public void awaitReady() throws InterruptedException {
		while (this.notReady) {
			//noinspection BusyWait
			Thread.sleep(10);
		}
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
		System.out.println("Initialize Bot");
		this.ponyBot = this.newPonyBot();
		System.out.println("Building Pony");
		this.build();
		System.out.println("Injecting PonyBot with config values");
		this.injector = new PonyInjector(this.ponyBot, this.config);
		this.injector.injectBot();
		this.ponyBot.init(args);
		System.out.println("Building JDA");
		JDA jda = this.buildJda();
		this.notReady = false;
	}

	private JDA buildJda() throws LoginException, InterruptedException, NoSuchFieldException, IllegalAccessException {
		JDABuilder builder = this.ponyBot.build();
		PonyManagerListener manager = Pony.getInstance().getManager(PonyManagerType.LISTENER);
		if (manager != null) {
			Object[] listeners = manager.getRegisterableListeners();
			builder.addEventListeners(listeners);
		}
		builder.setEnableShutdownHook(false);
		JDA jda = builder.build();
		jda.awaitReady();

		this.injector.injectJda(jda);
		return jda;
	}

	private void injectJdaToManager() {
		System.out.println("Setting JDA");
		for (PonyManager manager : this.getManagers()) {
			manager.setJda(this.ponyBot.getJda());
		}
	}

	private List<PonyManager> getManagers() {
		List<PonyManager> managers = new ArrayList<>();
		for (PonyManagerType<?> manager : this.managers) {
			managers.add(manager.manager());
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
		this.initManager(PonyManagerType.AUDIO);
	}


	private <M extends PonyManager> void initManager(PonyManagerType<M> type) {
		PonyManager manager = type.manager();
		this.managers.add(type);
		manager.init();
		manager.setEnabled(true);
	}

	@NotNull private Thread shutdownPony() {
		return new Thread(() -> {
			this.ponyBot.beforeShutdown();
			System.out.println("Shutting down Pony");
			PonyManagerType.COMMAND.manager().getRegistry().shutdown();
			System.out.println("Shutting down JDA");
			this.ponyBot.getJda().shutdownNow();
			PonyUtils.await(()->this.ponyBot.getJda().getStatus() == JDA.Status.SHUTDOWN);
			System.out.println("JDA closed");
			this.ponyBot.afterShutdown();
			System.out.println("Pony closed. Thank you and good bye. :D");
		}, "Pony Shutdown Hook");
	}

	private void loadConfig() throws IOException {
		InputStream input = Pony.class.getResourceAsStream("/config.properties");
		this.config = new PonyConfig();
		this.config.load(input);
	}

	private PonyBot newPonyBot() throws Exception {
		String ponyBotMain = this.config.getPonyBotMain();
		return (PonyBot) Class.forName(ponyBotMain).getConstructor().newInstance();
	}

	public PonyBot getPonyBot() {
		return this.ponyBot;
	}

	public PonyConfig getConfig() {
		return this.config;
	}

	public <M extends PonyManager> M getManager(PonyManagerType<M> type) {
		if (this.managers.contains(type)) {
			return type.manager();
		}
		return null;
	}
}
