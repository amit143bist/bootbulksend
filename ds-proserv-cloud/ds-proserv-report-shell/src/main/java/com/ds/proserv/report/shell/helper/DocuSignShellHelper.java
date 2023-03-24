package com.ds.proserv.report.shell.helper;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import com.ds.proserv.cache.manager.DSCacheManager;
import com.ds.proserv.common.constant.PromptColor;
import com.ds.proserv.common.constant.PropertyCacheConstants;

public class DocuSignShellHelper {

	private DSCacheManager dsCacheManager;
	private Terminal terminal;

	public DocuSignShellHelper(Terminal terminal, DSCacheManager dsCacheManager) {

		this.terminal = terminal;
		this.dsCacheManager = dsCacheManager;
	}

	public Terminal getTerminal() {
		return terminal;
	}

	public String getColored(String message, PromptColor color) {
		return (new AttributedStringBuilder())
				.append(message, AttributedStyle.DEFAULT.foreground(color.toJlineAttributedStyle())).toAnsi();
	}

	public String getInfoMessage(String message) {
		return getColored(message, PromptColor
				.valueOf(this.dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.SHELL_INFO_COLOR)));
	}

	public String getSuccessMessage(String message) {
		return getColored(message, PromptColor.valueOf(
				this.dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.SHELL_SUCCESS_COLOR)));
	}

	public String getWarningMessage(String message) {
		return getColored(message, PromptColor.valueOf(
				this.dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.SHELL_WARNING_COLOR)));
	}

	public String getErrorMessage(String message) {
		return getColored(message, PromptColor.valueOf(
				this.dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.SHELL_ERROR_COLOR)));
	}

	/**
	 * Print message to the console in the default color.
	 *
	 * @param message message to print
	 */
	public void print(String message) {
		print(message, null);
	}

	/**
	 * Print message to the console in the success color.
	 *
	 * @param message message to print
	 */
	public void printSuccess(String message) {
		print(message, PromptColor.valueOf(
				this.dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.SHELL_SUCCESS_COLOR)));
	}

	/**
	 * Print message to the console in the info color.
	 *
	 * @param message message to print
	 */
	public void printInfo(String message) {
		print(message, PromptColor
				.valueOf(this.dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.SHELL_INFO_COLOR)));
	}

	/**
	 * Print message to the console in the warning color.
	 *
	 * @param message message to print
	 */
	public void printWarning(String message) {
		print(message, PromptColor.valueOf(
				this.dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.SHELL_WARNING_COLOR)));
	}

	/**
	 * Print message to the console in the error color.
	 *
	 * @param message message to print
	 */
	public void printError(String message) {
		print(message, PromptColor.valueOf(
				this.dsCacheManager.prepareAndRequestCacheDataByKey(PropertyCacheConstants.SHELL_ERROR_COLOR)));
	}

	/**
	 * Generic Print to the console method.
	 *
	 * @param message message to print
	 * @param color   (optional) prompt color
	 */
	public void print(String message, PromptColor color) {
		String toPrint = message;
		if (color != null) {
			toPrint = getColored(message, color);
		}
		terminal.writer().println(toPrint);
		terminal.flush();
	}

}