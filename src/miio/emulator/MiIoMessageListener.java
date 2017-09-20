/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package miio.emulator;

import java.net.SocketAddress;

/**
 * Interface for a listener on the {@link MiIoCommunication}.
 * Informs when a message is received.
 *
 * @author Marcel Verpaalen- Initial contribution
 */
public interface MiIoMessageListener {

    void onMessageReceived(Message message, String string, SocketAddress socketAddress);

    /**
     * Callback method for the {@link MiIoMessageListener}
     *
     * @param status - Status online/offline
     */

}
