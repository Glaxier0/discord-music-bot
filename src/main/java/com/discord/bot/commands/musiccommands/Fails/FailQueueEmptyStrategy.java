package com.discord.bot.commands.musiccommands.Fails;

public class FailQueueEmptyStrategy implements FailDescriptionStrategy {
    public String getFailDescription() {
        return "The queue is currently empty";
    }
}
