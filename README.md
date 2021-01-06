# MIO

Lightweight library for easy I/O handling

## Features
* Start the process at any time by calling ```start()```
* Stop the process by calling ```stop()```
* Pause the process by calling ```setPaused(true)```
* Follow the process through progress listeners using ```addProgressListener()```, that can show:
  - Full size of file
  - Current read size
  - Reading speed
  - Percent of completed work
  - Other process specified values via ```getProcess()```
  
## Usage

### Using MIO class
MIO class can provide easy access to all operations, but you can't add any progress listeners or set specific options
```java
  MIO.copy("myFolder", "anotherFolder");
```

### Using Process object
Process object can let you specify any option and ability to call ```stop()```, ```pause()```, ```addProgressListener()``` methods
```java
  CopyingProcess process = new CopyingProcess("myFolder", "anotherFolder");
  process.setCopyOnlyContent(true);
  process.addProgressListener(event -> {
      System.out.println("Current file: " + event.getProcess().getCurrentCopyingFromFile().getName());
      
      if(event.getPercent() >= 50)    // Stop copying at 50%
          event.getProcess().stop();
  });
  process.startSync();
```
