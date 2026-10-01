# miio-emu
Xiaomi Mi IO device emulator

This is emulating various Xiaomi devices, to support development of alternative control programs

Device responses are taken from `<model>.json` in the working directory (or hardcoded in FakeResponses.java).
If no model file exists, one is generated from the openHAB miio binding database file `<model>-miot.json`
(or `<model>.json`): point to the database folder with `-Dmiio.database=<dir>`.
Edit the generated file to set the values you want to see.
MIoT devices are supported: `get_properties`/`set_properties` (found by siid/piid, written values are kept
while running) and `action`. An action can change properties, e.g. in the model file:

    "actions": [ { "siid": 2, "aiid": 1, "sets": [ { "siid": 2, "piid": 1, "value": 4 } ] } ]

## Run

Interactive menu (default device in `miio-default.json`; from Eclipse run `EmulatorMain` as Java Application):

    mvn compile exec:java

Directly as a model (stays running until stopped, changed values are not saved): `EmulatorMain <model> [token] [did]`

    mvn compile exec:java -Dexec.args="careli.fryer.maf02" -Dmiio.database=<path to binding>/src/main/resources/database

The openHAB binding always connects to UDP port 54321 (override for other clients with `-Dmiio.port`).
On Windows this port can be in a range reserved by Hyper-V/WinNAT, in which case the emulator reports it can't open the port.
Check with `netsh int ipv4 show excludedportrange protocol=udp`.

Thing configuration for the binding: host = IP of the machine running the emulator, token `AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA`,
deviceId `AABBCCDD` (defaults), model = the emulated model.
