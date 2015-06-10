package org.eclipse.smarthome.binding.iotivity.discovery;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.californium.core.coap.MessageObserverAdapter;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.smarthome.binding.iotivity.IoTivityBindingConstants;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class IoTivityDiscoveryService extends AbstractDiscoveryService {

	private final Logger logger = LoggerFactory
			.getLogger(IoTivityDiscoveryService.class);
	private Endpoint client;

	public IoTivityDiscoveryService() {
		super(IoTivityBindingConstants.SUPPORTED_THING_TYPES_UIDS, 10);
		
		NetworkConfig config = NetworkConfig.getStandard();
		config.set(NetworkConfig.Keys.DEDUPLICATOR,
				NetworkConfig.Keys.NO_DEDUPLICATOR);
		client = new CoAPEndpoint(0, config);
		try {
			client.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public Set<ThingTypeUID> getSupportedThingTypes() {
		return IoTivityBindingConstants.SUPPORTED_THING_TYPES_UIDS;
	}

	/**
	 * Submit the discovered location to the Smarthome inbox
	 * 
	 * @param name
	 * @param uri
	 */
	private void submitDiscoveryResults(String name, String uri) {
		ThingUID uid = new ThingUID(
				IoTivityBindingConstants.THING_TYPE_IOTIVITY, name);
		if (uid != null) {
			Map<String, Object> properties = new HashMap<>(1);
			properties.put("uri", uri);
			DiscoveryResult result = DiscoveryResultBuilder.create(uid)
					.withProperties(properties)
					.withRepresentationProperty("uri")
					.withLabel("IoTivity light – " + name).build();
			thingDiscovered(result);
		}
	}

	private void discoverIoTivity() {

		Request req = Request.newGet().setURI(
				"coap://224.0.1.187:5683/oic/res?rt=core.light");
		req.getOptions().removeUriPort();

		client.sendRequest(req);

		req.addMessageObserver(new MessageObserverAdapter() {
			@Override
			public void onResponse(Response response) {
				System.out.println(response.getSource() + ":"
						+ response.getSourcePort());

				// parse response as JSON
				// looks like:
				// {"oic":[{"href":"/a/light","sid":"70eb96a2-2de0-46ec-8a50-83da8ea4ef9d","prop":{"rt":["core.light","core.brightlight"],"if":["oic.if.baseline","oic.if.ll"],"obs":1}}]}

				Gson gson = new Gson();
				JsonObject obj = gson.fromJson(response.getPayloadString(),
						JsonObject.class);

				String uri = obj.getAsJsonArray("oic").get(0)
						.getAsJsonObject().get("href").getAsString();
				
				String sid = obj.getAsJsonArray("oic").get(0)
						.getAsJsonObject().get("sid").getAsString().split("-")[0];

				submitDiscoveryResults(sid, "coap://" + response.getSource().getHostAddress()
						+ ":" + response.getSourcePort() + uri);
				
				// System.out.println(Utils.prettyPrint(response));
			}
		});
	}

	@Override
	protected void startBackgroundDiscovery() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					discoverIoTivity();
					try {
						Thread.sleep(10L * 1000);
					} catch (InterruptedException e) {
					}
				}
			}

		}).start();
	}

	@Override
	protected void startScan() {
		discoverIoTivity();
		removeOlderResults(getTimestampOfLastScan());
	}

}