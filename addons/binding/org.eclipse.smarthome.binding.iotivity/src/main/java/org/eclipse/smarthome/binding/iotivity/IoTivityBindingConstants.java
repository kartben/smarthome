/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.iotivity;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link IoTivityBinding} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author Benjamin Cabé - Initial contribution
 */
public class IoTivityBindingConstants {

    public static final String BINDING_ID = "iotivity";
    
    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_IOTIVITY = new ThingTypeUID(BINDING_ID, "light");

    // List of all Channel ids
    public final static String CHANNEL_POWER = "power";
    
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_IOTIVITY);


}
