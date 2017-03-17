package com.redhat.iot.demo.simulator;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.redhat.iot.simulator.data.Device;
import com.redhat.iot.simulator.data.Sensor;
import com.redhat.iot.simulator.device.DeviceSim;
import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.cloud.CloudClientListener;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Example of the Kura Camel application.
 */
public class SensorSimulator implements ConfigurableComponent, CloudClientListener {

    private static final Logger logger = LoggerFactory.getLogger(SensorSimulator.class);
    public static final String TOPIC_PREFIX = "topic.prefix";

    private static String KURA = "cloud:";
    private static String TOPIC = "sensorsim/assets";

    private CloudService cloudService;
    private CloudClient cloudClient;

    private String topic;
    private Device device;


    public void setCloudService(CloudService cloudService) {
        logger.info("Setting cloud service");
        this.cloudService = cloudService;
    }

    public void unsetCloudService(CloudService cloudService) {
        this.cloudService = null;
    }

    // Gets called when installed, or Kura is restarted
    public void start(final Map<String, Object> properties) throws Exception {
        logger.info("Start: {}", properties.entrySet());

        if (!(Boolean)properties.get("enabled")) {
            // This component is not enabled (it is paused)
            return;
        }

        // Create classes, threads, etc and do stuff here, but return when finished

    }

    public void updated(final Map<String, Object> properties) throws Exception {
        logger.info("Updating: {}", properties.entrySet());

        topic = (String) properties.get(TOPIC_PREFIX);


        if (!(Boolean)properties.get("enabled")) {
            return;
        }

        logger.info("Retrieving properties", properties.get("sensorconfig"));

        Gson gson = new Gson();
        device = gson.fromJson((String) properties.get("sensorconfig"), Device.class);
        logger.info("Config for {} retrieved", device.getSensors());
        ExecutorService executor = Executors.newFixedThreadPool(device.getDevices());
        long start = new Date().getTime();

        for (int x = 0; x < device.getDevices(); x++) {
            List<Sensor> sensors = Lists.newArrayList();
            for (Sensor s: device.getSensors()) {
                Sensor sensor = new Sensor();
                sensor.setEmittime(s.getEmittime());
                sensor.setId(s.getId());
                sensor.setMean(s.getMean());
                sensor.setVariance(s.getVariance());
                sensors.add(s);
            }
            System.out.println("Creating Device: " + x);
            DeviceSim device = new DeviceSim(x, this.device.getName(), sensors, this.device.isTimeoffset(), this.device.getRuntime());
            System.out.println("Starting Thread: " + x);
            executor.execute(device);
        }

        long dur = new Date().getTime() - start;
        System.out.println(dur);
        while (dur < device.getRuntime()) {
            try {
                System.out.println("Sleeping in mainthread for 10 seconds");
                Thread.sleep(10000);
                dur = new Date().getTime() - start;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.out.println("Shutdown all Threads");
        executor.shutdownNow();

    }

    public void stop() throws Exception {
        logger.info("Stopping: ");
        // Stop everything and clean up

    }

    private void doPublish() {
        KuraPayload payload = new KuraPayload();

        // Timestamp the message
        payload.setTimestamp(new Date());

        // Add the temperature as a metric to the payload
/*
        payload.addMetric("temperature", this.temperature);

        // add all the other metrics
        for (Sensor sensor : device.getSensors()) {
            if ("metric.char".equals(metric)) {
                // publish character as a string as the
                // "char" type is not support in the EDC Payload
                payload.addMetric(metric, String.valueOf(this.properties.get(metric)));
            } else if ("metric.short".equals(metric)) {
                // publish short as an integer as the
                // "short " type is not support in the EDC Payload
                payload.addMetric(metric, ((Short) this.properties.get(metric)).intValue());
            } else if ("metric.byte".equals(metric)) {
                // publish byte as an integer as the
                // "byte" type is not support in the EDC Payload
                payload.addMetric(metric, ((Byte) this.properties.get(metric)).intValue());
            } else {
                payload.addMetric(metric, this.properties.get(metric));
            }
        }

        // Publish the message
        try {
            int messageId = this.cloudClient.publish(topic, payload, qos, retain);
            logger.info("Published to {} message: {} with ID: {}", new Object[] { topic, payload, messageId });
        } catch (Exception e) {
            logger.error("Cannot publish topic: " + topic, e);
        }
*/
        logger.info("Created KuraPayload {}", payload.toString());

    }

    private static String getDeviceAddressFromTopic(String in) {
        return in.substring(in.lastIndexOf("/") + 1);
    }

    @Override
    public void onControlMessageArrived(String deviceId, String appTopic, KuraPayload msg, int qos, boolean retain) {
        logger.info("Sensor Simulator - Received control message");
    }

    @Override
    public void onMessageArrived(String deviceId, String appTopic, KuraPayload msg, int qos, boolean retain) {
        logger.info("Sensor Simulator - Received message");
    }

    @Override
    public void onConnectionLost() {
        logger.info("Sensor Simulator - Connection lost");
    }

    @Override
    public void onConnectionEstablished() {
        logger.info("Sensor Simulator - Connection established");
    }

    @Override
    public void onMessageConfirmed(int messageId, String appTopic) {
        logger.info("Sensor Simulator - Message confirmed");
    }

    @Override
    public void onMessagePublished(int messageId, String appTopic) {
        logger.info("Sensor Simulator - Message published");
    }
}