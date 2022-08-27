# Music Player
Music Player is an android application that can play song previews from iTunes API according to user search results. The main feature in this application is search song and media player control.
So the user can also play/pause and playing previous or next music.

# About the Android Project
This application is implementing MVVM architecture pattern where the view (activity/fragment) observe live data from the view model. 
While the data that observed in view model is obtained from data source layer (model and repository). 
Then in this project, the data source is the only layer that connecting response API to presentation layer (view and view model) so it can be viewed by the users.

# Tech Stack
- Kotlin Programming Language
- Koin Dependency Injection
- Flow
- Jetpack Library
  - Lifecycle
  - Navigation
  - UI
- Retrofit2
- Glide

# API
- iTunes API

# Project Installation
1. Clone the repository

   ```sh
   git clone https://github.com/iqbalShafiq/MusicPlayer.git
   cd tandur-android
   ```
   
2. Run the app from emulator or physical device 

# Screenshots
![Splash Screen](https://i.ibb.co/nPP0fS2/Splash-Screen.jpg)
![Empty State](https://i.ibb.co/v3gV2qm/Empty-State.jpg)
![Search Result](https://i.ibb.co/6F2vnQL/Search-Result.jpg)
![Playing Music](https://i.ibb.co/pL8K16h/Playing-Music.jpg)
