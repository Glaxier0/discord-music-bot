package com.discord.bot.commands;

import com.discord.bot.commands.admincommands.GuildsCommand;
import com.discord.bot.commands.admincommands.LogsCommand;
import com.discord.bot.commands.musiccommands.*;
import com.discord.bot.service.RestService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandManager extends ListenerAdapter {

    RestService restService;
    MusicCommandUtils musicCommandUtils;
    private Map<String, ISlashCommand> commandsMap;

    public CommandManager(RestService restService) {
        this.restService = restService;
        this.musicCommandUtils = new MusicCommandUtils();
        commandMapper();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();

        ISlashCommand command;
        if ((command = commandsMap.get(commandName)) != null) {
            command.execute(event);
        }
    }

    private void commandMapper() {
        commandsMap = new ConcurrentHashMap<>();
        //Admin Commands
        commandsMap.put("guilds", new GuildsCommand());
        commandsMap.put("logs", new LogsCommand());
        //Music Commands
        commandsMap.put("play", new PlayCommand(restService, musicCommandUtils));
        commandsMap.put("skip", new SkipCommand(musicCommandUtils));
        commandsMap.put("pause", new PauseCommand(musicCommandUtils));
        commandsMap.put("resume", new ResumeCommand(musicCommandUtils));
        commandsMap.put("leave", new LeaveCommand(musicCommandUtils));
        commandsMap.put("queue", new QueueCommand(musicCommandUtils));
        commandsMap.put("swap", new SwapCommand(musicCommandUtils));
        commandsMap.put("shuffle", new ShuffleCommand(musicCommandUtils));
        commandsMap.put("mhelp", new MusicHelpCommand(musicCommandUtils));
    }
}
