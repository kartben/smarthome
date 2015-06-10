/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.iotivity.handler;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.smarthome.binding.iotivity.IoTivityBindingConstants;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link IoTivityHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * 
 * @author Benjamin Cabé - Initial contribution
 */
public class IoTivityHandler extends BaseThingHandler {

	private Logger logger = LoggerFactory.getLogger(IoTivityHandler.class);
	private String uri;

	ScheduledFuture<?> refreshJob;

	public IoTivityHandler(Thing thing) {
		super(thing);
	}

	@Override
	public void initialize() {
		logger.debug("Initializing IoTivity handler.");
		super.initialize();

		Configuration config = getThing().getConfiguration();

		uri = (String) config.get("uri");

		startAutomaticRefresh();

	}

	@Override
	public void dispose() {
		refreshJob.cancel(true);
	}

	private void startAutomaticRefresh() {

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					updateState(new ChannelUID(getThing().getUID(),
							IoTivityBindingConstants.CHANNEL_POWER), getPower());
				} catch (Exception e) {
					logger.debug("Exception occurred during execution: {}",
							e.getMessage(), e);
				}
			}
		};

		refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, 5,
				TimeUnit.SECONDS);
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		if (!(command instanceof RefreshType))
			logger.debug("Command {} is not supported for channel: {}",
					command, channelUID.getId());
		else {
			if (channelUID.getId().equals(
					IoTivityBindingConstants.CHANNEL_POWER)) {
				updateState(channelUID, getPower());
			}
		}
	}

	private State getPower() {
		Request req = Request.newGet().setURI(uri);
		req.getOptions().removeUriPort();
		req.send();		

		Response response;
		try {
			response = req.waitForResponse(1000);
			logger.debug(response.getPayloadString());
			Gson gson = new Gson();
			JsonObject obj = gson.fromJson(response.getPayloadString(),JsonObject.class);
			// {oic=[{href=/a/light, rep={name=John's light, power=0.0, state=false}}]}

			Number power = obj.getAsJsonArray("oic").get(0).getAsJsonObject().get("rep").getAsJsonObject().get("power").getAsNumber();
			
			return new DecimalType(power.floatValue());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return UnDefType.UNDEF;
	}
}
