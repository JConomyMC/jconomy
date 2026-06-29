if spigot docker file does not exist
  if spigot JAR does not exist
    if buildtools JAR does not exist
      download buildtools JAR
    run buildtools JAR in a temporary container to generate spigot JAR
  add any fixtures (e.g. writing the player file)


run the spigot JAR

wait for spigot server to start

execute commands
stop spigot server

query the database and assert

