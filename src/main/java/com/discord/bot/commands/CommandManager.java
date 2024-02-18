package com.discord.bot.commands;

import com.discord.bot.commands.admincommands.GuildsCommand;
import com.discord.bot.commands.admincommands.LogsCommand;
import com.discord.bot.commands.musiccommands.*;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.RestService;
import com.discord.bot.service.audioplayer.PlayerManagerService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandManager extends ListenerAdapter {
    final RestService restService;
    final PlayerManagerService playerManagerService;
    final MusicCommandUtils musicCommandUtils;
    private final String adminUserId;
    private Map<String, ISlashCommand> commandsMap;

    public CommandManager(RestService restService, PlayerManagerService playerManagerService,
                          MusicCommandUtils musicCommandUtils, String adminUserId) {
        this.restService = restService;
        this.playerManagerService = playerManagerService;
        this.musicCommandUtils = musicCommandUtils;
        this.adminUserId = adminUserId;
        commandMapper();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();

        ISlashCommand command;
        if ((command = commandsMap.get(commandName)) != null) {
            command.execute(event);
        }
    }

    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
        IButtonInteraction interaction = new QueueButton(playerManagerService, musicCommandUtils);
        interaction.click(event);
    }

    private void commandMapper() {
        commandsMap = new ConcurrentHashMap<>();
        //Admin Commands
        commandsMap.put("guilds", new GuildsCommand(adminUserId));
        commandsMap.put("logs", new LogsCommand(adminUserId));
        //Music Commands
        commandsMap.put("play", new PlayCommand(restService, playerManagerService, musicCommandUtils));
        commandsMap.put("skip", new SkipCommand(playerManagerService, musicCommandUtils));
        commandsMap.put("forward", new ForwardCommand(playerManagerService, musicCommandUtils));
        commandsMap.put("rewind", new RewindCommand(playerManagerService, musicCommandUtils));
        commandsMap.put("pause", new PauseCommand(playerManagerService, musicCommandUtils));
        commandsMap.put("resume", new ResumeCommand(playerManagerService, musicCommandUtils));
        commandsMap.put("leave", new LeaveCommand(playerManagerService, musicCommandUtils));
        commandsMap.put("queue", new QueueCommand(playerManagerService, musicCommandUtils));
        commandsMap.put("swap", new SwapCommand(playerManagerService, musicCommandUtils));
        commandsMap.put("shuffle", new ShuffleCommand(playerManagerService, musicCommandUtils));
        commandsMap.put("loop", new LoopCommand(playerManagerService, musicCommandUtils));
        commandsMap.put("remove", new RemoveCommand(playerManagerService, musicCommandUtils));
        commandsMap.put("nowplaying", new NowPlayingCommand(playerManagerService, musicCommandUtils));
        commandsMap.put("mhelp", new MusicHelpCommand());
    }
}