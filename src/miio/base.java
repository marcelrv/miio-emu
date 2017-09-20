package miio;

import org.slf4j.LoggerFactory;

import miio.emulator.MiIoEmulator;

public class base {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(base.class);

    public static void main(String[] args) {
        logger.info("Emulator Started");
        MiIoEmulator emu = new MiIoEmulator();
        emu.start();
        try {
            Thread.sleep(100000000L);
        } catch (InterruptedException e) {
            logger.info("Emulator Ended");
        }
    }

}
