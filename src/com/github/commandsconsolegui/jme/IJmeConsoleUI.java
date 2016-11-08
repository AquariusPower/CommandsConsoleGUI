package com.github.commandsconsolegui.jme;

import com.github.commandsconsolegui.cmd.IConsoleUI;
import com.jme3.scene.Spatial;

public interface IJmeConsoleUI extends IConsoleUI{
	public abstract boolean isHintBox(Spatial target);
	
	public abstract boolean isScrollRequestTarget(Spatial target);

	public abstract void setScrollRequestTarget(Spatial target);
}
