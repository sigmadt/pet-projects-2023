# Torrentino Project
CLI application which supports Torrent Protocol using `TCP` instead of `UDP`. 


Files and active users are stored in `Torrent Tracker`. Users may not have all the parts of the files stored on their local machines.


## Torrent Protocol
- Client connects to `Tracker` and sends list of shared files
- `Tracker` gives clients information about seeds which have parts of desired file
- Peer to peer connection is used to exchange parts of file
- After downloading part of file client becomes a seed

## Tracker Usage
- `list`  — list available files
- `users` — list active users
- `exit`  — close tracker server

## Torrent Client Usage
- `upload <source>` — upload file from local machine
- `list` — list available files
- `download <id> [destination]` — download file using id from torrent
- `exit` — close connection to tracker


## Details
[`Protobuf`](https://developers.google.com/protocol-buffers) is used for de/serialization messages 
for communication between client and server. Server stores all information about users and files locally 
to prevent losing data between launches. Client information is stored the same way.


## Launch Instructions
- Build `jars` for server and client using gradle tasks
- Use `tracker.sh` to launch torrent tracker
- Use `client.sh` to launch torrent client
- Enjoy!