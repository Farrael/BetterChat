# BetterChat #

Edit chat format and default message for minecraft server.

Compatible Bukkit and Spigot 1.8.1, PermissionEx and GroupManager.

### 1- How the plugin work

* Place the .jar in your plugins folder.
* Start your server.
* The plugin will create a BetterChat folder with configurations in your plugin folder.

### 2 - Commands

* /chat [on/off] - Enable/Disable plugin
* /chat reload - Reload plugin configuration

### 3 - Configuration

* join-display - Display message when player login
* join-message - Player login message (work with [color](#color))
* leave-display - Display message when player quit
* leave-message - Play quit message (work with [color](#color))
* first-message-display - Display message on player first connection
* first-message-broadcast - First connection message (broadcast)
* first-message-player - First connection message (player only)
* chat-format - Chat format (see [format](#format))
* whisp-to-format - Sender wispher format (see [format](#format))
* whisp-from-format - Receiver wispher format (see [format](#format))
* whisp-spy-format - Interception wispher format (see [format](#format))
* player-color - Change player display-name with color
* console-chat - Display message on console
* player-tab - Change player display name on tabulation
* player-click - Enable %player% click event (see [format](#format))
* player-click-message - Message on player click(see [format](#format))
* player-hover - Enable %player% hover event (see [format](#format))
* player-hover-message - Message on player hover (see [format](#format))
* enable - Enable/Disable plugin
* console-Debug - Debug message on console.

### 4 - Format <a id="format"></a>
You can use the following formats to add dynamisme in your messages.

<a id="color"></a>Color need to start with '&', for exemple '&9' is blue.

* %server% - Server name (server.conf)
* %suffix% - Player suffix
* %prefix% - Player prefix
* %color% - Player name color (last color in prefix)
* %playername% - Player name.
* %player% - Player name with hover and click event.
* %message% - Message send (only work on chat-format)
* %time% - Current server time
* %item% - Item player hold (with hover message)
* %position% - Player position

### 5 - Contributors

* Farrael