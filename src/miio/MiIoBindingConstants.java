/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package miio;

/**
 * The {@link MiIoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marcel Verpaalen - Initial contribution
 */

public final class MiIoBindingConstants {

    public static final String BINDING_ID = "miio";

    // List of all Thing Type UIDs
    public static final String THING_TYPE_MIIO = "generic";
    public static final String THING_TYPE_BASIC = "basic";
    public static final String THING_TYPE_VACUUM = "vacuum";
    public static final String THING_TYPE_UNSUPPORTED = "unsupported";
}
