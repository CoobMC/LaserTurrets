package games.coob.laserturrets.util;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.conversation.SimplePrompt;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.model.RangedValue;
import org.mineacademy.fo.model.Replacer;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DecimalPrompt extends SimplePrompt {

	private final String question;
	private final String menuTitle;
	private final Menu menu;
	private final Supplier<Object> getter;
	private final RangedValue minMaxRange;
	private final Consumer<Double> setter;

	public DecimalPrompt(final String question, final Menu menu, final String menuTitle, final RangedValue minMaxRange, final Supplier<Object> getter, final Consumer<Double> setter) {
		this.question = question;
		this.menu = menu;
		this.menuTitle = menuTitle;
		this.getter = getter;
		this.minMaxRange = minMaxRange;
		this.setter = setter;
	}

	@Override
	protected String getPrompt(final ConversationContext ctx) {
		return this.question.replace("{current}", getter != null ? getter.get().toString() : "");
	}

	@Override
	protected boolean isInputValid(final ConversationContext context, final String input) {
		return Valid.isDecimal(input) && Valid.isInRange(Double.parseDouble(input), minMaxRange.getMinDouble(), minMaxRange.getMaxDouble());
	}

	@Override
	protected String getFailedValidationText(final ConversationContext context, final String invalidInput) {
		return "Invalid input '" + invalidInput + "'! Enter a whole number from " + minMaxRange.getMinDouble() + " to " + minMaxRange.getMaxDouble() + ".";
	}

	@Override
	protected String getMenuAnimatedTitle() {
		return menuTitle != null ? "&a" + menuTitle.substring(0, 1).toUpperCase() + menuTitle.substring(1) + " set to " + getter.get() + "!" : null;
	}

	@Override
	protected Prompt acceptValidatedInput(final ConversationContext context, final String input) {
		setter.accept(Double.parseDouble(input));

		return END_OF_CONVERSATION;
	}

	public Menu getMenu() {
		return this.menu.newInstance();
	}

	public static Button makeDecimalPrompt(final ItemCreator item, final String question, final RangedValue minMaxRange, final Consumer<Double> setter) {
		return makeDecimalPrompt(item, question, minMaxRange, null, setter);
	}

	/**
	 * A convenience method for creating decimal prompts
	 *
	 * @param item
	 * @param question
	 * @param minMaxRange
	 * @param getter
	 * @param setter
	 * @return
	 */
	public static Button makeDecimalPrompt(final ItemCreator item, final String question, final RangedValue minMaxRange, final Supplier<Object> getter, final Consumer<Double> setter) {
		return makeDecimalPrompt(item, question, null, minMaxRange, getter, setter);
	}

	public static Button makeDecimalPrompt(final ItemCreator item, final String question, final String menuTitle, final RangedValue minMaxRange, @Nullable final Supplier<Object> getter, final Consumer<Double> setter) {
		return new Button() {

			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				new games.coob.laserturrets.util.SimplePrompt() {

					@Override
					protected String getPrompt(final ConversationContext ctx) {
						return question.replace("{current}", getter != null ? getter.get().toString() : "");
					}

					@Override
					protected boolean isInputValid(final ConversationContext context, final String input) {
						return Valid.isDecimal(input) && Valid.isInRange(Double.parseDouble(input), minMaxRange.getMinDouble(), minMaxRange.getMaxDouble());
					}

					@Override
					protected String getFailedValidationText(final ConversationContext context, final String invalidInput) {
						return "Invalid input '" + invalidInput + "'! Enter a whole number from " + minMaxRange.getMinDouble() + " to " + minMaxRange.getMaxDouble() + ".";
					}

					@Override
					protected String getMenuAnimatedTitle() {
						return menuTitle != null ? "&9" + menuTitle.substring(0, 1).toUpperCase() + menuTitle.substring(1) + " set to " + getter.get() + "!" : null;
					}

					@Override
					protected Prompt acceptValidatedInput(final ConversationContext context, final String input) {
						setter.accept(Double.parseDouble(input));

						return END_OF_CONVERSATION;
					}
				}.show(player);

				menu.restartMenu();
			}

			@Override
			public ItemStack getItem() {
				final ItemStack itemstack = item.make();
				final ItemMeta meta = itemstack.getItemMeta();

				meta.setLore(Replacer.replaceArray(meta.getLore(), "current", getter != null ? getter.get().toString() : ""));
				itemstack.setItemMeta(meta);

				return itemstack;
			}
		};
	}
}
