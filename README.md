# miio-emu

An emulator for Xiaomi Mi IO (miIO) devices. It listens on a UDP port, speaks the encrypted miIO protocol and answers
requests with canned responses, so you can develop and test control software (for example the openHAB miio binding)
without owning the physical device.

Quick start: emulate the Mi Smart Air Fryer from the project directory (the model file `careli.fryer.maf02.json` is
included), then add a Thing for it in openHAB, see [Connect a client](#connect-a-client):

```shell
mvn compile exec:java "-Dexec.args=careli.fryer.maf02"
```

## What it does

- Answers the miIO discovery "hello" packet with a reply that carries the configured device id (`did`) and a timestamp,
  with the token in the checksum field (`MiIoEmulator`, tested in `MiIoEmulatorTest`).
- Decrypts incoming commands with the configured token and encrypts the answers (AES-128-CBC, key and IV derived from
  the token, see `MiIoCrypto`). A message that cannot be decrypted (for example because of a wrong token), is not a JSON
  object, or lacks `id`, `method` or a required `params` is logged and not answered.
- Emulates one device (one model) per process. The answers come from a model file `<model>.json`, see
  [Configure](#configure).
- Supports classic miIO commands (`get_prop` and any other method) and MIoT commands (`get_properties`,
  `set_properties`, `action`).
- Keeps values written by the client in memory while it runs, so a `set_properties` is visible in a later
  `get_properties`.
- Can generate a first model file from the device database of the openHAB miio binding.

### Commands

Every answer has the form `{"id": <request id>, "result": ...}`. The emulator never sends an `error` object.

| Command          | Answer                                                                                                                                                                                                                                                                       |
| ---------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `miIO.info`      | Fixed device information. `token` and `model` are the configured values, the other fields (`mac`, `fw_ver`, `hw_ver`, `ap`, `netif`, ...) are hardcoded in `MiIoEmulator`.                                                                              |
| `get_prop`       | One value per requested property name, taken from `properties` of the model file. An unknown property is added to the in-memory model: it gets the default from `FakeResponses` if its name is listed there (for example `power`, `mode`, `aqi`), otherwise `null` on the first request and `""` afterwards. |
| `get_properties` | MIoT read. One entry per requested item with `did`, `siid` and `piid` echoed, `code` 0 and `value`. The property is found by `siid`/`piid`, else by name (`did`). An unknown property is added and answered with `""` (or the `FakeResponses` default for that name).        |
| `set_properties` | MIoT write. Stores the value (adds the property if it is not in the model) and answers `code` 0 for every item.                                                                                                                                                              |
| `action`         | MIoT action. With `siid` and `aiid` present: `code` 0 and `out` from the model (empty if not defined), after applying the `sets` of that action. Without `siid` or `aiid`: `code` -4000. The `in` parameters are ignored.                                                    |
| any other method | The `fakeresponse` of the entry in `commands` with the same `command` name (parameters are not compared). If there is none, or its `fakeresponse` is `null`, the answer is `["ok"]`. A method not yet in the model is added to it, together with the parameters received.   |

### Limitations

- Responses are static values from the model file. Only the `sets` of an MIoT `action` change other values; there is no
  further device behaviour (no state machine, timers, maps, firmware, cloud).
- Only one emulator can run per UDP port. A second one fails to start (covered by `MiIoEmulatorTest`). The openHAB
  binding always uses port 54321 and the emulator binds all interfaces, so openHAB can talk to only one emulated device
  per machine.
- A packet whose length field is 32 is answered as a "hello", whatever its content. The checksum of incoming command
  packets is not verified.
- mDNS registration (`MDNSServiceRegistration`, jmDNS) exists but is switched off: `EmulatorMain.enableMdns` is a
  hardcoded `false` without a command line option. `MDNSTest`, `ssdpTest` (which uses the `SSDP` class) and `jsonTest`
  are stand-alone experiments with their own `main` methods and are not started by `EmulatorMain`.
- The model list in `MiIoDevices` (377 models plus `UNKNOWN`) is only used for names in the menu and the log. Which
  devices answer sensibly depends on the model files, see [Included model files](#included-model-files).

## Run

Requirements: a JDK 17 or newer (`maven.compiler.release` is 17 in `pom.xml`) and Maven. Run the commands from the
project directory: model files and `miio-default.json` are read from and written to the working directory.

### Interactive menu

Without arguments `EmulatorMain` starts the device from `miio-default.json` and shows a menu on the console:

```shell
mvn compile exec:java
```

In Eclipse run `EmulatorMain` as Java Application (the project has a `.classpath` for Java 17 with `src` and `include`
as source folders). Make sure the working directory is the project directory.

Menu options (type the option and press Enter; input is read as one word, so it cannot contain spaces):

| Input       | Effect                                                                                                                   |
| ----------- | ------------------------------------------------------------------------------------------------------------------------ |
| `?`         | Log the current setting (device, did, token).                                                                            |
| `l`         | List all known devices with their number, model and description.                                                         |
| `<number>`  | Select the device with that number from the `l` list.                                                                    |
| `<model>`   | Select a device by exact model name, for example `zhimi.airpurifier.ma4`. Only models listed in `MiIoDevices` are accepted. |
| `d:<did>`   | Set the device id. Exactly 8 characters are required (hexadecimal expected, 4 bytes).                                    |
| `t:<token>` | Set the token. Exactly 32 characters are required (hexadecimal expected, 16 bytes).                                      |
| `s`         | Save the responses to `<model>.json`.                                                                                    |
| `r`         | Reload the responses from `<model>.json`.                                                                                |
| `q`         | Stop and quit.                                                                                                           |

The option list is printed again after every input. `h` is shown in the list but has no handler. Every input that is
not a valid `d:`, `t:` or model name also logs `Unknown device`, including `?`, `l`, `s`, `r` and numbers; this is
harmless.

What happens after each input (`EmulatorMain.main`):

- Any input other than `q` stops the emulator, writes `miio-default.json` and starts the (possibly new) device again,
  so a new device, did or token is active from then on and the model file is read again.
- Every stop, including `q`, saves the current in-memory responses to `<model>.json`. Edits you make to a model file
  while the emulator runs are therefore overwritten at the next input, unless that input is `r`, which reloads the
  file before the save.
- `q` quits without writing `miio-default.json`. Always quit with `q`: there is no shutdown hook, so Ctrl+C does not
  save the responses.
- The file is saved in Gson's format: 2-space indentation, `null` fields written and missing `actions` or `commands`
  lists added. The first menu input therefore rewrites the model files of this repository. Check `git diff` before
  committing.
- The menu itself is printed through the logger (at `info`), so in interactive mode do not use `mvn -q` or a log level
  above `info`: the menu would not be shown.

### Headless

With arguments the emulator starts directly and runs until you kill it (Ctrl+C). The arguments are the model, then the
token and the did, both optional:

```text
EmulatorMain <model> [token] [did]
```

```shell
mvn compile exec:java "-Dexec.args=careli.fryer.maf02"
mvn compile exec:java "-Dexec.args=careli.fryer.maf02 BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB 11223344"
```

In headless mode:

- `miio-default.json` is neither read nor written. Token and did default to the values in
  [Token and did](#token-and-did).
- The model does not have to be listed in `MiIoDevices` (it is then logged as `unlisted model`).
- Values changed by the client, and properties and commands added while running, are not saved when the process is
  stopped. The only file written is a model file generated from the database, once, when it is created.

### System properties

| Property        | Default                                                                                                  | Meaning                                                                                                             |
| --------------- | -------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| `miio.port`     | `54321`                                                                                                  | UDP port to listen on (`MiIoReceiver`).                                                                             |
| `miio.database` | `../../oh4/openhab-main/git/openhab-addons/bundles/org.openhab.binding.miio/src/main/resources/database` | Folder with the openHAB miio binding device database files (`DatabaseImporter`), relative to the working directory. |

Pass them to Maven in quotes. This is required in Windows PowerShell, which otherwise splits `-Dmiio.port=55322` at the
dot. Do not use port 55321 for this: the tests use it:

```shell
mvn compile exec:java "-Dexec.args=careli.fryer.maf02" "-Dmiio.port=55322" "-Dmiio.database=C:/path/to/org.openhab.binding.miio/src/main/resources/database"
```

The default database folder is a path relative to the author's checkout layout, so on another machine set
`miio.database` explicitly.

### Port issues

- The openHAB binding always connects to UDP port 54321 (`MiIoBindingConstants.PORT` in the binding source). Use
  `miio.port` only for other clients.
- If the port cannot be opened, the emulator logs `Could not open UDP port <port>: ...` and does not start. For the
  default port the message suggests two causes: another process (or emulator) is already using it, or on Windows the
  port is in a reserved range. Check the reserved ranges with:

  ```shell
  netsh int ipv4 show excludedportrange protocol=udp
  ```

- The socket is bound to all interfaces and allows broadcast.
- The tests run on port 55321 (set in the `pom.xml` surefire configuration) so they do not clash with a running
  emulator on the default port.

### Connect a client

- Point the client at the IP address of the machine running the emulator, UDP port 54321 (or the `miio.port` you set).
- Use the emulator token and did, and the emulated model.
- For the openHAB miio binding, configure a Thing with these parameters (`config.xml` of the binding): `host` (the IP
  of the machine running the emulator), `token` (required: enter the emulator's token, by default
  `AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA`), `deviceId` and `model` (optional, the emulated model). The binding always uses
  port 54321.
- `deviceId` is the did as a decimal number. For the default `AABBCCDD` that is `2864434397`; for another did convert
  the 8 hexadecimal characters to decimal. The binding still accepts a hexadecimal value that contains a letter, but
  logs a warning (`MiIoAbstractHandler`). Without a cloud server it then ignores the value and asks the device for its
  id. A did of only digits, such as `11223344`, is read as a decimal number.
- openHAB's miio discovery can also find the emulator on port 54321: it reads the token from the `hello` reply
  (`MiIoDiscovery`) and suggests a Thing with host, deviceId and token filled in. Anyone on the network who sends a
  `hello` receives the token, so never configure the token of a real device in the emulator.

### Logging

Logging uses slf4j-simple, configured in `include/simplelogger.properties` (copied to the classpath by the `resources`
setting in `pom.xml`): default level `debug`, the `DNSStateTask` logger at `info`, timestamps shown, short logger
names, no thread names. Override the level with a system property, for example
`"-Dorg.slf4j.simpleLogger.defaultLogLevel=info"` for less output. At `info` every received command and its response is
logged. `debug` adds details such as defaults used and properties or commands added; `trace` also logs a hex dump of
every received packet. The log goes to stderr.

### Tests

```shell
mvn test
```

The JUnit 5 tests in `test/` cover the database import (`DatabaseImporterTest`), the MIoT property and action handling
(`FakeResponseGeneratorTest`) and the UDP behaviour (`MiIoEmulatorTest`: hello, `miIO.info`, MIoT get/set/action and a
second emulator on the same port). They use the `careli.fryer.maf02.json` model file from the working directory.

## Configure

### Defaults file

`miio-default.json` is used by the interactive menu only. It holds the device that is started:

```json
{
  "model": "roborock.vacuum.a38",
  "token": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
  "did": "AABBCCDD"
}
```

The file is rewritten after every menu input except `q`. The `model` from this file is used as is, so a model that is
not in `MiIoDevices` works too if you edit the file by hand. If the file is missing, or a field is missing, the values
in [Token and did](#token-and-did) apply and the model defaults to `dreame.vacuum.p2009`
(`EmulatorMain.DEFAULT_DEVICE`).

### Token and did

| Setting | Default                            | Format                               | Where it is used                                                                        |
| ------- | ---------------------------------- | ------------------------------------ | --------------------------------------------------------------------------------------- |
| token   | `AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA` | 32 hexadecimal characters (16 bytes) | Encrypts and decrypts all traffic, is returned in the `hello` reply and in `miIO.info`. |
| did     | `AABBCCDD`                         | 8 hexadecimal characters (4 bytes)   | Device id in the header of every reply, including `hello`.                              |

The defaults are in `Defaults.java`. Set them in `miio-default.json`, with the `t:` and `d:` menu options, or as the
second and third argument in headless mode. Use exactly 32 hexadecimal characters for the token and 8 for the did. Only
the `t:` and `d:` menu options check the length (an invalid value is ignored and logged as `Unknown device`). Values
from `miio-default.json` or the command line are not checked: an odd length stops the emulator at start, and other
wrong lengths give an emulator that cannot answer. The client must use the same token.

### Model files

The responses of the emulated device are read from `<model>.json` in the working directory, for example
`careli.fryer.maf02.json`. When the emulator starts, the `model` field of the loaded data is set to the model that was
started.

Format (`ModelLoader`, `Property`, `Command`, `Action`):

| Field          | Meaning                                                                                                                                                                       |
| -------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `model`        | Model name.                                                                                                                                                                   |
| `ismiot`       | `true` or `false`. Informational: it is written to the file (and set by the database import) but is not used to select behaviour.                                             |
| `properties[]` | Values for `get_prop` and MIoT reads: `property` (name), `fakeresponse` (any JSON value), `datatype` (stored only, not used) and, for MIoT, `siid` and `piid`.                 |
| `commands[]`   | Responses for all other methods: `command` (method name), `param` (recorded parameters, not used for matching) and `fakeresponse` (any JSON value; `null` gives `["ok"]`).  |
| `actions[]`    | MIoT actions: `siid`, `aiid`, optional `out` (JSON array returned as `out`) and optional `sets[]` (`siid`, `piid`, `value`: the property is set to that value when the action is called). |

Entries in `commands` for `miIO.info`, `get_prop`, `get_properties`, `set_properties` and `action` are never used,
because the emulator handles those methods itself (`MiIoEmulator.commandReceived`).

If `<model>.json` (or `miio-default.json`) is not valid JSON, or does not match the format, the emulator stops with a
`JsonSyntaxException` at start or on `r`. An empty file is treated like a missing one.

A missing model file is not an error. The emulator then tries to generate it, see [Add a new device](#add-a-new-device).
If nothing can be generated it starts with an empty model: every request is answered with the defaults described in
[Commands](#commands), and in the interactive menu the requests seen are saved to `<model>.json` at the next stop.

#### Included model files

Checked from the `ismiot` field and the contents of the files in this repository:

| Model file                                                                                                                                                           | Kind                                                                                                                                  |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| `careli.fryer.maf02.json`                                                                                                                                            | MIoT (`ismiot` true): properties with `siid`/`piid` and four actions with `sets`. Used by the tests.                                   |
| `dreame.vacuum.mc1808.json`                                                                                                                                          | `ismiot` true, but its properties have no `siid`/`piid`, so they are only reachable by name.                                          |
| `roborock.vacuum.a23.json`, `roborock.vacuum.a38.json`                                                                                                               | Classic (`ismiot` false), `commands` only (vacuum commands such as `get_dnd_timer` and `get_clean_summary`).                          |
| `roborock.vacuum.a24.json`                                                                                                                                           | Classic, `commands` only. `roborock.vacuum.a24` is not in `MiIoDevices`.                                                              |
| `chuangmi.remote.v2.json`, `chuangmi.ir.v2.json`                                                                                                                     | Classic (`ismiot` false). The remote has three `commands`, the `ir.v2` file has no commands or properties.                            |
| `dreame.vacuum.p2009.json`                                                                                                                                           | Classic (`ismiot` false), one command (`get_indicatorLamp`), no properties.                                                           |
| `mijia.vacuum.v2.json`, `mrbond.airer.m1super.json`, `viomi.vacuum.v8.json`, `zhimi.airpurifier.ma4.json`, `zhimi.airpurifier.mb3.json`, `zimi.powerstrip.v2.json` | Classic (`ismiot` false), `properties` by name.                                                                                       |
| `yeelink.light.ceiling7.json`, `yeelink.light.lamp1.json`, `zhimi.airpurifier.sa1.json`                                                                              | No `ismiot` field. `ceiling7` has six properties, `lamp1` has none, `sa1` has one (unused) `get_properties` command and no properties.                          |
| `mijia.vacuum.v2-miotEMU.json`, `philips.light.mceilm-miotEMU.json`                                                                                                  | Only a `properties` array (no `model`, no `siid`/`piid`). The file names do not match a model name in `MiIoDevices`. Purpose not documented. |

The `model` field inside `roborock.vacuum.a24.json` and `roborock.vacuum.a38.json` says `roborock.vacuum.a23`. This is
harmless: the field is replaced by the started model when the file is loaded.

Other devices in `MiIoDevices` have no model file here. They get one when it is generated from the database or created
by hand.

### Add a new device

1. Start the emulator with the new model name (headless, or put it in `miio-default.json`).
1. If `<model>.json` does not exist, `DatabaseImporter` looks in the folder given by `miio.database` for
   `<model>-miot.json` first and then `<model>.json`: the device database files of the openHAB miio binding. If one is
   found, a model file is built from its `deviceMapping.channels` and written to `<model>.json` right away. If none is
   found, this is logged (`No database file for <model> found in ...`) and the model starts empty. Only the file name
   is checked, not the `id` list inside the files, and many models share a file named after another model (for example
   `careli.fryer.maf03` is in `careli.fryer.maf01-miot.json`). For those, copy the file to a separate folder as
   `<model>-miot.json` (or `<model>.json`) and point `miio.database` at that folder.
1. For each channel with a `property`, the import creates:
   - a property entry, with `siid` and `piid` when the channel has them;
   - a default value: the first `stateDescription` option, else the `minimum` for number channels, else `true` for
     `Switch`, `0` for other number and dimmer channels, `""` for `String`, otherwise `null`.

   For each MIoT action (an entry with `siid` and `aiid`) in a channel's `actions` it creates an action entry without
   `sets` or `out`.
   `ismiot` is set to `true` if a property has both `siid` and `piid`, or if a complete MIoT action (`siid` and `aiid`)
   was found. Commands are not imported.
1. Edit the generated `<model>.json` to the values and responses your client should see. In the interactive menu enter
   `r` after editing (see [Interactive menu](#interactive-menu)); in headless mode restart the emulator.
1. Alternatively, capture what a client asks: run the model, connect the client, then use `s` in the interactive menu so
   the properties and commands it requested are saved to `<model>.json`, and adjust their `fakeresponse`.
1. To select the model by name or number in the interactive menu it has to be added to the `MiIoDevices` enum in the
   source. The model name in `miio-default.json` or the headless argument works without that.

### MIoT properties, actions and sets

MIoT properties are matched on `siid` and `piid` first, and by name (the `did` of the request) if no `siid`/`piid`
matches. A write with `set_properties` replaces the `fakeresponse` of the property. Example property from
`careli.fryer.maf02.json`:

```json
{ "property": "target-temperature", "fakeresponse": 180, "datatype": null, "siid": 2, "piid": 4 }
```

An action can change properties and return values. In `careli.fryer.maf02.json`, action 1 of service 2 sets `status`
(2.1) to 4 and `left-time` (2.5) to 15:

```json
{
  "actions": [
    {
      "siid": 2,
      "aiid": 1,
      "sets": [
        { "siid": 2, "piid": 1, "value": 4 },
        { "siid": 2, "piid": 5, "value": 15 }
      ]
    }
  ]
}
```

Add `"out": [ ... ]` to an action to define the returned values. A property in `sets` that is not in the model is
logged and skipped. An action that is not in the model is answered with `code` 0 and an empty `out`, and is added to
the model without effects.

## License

GNU General Public License v3, see `COPYING.txt`.
