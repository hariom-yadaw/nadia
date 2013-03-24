package net.mmberg.nadia.ui;

public abstract class UserInterface {

	protected UIConsumer consumer=null; 
	
	public void register(UIConsumer consumer){
		this.consumer=consumer;
	}
	
	public abstract void start();
	public abstract void stop();
	

	
}
