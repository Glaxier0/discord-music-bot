package com.discord.bot.commands;

import com.discord.bot.commands.admincommands.GuildsCommand;
import com.discord.bot.commands.admincommands.LogsCommand;
import com.discord.bot.commands.musiccommands.*;
import com.discord.bot.service.MusicCommandUtils;
import com.discord.bot.service.ReplyService;
import com.discord.bot.service.RestService;
import com.discord.bot.service.audioplayer.PlayerManagerService;

import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandManager extends ListenerAdapter {
    final RestService restService;
    final PlayerManagerService playerManagerService;
    final MusicCommandUtils musicCommandUtils;
    final ReplyService replyService;
    private final String adminUserId;
    private Map<String, ISlashCommand> commandsMap;

    public CommandManager(RestService restService, PlayerManagerService playerManagerService,
            MusicCommandUtils musicCommandUtils, ReplyService replyService, String adminUserId) {
        this.restService = restService;
        this.playerManagerService = playerManagerService;
        this.musicCommandUtils = musicCommandUtils;
        this.replyService = replyService;
        this.adminUserId = adminUserId;
        commandMapper();
    }

    @Override
    public void onSlashCommandInteraction(@SuppressWarnings("null") @NonNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();

        ISlashCommand command;
        if ((command = commandsMap.get(commandName)) != null) {
            command.execute(event);
        }
    }

    @Override
    public void onButtonInteraction(@SuppressWarnings("null") @NonNull ButtonInteractionEvent event) {
        IButtonInteraction interaction = new QueueButton(playerManagerService, musicCommandUtils);
        interaction.click(event);
    }

    private void commandMapper() {
        commandsMap = new ConcurrentHashMap<>();
        //Admin Commands
        commandsMap.put("guilds", new GuildsCommand(adminUserId));
        commandsMap.put("logs", new LogsCommand(adminUserId));
        //Music Commands
        commandsMap.put("play", new PlayCommand(restService, playerManagerService, musicCommandUtils, replyService));
        commandsMap.put("skip", new SkipCommand(playerManagerService, musicCommandUtils, replyService));
        commandsMap.put("forward", new ForwardCommand(playerManagerService, musicCommandUtils, replyService));
        commandsMap.put("rewind", new RewindCommand(playerManagerService, musicCommandUtils, replyService));
        commandsMap.put("pause", new PauseCommand(playerManagerService, musicCommandUtils, replyService));
        commandsMap.put("resume", new ResumeCommand(playerManagerService, musicCommandUtils, replyService));
        commandsMap.put("leave", new LeaveCommand(playerManagerService, musicCommandUtils, replyService));
        commandsMap.put("queue", new QueueCommand(playerManagerService, musicCommandUtils));
        commandsMap.put("swap", new SwapCommand(playerManagerService, musicCommandUtils, replyService));
        commandsMap.put("shuffle", new ShuffleCommand(playerManagerService, musicCommandUtils, replyService));
        commandsMap.put("loop", new LoopCommand(playerManagerService, musicCommandUtils, replyService));
        commandsMap.put("remove", new RemoveCommand(playerManagerService, musicCommandUtils, replyService));
        commandsMap.put("nowplaying", new NowPlayingCommand(playerManagerService, musicCommandUtils, replyService));
        commandsMap.put("mhelp", new MusicHelpCommand(musicCommandUtils, replyService));
    }
}