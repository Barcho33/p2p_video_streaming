
# NucleusStream

**NucleusStream** is a peer-to-peer (P2P) video streaming system developed in Java, using Maven and JavaFX. The system is split into three Java projects: `Peer`, `Common`, and `Tracker`.

## Project Overview

### Peer
The Peer project handles the user interface and all client-side operations. It runs a local HTTP server on `localhost` that enables communication with other peers. Peer also:
- Uses a local SQLite database for storing metadata.
- Runs an Electron-based HLS video player as a separate process.
- Converts videos to `.m3u8` playlists and `.ts` segments using FFmpeg.
- Stores all metadata in the local SQLite database.

### Common
This module acts as an internal API used by both peers and the tracker. It includes:
- Domain model classes: `User`, `Video`, `VideoChunk`, and `Thumbnail`.
- Communication classes for serialized object transfer.
- An enum that describes the type of operation being performed.

### Tracker
The Tracker manages network metadata, such as:
- Which peers are connected.
- What segments they host.
- Video metadata shared by peers (based on their local SQLite database).
It uses:
- MySQL database to store user, video, and segment data.
- A multithreaded `ClientHandler` to handle simultaneous peer connections.
- A `DatabaseBroker` class to read/write data to the database.

By default, Tracker runs on `localhost` and connects to a MySQL database named `peer_to_peer`.

## Requirements

To run the system, make sure the following dependencies are installed:

- Linux OS (currently supported only on Linux)
- JDK 21
- Maven (`mvn`)
- FFmpeg
- JavaScript
- Electron
- Node.js (`npm`)

## Domain Classes (in Common)

- **User**: Stores user credentials and their list of available videos.
- **Video**: Represents video metadata like name, duration, and size.
- **VideoChunk**: Represents segments of a video, associated with a specific video ID.
- **Thumbnail**: Stores preview image data related to videos.

## Typical Workflow

1. The client (Peer) starts and sends a handshake to the Tracker containing video metadata from its local SQLite database.
2. When the client wants to play a video, it first tries to stream available segments from its own local storage.
3. If a needed segment is not available locally, the client contacts the Tracker and requests a list of peer IP addresses that host that segment.
4. The client then picks a random peer from the list (non-optimized) and requests the segment.
5. Once received, the segment is streamed, stored locally, and the client notifies the Tracker that it now also hosts that segment. The local SQLite database is updated accordingly.
6. If a client uploads a new video, it uses FFmpeg to split the video into `.ts` segments and a `.m3u8` playlist.
7. The client updates its local SQLite database with the new video and segment metadata.
8. Finally, it informs the Tracker about the newly available video.

## Database Setup

Before running the application, you need to create the required MySQL database and tables.

Paste the following SQL script in MySQL Workbench (or another MySQL client):

```
CREATE TABLE `user` (
  `uuid` VARCHAR(36) NOT NULL,
  `username` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`uuid`)
);

CREATE TABLE `video` (
  `uuid` VARCHAR(36) NOT NULL,
  `video_title` VARCHAR(20) NOT NULL,
  `upload_date` DATETIME(6) NOT NULL,
  `uploader` VARCHAR(20) NOT NULL,
  `size` INT,
  `segment_num` INT,
  PRIMARY KEY (`uuid`)
);

CREATE TABLE `thumbnail` (
  `videoId` VARCHAR(36) NOT NULL,
  `image` MEDIUMBLOB NOT NULL,
  PRIMARY KEY (`videoId`),
  FOREIGN KEY (`videoId`) REFERENCES `video`(`uuid`)
);

```

## Running the Application

### Tracker

```bash
mvn clean package
cd target
java -jar Tracker-1.0-SNAPSHOT.jar
```

### Common

```bash
mvn clean package
```

### Peer

```bash
mvn clean package
mvn javafx:run
```

## Disclaimer

This is a student project developed as part of a learning process. Some implementation decisions were made for simplicity rather than optimality. For example:

- The UI uses static form dimensions, which may result in layout or font scaling issues on displays with different resolutions or DPI settings.
- Peer selection for video segment downloading is currently random and not optimized for network efficiency.
- Error handling and security mechanisms are minimal or not yet implemented.

The project is under active development and may evolve significantly over time.
