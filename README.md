# Projet4/BeatBot

BeatBot is an app that tries to solve the problem of streaming the music you want when you want it. Through my experience using music applications while running I found limitations trying control the genre and the tempo to fit my needs. There are multiple Genres that I enjoy running to and I didn't have a good way of mixing them. Also, like almost any Genre, there are slower and faster songs and selecting for them isn't always easy. The only way to truly control the tempo and genre together is to make your own playlist of hand picked songs, which for me is more time than I want to spend to have running music. BeatBot allows listeners to choose multiple genres to stream at once and has a new creative UI feature that allows both tempo and tempo range to be set with one button.

Features:
  - Select multiple genres to mix into your queue
  - Filter the genre results using song data (tempo)
  
Known bugs:
  - Using the Spotify SDK allowed for the creation of this app in a very short time frame but also has some limitations; There is no player callback method that indicates the song has ended, There is no error thrown from the player when connection is lost.
  - Application needs to be restarted when connection is lost to the player
  - no network connection on initail login requires the application to be restarted
