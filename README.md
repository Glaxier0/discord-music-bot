# discord-music-bot

Discord music bot using JDA, Lavaplayer, Spotify API, YoutubeAPI and PostgreSQL database.

Discord API - [JDA Wrapper](https://github.com/DV8FromTheWorld/JDA)

Discord API BOT TOKEN from [here](https://discord.com/developers/applications)

From [Google Console](https://console.cloud.google.com/apis/dashboard) create a project, enable [YouTube API](https://developers.google.com/youtube/v3) and create api key from [Google Console](https://console.cloud.google.com/apis/dashboard).

Create a [Spotify App](https://developer.spotify.com/dashboard/applications), get client id and client secret from there.

Edit [application.yaml](https://github.com/Glaxier0/discord-music-bot/blob/Main/src/main/resources/application.yaml) file.

Use docker command below before running bot, it will create local PostgreSQL database for you.
```
docker compose up
```

For bot usage type /mhelp.
