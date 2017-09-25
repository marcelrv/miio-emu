/**
 * Mi IO device emulator Copyright (C) 2017  M. Verpaalen
 *
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package miio.emulator;

/**
 * The {@link MiIoCommand} contains all known commands for the Xiaomi vacuum & various Mi IO commands
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public enum MiIoCommand {

    MIIO_INFO("miIO.info"),
    MIIO_WIFI("miIO.wifi_assoc_state"),
    MIIO_ROUTERCONFIG("miIO.miIO.config_router"),

    GET_PROPERTY("get_prop"),

    START_VACUUM("app_start"),
    STOP_VACUUM("app_stop"),
    START_SPOT("app_spot"),
    PAUSE("app_pause"),
    CHARGE("app_charge"),
    FIND_ME("find_me"),

    CONSUMABLES_GET("get_consumable"),
    CONSUMABLES_RESET("reset_consumable"),
    CLEAN_SUMMARY_GET("get_clean_summary"),
    CLEAN_RECORD_GET("get_clean_record"),
    CLEAN_RECORD_MAP_GET("get_clean_record_map"),

    GET_MAP("get_map_v1"),
    GET_STATUS("get_status"),
    GET_SERIAL_NUMBER("get_serial_number"),

    DND_GET("get_dnd_timer"),
    DND_SET("set_dnd_timer"),
    DND_CLOSE("close_dnd_timer"),

    TIMER_SET("set_timer"),
    TIMER_UPDATE("upd_timer"),
    TIMER_GET("get_timer"),
    TIMER_DEL("del_timer"),

    SOUND_INSTALL("dnld_install_sound"),
    SOUND_GET_CURRENT("get_current_sound"),
    LOG_UPLOAD_GET("get_log_upload_status"),
    LOG_UPLOAD_ENABLE("enable_log_upload"),

    SET_MODE("set_custom_mode"),
    GET_MODE("get_custom_mode"),

    REMOTE_START("app_rc_start"),
    REMOTE_END("app_rc_end"),
    REMOTE_MOVE("app_rc_move"),

    UNKNOWN("");

    private final String command;

    private MiIoCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static MiIoCommand getCommand(String commandString) {
        for (MiIoCommand mioCmd : MiIoCommand.values()) {
            if (mioCmd.getCommand().equals(commandString)) {
                return mioCmd;
            }
        }
        return UNKNOWN;
    }

}
