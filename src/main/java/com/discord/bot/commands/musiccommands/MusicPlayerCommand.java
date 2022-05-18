package com.discord.bot.commands.musiccommands;

import com.discord.bot.commands.ISlashCommand;
import com.discord.bot.service.audioplayer.PlayerManagerService;

public abstract class MusicPlayerCommand implements ISlashCommand {
    MusicCommandUtils utils;
    PlayerManagerService playerManagerService;
}
