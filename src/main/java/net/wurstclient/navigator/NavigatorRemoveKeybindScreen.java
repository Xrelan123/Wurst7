/*
 * Copyright (C) 2014 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.navigator;

import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.wurstclient.WurstClient;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.util.RenderUtils;

public class NavigatorRemoveKeybindScreen extends NavigatorScreen
{
	private NavigatorFeatureScreen parent;
	private TreeMap<String, PossibleKeybind> existingKeybinds;
	private String hoveredKey = "";
	private String selectedKey = "";
	private String text = "Select the keybind you want to remove.";
	private ButtonWidget removeButton;
	
	public NavigatorRemoveKeybindScreen(
		TreeMap<String, PossibleKeybind> existingKeybinds,
		NavigatorFeatureScreen parent)
	{
		this.existingKeybinds = existingKeybinds;
		this.parent = parent;
	}
	
	@Override
	protected void onResize()
	{
		// OK button
		removeButton = new ButtonWidget(width / 2 - 151, height - 65, 149, 18,
			"Remove", b -> remove());
		removeButton.active = !selectedKey.isEmpty();
		addButton(removeButton);
		
		// cancel button
		addButton(new ButtonWidget(width / 2 + 2, height - 65, 149, 18,
			"Cancel", b -> minecraft.openScreen(parent)));
	}
	
	private void remove()
	{
		String oldCommands =
			WurstClient.INSTANCE.getKeybinds().getCommands(selectedKey);
		if(oldCommands == null)
			return;
		
		ArrayList<String> commandsList = new ArrayList<>(Arrays.asList(
			oldCommands.replace(";", "�").replace("��", ";").split("�")));
		
		String command = existingKeybinds.get(selectedKey).getCommand();
		while(commandsList.contains(command))
			commandsList.remove(command);
		
		if(commandsList.isEmpty())
			WurstClient.INSTANCE.getKeybinds().remove(selectedKey);
		else
		{
			String newCommands = String.join("�", commandsList)
				.replace(";", "��").replace("�", ";");
			WurstClient.INSTANCE.getKeybinds().add(selectedKey, newCommands);
		}
		
		WurstClient.INSTANCE.getNavigator()
			.addPreference(parent.getFeature().getName());
		
		minecraft.openScreen(parent);
	}
	
	@Override
	protected void onKeyPress(int keyCode, int scanCode, int int_3)
	{
		if(keyCode == 1)
			minecraft.openScreen(parent);
	}
	
	@Override
	protected void onMouseClick(double x, double y, int button)
	{
		// commands
		if(!hoveredKey.isEmpty())
		{
			selectedKey = hoveredKey;
			removeButton.active = true;
		}
	}
	
	@Override
	protected void onUpdate()
	{
		// content height
		setContentHeight(existingKeybinds.size() * 24 - 10);
	}
	
	@Override
	protected void onRender(int mouseX, int mouseY, float partialTicks)
	{
		// title bar
		GL11.glEnable(GL_TEXTURE_2D);
		drawCenteredString(minecraft.textRenderer, "Remove Keybind", middleX,
			32, 0xffffff);
		glDisable(GL_TEXTURE_2D);
		
		// background
		int bgx1 = middleX - 154;
		int bgx2 = middleX + 154;
		int bgy1 = 60;
		int bgy2 = height - 43;
		
		// scissor box
		RenderUtils.scissorBox(bgx1, bgy1, bgx2,
			bgy2 - (buttons.isEmpty() ? 0 : 24));
		glEnable(GL_SCISSOR_TEST);
		
		// possible keybinds
		hoveredKey = "";
		int yi = bgy1 - 12 + scroll;
		for(Entry<String, PossibleKeybind> entry : existingKeybinds.entrySet())
		{
			String key = entry.getKey();
			PossibleKeybind keybind = entry.getValue();
			yi += 24;
			
			// positions
			int x1 = bgx1 + 2;
			int x2 = bgx2 - 2;
			int y1 = yi;
			int y2 = y1 + 20;
			
			// color
			if(mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2)
			{
				hoveredKey = key;
				if(key.equals(selectedKey))
					glColor4f(0F, 1F, 0F, 0.375F);
				else
					glColor4f(0.25F, 0.25F, 0.25F, 0.375F);
			}else if(key.equals(selectedKey))
				glColor4f(0F, 1F, 0F, 0.25F);
			else
				glColor4f(0.25F, 0.25F, 0.25F, 0.25F);
			
			// button
			drawBox(x1, y1, x2, y2);
			
			// text
			GL11.glEnable(GL_TEXTURE_2D);
			drawString(minecraft.textRenderer, key.replace("key.keyboard.", "")
				+ ": " + keybind.getDescription(), x1 + 1, y1 + 1, 0xffffff);
			drawString(minecraft.textRenderer, keybind.getCommand(), x1 + 1,
				y1 + 1 + minecraft.textRenderer.fontHeight, 0xffffff);
			glDisable(GL_TEXTURE_2D);
		}
		
		// text
		GL11.glEnable(GL_TEXTURE_2D);
		int textY = bgy1 + scroll + 2;
		for(String line : text.split("\n"))
		{
			drawString(minecraft.textRenderer, line, bgx1 + 2, textY, 0xffffff);
			textY += minecraft.textRenderer.fontHeight;
		}
		
		// scissor box
		glDisable(GL_SCISSOR_TEST);
		
		// buttons below scissor box
		for(int i = 0; i < buttons.size(); i++)
		{
			AbstractButtonWidget button = buttons.get(i);
			
			// positions
			int x1 = button.x;
			int x2 = x1 + button.getWidth();
			int y1 = button.y;
			int y2 = y1 + 18;
			
			// color
			if(!button.active)
				glColor4f(0F, 0F, 0F, 0.25F);
			else if(mouseX >= x1 && mouseX <= x2 && mouseY >= y1
				&& mouseY <= y2)
				glColor4f(0.375F, 0.375F, 0.375F, 0.25F);
			else
				glColor4f(0.25F, 0.25F, 0.25F, 0.25F);
			
			// button
			glDisable(GL_TEXTURE_2D);
			drawBox(x1, y1, x2, y2);
			
			// text
			GL11.glEnable(GL_TEXTURE_2D);
			drawCenteredString(minecraft.textRenderer, button.getMessage(),
				(x1 + x2) / 2, y1 + 4, 0xffffff);
		}
	}
	
	@Override
	protected void onMouseDrag(double mouseX, double mouseY, int button,
		double double_3, double double_4)
	{
		
	}
	
	@Override
	protected void onMouseRelease(double x, double y, int button)
	{
		
	}
}
