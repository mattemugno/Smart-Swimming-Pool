#include "contiki.h"
#include "net/routing/routing.h"
#include "mqtt.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-icmp6.h"
#include "net/ipv6/sicslowpan.h"
#include "sys/etimer.h"
#include "os/sys/log.h"

#include <string.h>
#include <sys/node-id.h>

/*---------------------------------------------------------------------------*/
#define LOG_MODULE "mqtt-client-water-level"
#ifdef MQTT_CLIENT_CONF_LOG_LEVEL
#define LOG_LEVEL MQTT_CLIENT_CONF_LOG_LEVEL
#else
#define LOG_LEVEL LOG_LEVEL_DBG
#endif

/*---------------------------------------------------------------------------*/
/* MQTT broker address */
#define MQTT_CLIENT_BROKER_IP_ADDR "fd00::1"

static const char *broker_ip = MQTT_CLIENT_BROKER_IP_ADDR;

/* Default config values */
#define DEFAULT_BROKER_PORT         1883
#define DEFAULT_PUBLISH_INTERVAL    (30 * CLOCK_SECOND)

/*---------------------------------------------------------------------------*/
/* Various states */

static uint8_t state;

#define STATE_INIT          0
#define STATE_NET_OK        1
#define STATE_CONNECTING    2
#define STATE_CONNECTED     3
#define STATE_SUBSCRIBED    4
#define STATE_DISCONNECTED  5

/*---------------------------------------------------------------------------*/
PROCESS_NAME(mqtt_client_water);
AUTOSTART_PROCESSES(&mqtt_client_water);

/*---------------------------------------------------------------------------*/
#define MAX_TCP_SEGMENT_SIZE    32
#define CONFIG_IP_ADDR_STR_LEN   64

/*---------------------------------------------------------------------------*/
#define BUFFER_SIZE 64

static char client_id[BUFFER_SIZE];
static char pub_topic[BUFFER_SIZE];

static struct mqtt_connection conn;

#define STATE_MACHINE_PERIODIC     (CLOCK_SECOND)
static struct etimer periodic_timer;

mqtt_status_t status;
char broker_address[CONFIG_IP_ADDR_STR_LEN];

#define APP_BUFFER_SIZE 512
static char app_buffer[APP_BUFFER_SIZE];

static struct mqtt_message *msg_ptr = 0;

static bool inc_water_level = false;
static bool dec_water_level = false;
static int MIN_WATER_LEV = 50;
static int MAX_WATER_LEV = 100;
static int water_level = 70; 
static int variation = 0;

/*---------------------------------------------------------------------------*/
PROCESS(mqtt_client_water, "MQTT Client - Water Level");


/*---------------------------------------------------------------------------*/

static void pub_handler(const char *topic, uint16_t topic_len, const uint8_t *chunk, uint16_t chunk_len)
{
	LOG_INFO("Message received: topic='%s' (len=%u), chunk_len=%u\n", topic, topic_len, chunk_len);

	if(strcmp(topic, "water-level-command") != 0){
		LOG_ERR("Topic not valid!\n");
		return;
	}
	
	LOG_INFO("Received Actuator command\n");
	if (strcmp((const char*) chunk, "INC") == 0){
		LOG_INFO("Switch ON water pump to increment water level \n");
		inc_water_level = true;
		dec_water_level = false;	
	}else if (strcmp((const char*) chunk, "DEC") == 0){
		LOG_INFO("Switch ON water pump to decrement water level \n");	
		inc_water_level = false;
		dec_water_level = true;
	}else if (strcmp((const char*) chunk, "OFF") == 0){
		LOG_INFO("Turn OFF water pump \n");
		inc_water_level = false;
		dec_water_level = false;
	} else {
		LOG_INFO("Received configuration message\n");

		const char* min_key = "\"min\":";
		const char* max_key = "\"max\":";
		const char* min_pos = strstr((const char*)chunk, min_key);
		const char* max_pos = strstr((const char*)chunk, max_key);

		if (min_pos && max_pos) {
			min_pos += strlen(min_key);
			max_pos += strlen(max_key);

			int min_value = atoi(min_pos);
			int max_value = atoi(max_pos);

			LOG_INFO("Minimum water level: %d, Maximum water level: %d\n", min_value, max_value);

			MIN_WATER_LEV = min_value;
			MAX_WATER_LEV = max_value;
		}
		else {
			LOG_ERR("Invalid configuration message format!\n");
		}
	}
}

static void mqtt_event(struct mqtt_connection *m, mqtt_event_t event, void *data)
{
  switch (event) {
    case MQTT_EVENT_CONNECTED:
      printf("MQTT client connected\n");
      state = STATE_CONNECTED;
      break;

    case MQTT_EVENT_DISCONNECTED:
      printf("MQTT Disconnect. Reason %u\n", *((mqtt_event_t *)data));

      state = STATE_DISCONNECTED;
      /* Process poll to enforce reconnection */ 
      process_poll(&mqtt_client_water);
      break;

    case MQTT_EVENT_PUBLISH: 
      msg_ptr = data;
      pub_handler(msg_ptr->topic, strlen(msg_ptr->topic), msg_ptr->payload_chunk, msg_ptr->payload_length);
      break;

    case MQTT_EVENT_SUBACK: 
      #if MQTT_311
      mqtt_suback_event_t *suback_event = (mqtt_suback_event_t *)data;
      if(suback_event->success) 
      	LOG_INFO("Application has subscribed to the topic\n");
      else 
      	LOG_ERR("Application failed to subscribe to topic (ret code %x)\n", suback_event->return_code);
      #else
      LOG_INFO("Application has subscribed to the topic\n");
      #endif
      break;
    case MQTT_EVENT_UNSUBACK: 
      LOG_INFO("Application is unsubscribed to topic successfully\n");
      break;

    case MQTT_EVENT_PUBACK: 
      printf("Publishing complete.\n");
      break;

    default:
      printf("Application got a unhandled MQTT event: %i\n", event);
      break;
  }
}
/*---------------------------------------------------------------------------*/

void adjustWaterLevel() {
    if (inc_water_level || dec_water_level) {
        variation = (rand() % 5) + 1; // a value in [1,3]
        if (rand() % 10 < 8) {
            if (inc_water_level)
                water_level += variation;
            else
                water_level -= variation;
        }
    } else { // water level regulator OFF
        if (rand() % 10 < 3) { 
            variation = (rand() % 7) - 4; // a value in [-2, 2]
            water_level += variation;
        }
    }

    /*if (water_level > MAX_WATER_LEV)
        water_level = MAX_WATER_LEV;
    else if (water_level < MIN_WATER_LEV)
        water_level = MIN_WATER_LEV;*/
}


/*---------------------------------------------------------------------------*/

PROCESS_THREAD(mqtt_client_water, ev, data)
{
  PROCESS_BEGIN();

  printf("MQTT Client Process - Water Level\n");

  snprintf(client_id, BUFFER_SIZE, "%02x%02x%02x%02x%02x%02x",
                     linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
                     linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
                     linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);

  /* Broker registration */ 
  mqtt_register(&conn, &mqtt_client_water, client_id, mqtt_event,
                  MAX_TCP_SEGMENT_SIZE);

  /* Initialize timer */ 
  etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);

  state = STATE_INIT;

  while (1) {
    PROCESS_YIELD();

    if ((ev == PROCESS_EVENT_TIMER && data == &periodic_timer) || 
	      ev == PROCESS_EVENT_POLL){

      if (state == STATE_INIT && uip_ds6_get_global(ADDR_PREFERRED) != NULL) {
        state = STATE_NET_OK;
      }

      if (state == STATE_NET_OK) {
        printf("Connecting to MQTT broker...\n");

        /* Copy MQTT broker address */
        memcpy(broker_address, broker_ip, strlen(broker_ip));
	
	/* Connect to MQTT broker */
        mqtt_connect(&conn, broker_address, DEFAULT_BROKER_PORT,
                     (DEFAULT_PUBLISH_INTERVAL * 3) / CLOCK_SECOND,
                     MQTT_CLEAN_SESSION_ON);
        state = STATE_CONNECTING;
      }

      if (state == STATE_CONNECTED){
	status = mqtt_subscribe(&conn, NULL, "water-level-command", MQTT_QOS_LEVEL_0);
	if(status == MQTT_STATUS_OUT_QUEUE_FULL) {
		LOG_ERR("Tried to subscribe but command queue was full!\n");
		PROCESS_EXIT();
	}
	state = STATE_SUBSCRIBED;
      }

      if (state == STATE_SUBSCRIBED) {
        /* Publish a message on topic "water-level*/
        sprintf(pub_topic, "%s", "water-level");
	adjustWaterLevel();
        sprintf(app_buffer, "{\"nodeId\": %d, \"height\": %d}", node_id, water_level);

        mqtt_publish(&conn, NULL, pub_topic, (uint8_t *)app_buffer,
               strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);
      }

     
      if (state == STATE_DISCONNECTED){
	LOG_ERR("Disconnected from MQTT broker\n");	
	state = STATE_INIT;
      }

      etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);
    }
    else if (ev == PROCESS_EVENT_EXIT) {
      mqtt_disconnect(&conn);
    }
    else if (ev == PROCESS_EVENT_CONTINUE) {
      printf("MQTT client connection failed\n");
    }
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
