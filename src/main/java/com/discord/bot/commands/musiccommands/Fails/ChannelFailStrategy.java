package com.discord.bot.commands.musiccommands.Fails;

public class ChannelFailStrategy implements FailDescriptionStrategy {
    @Override
    public String getFailDescription() {
        return "Please be in a same voice channel as bot.";
    }
}
