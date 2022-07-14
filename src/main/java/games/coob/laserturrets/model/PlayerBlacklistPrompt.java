package games.coob.laserturrets.model;

import games.coob.laserturrets.PlayerCache;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mineacademy.fo.conversation.SimplePrompt;

public class PlayerBlacklistPrompt extends SimplePrompt {

	@Override
	protected String getPrompt(final ConversationContext context) {
		return "&6What player shouldn't be targeted by this turret? You can add more players to the blacklist by using the /turret blacklist add <player> command.";
	}

	@Nullable
	@Override
	protected Prompt acceptValidatedInput(@NotNull final ConversationContext context, @NotNull final String input) {
		final TurretRegistry registry = TurretRegistry.getInstance();
		final PlayerCache cache = PlayerCache.from(this.getPlayer(context));

		registry.addPlayerToBlacklist(cache.getTurretBlock(), input);
		registry.register(cache.getTurretBlock(), cache.getTurretType());
		tell("&6You have added &7" + input + "&6 to the blacklist!");
		return END_OF_CONVERSATION;
	}
}
