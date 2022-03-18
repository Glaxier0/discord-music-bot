# discord-music-bot

Music bot with slash commands written in java-spring boot

Discord API - [JDA Wrapper](https://github.com/DV8FromTheWorld/JDA)

Discord API BOT TOKEN from [here](https://discord.com/developers/applications)

From [Google Console](https://console.cloud.google.com/apis/dashboard) create a project, enable [Youtube API](https://developers.google.com/youtube/v3) and create api key from [Google Console](https://console.cloud.google.com/apis/dashboard).

Create a [Spotify App](https://developer.spotify.com/dashboard/applications), get client id and client secret from there.

For playing age restricted videos and you have to set [__Secure-3PSID and __Secure-3PAPISID](https://github.com/Walkyst/lavaplayer-fork/issues/18). 

If you don't want your music bot to play age restricted videos just comment out [this](https://github.com/Glaxier0/discord-music-bot/blob/17d7c784195a0da49ef087a54ca724b7a0476a5c/src/main/java/com/discord/bot/GlaxierBot.java#L51).

Lastly install [redis](https://redis.io/) and create a database .

Just edit [application.properties](https://github.com/Glaxier0/discord-music-bot/blob/Main/src/main/resources/application.properties) file and you are ready to go.

For bot usage type /mhelp.
