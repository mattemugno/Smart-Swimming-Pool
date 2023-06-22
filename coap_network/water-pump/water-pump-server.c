#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"

#include "node-id.h"
#include "net/ipv6/simple-udp.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-ds6.h"
#include "net/ipv6/uip-debug.h"
#include "routing/routing.h"

#define SERVER_EP "coap://[fd00::1]:5683"
#define CONNECTION_TRY_INTERVAL 1
#define REGISTRATION_TRY_INTERVAL 1
#define SIMULATION_INTERVAL 1
#define ACTUATOR_TYPE "water_pump"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "water_pump"
#define LOG_LEVEL LOG_LEVEL_APP

#define INTERVAL_BETWEEN_SIMULATIONS 3
#define INTERVAL_BETWEEN_CONNECTION_TESTS 1

extern coap_resource_t res_water_pump_switch;

char *service_url = "/registration";
static bool registered = false;

static struct etimer connectivity_timer;
static struct etimer wait_registration;

PROCESS(water_pump_server, "Smart swimming pool Water Pump Server");
AUTOSTART_PROCESSES(&water_pump_server);

static bool is_connected()
{
	if(NETSTACK_ROUTING.node_is_reachable())
	{
		LOG_INFO("The Border Router is reachable\n");
		return true;
  	}
	else
	{
		LOG_INFO("Waiting for connection with the Border Router\n");
	}
	return false;
}


void client_chunk_handler(coap_message_t *response) {
	const uint8_t *chunk;
	if(response == NULL) {
		LOG_INFO("Request timed out\n");
		etimer_set(&wait_registration, CLOCK_SECOND* REGISTRATION_TRY_INTERVAL);
		return;
	}

	int len = coap_get_payload(response, &chunk);

	if(strncmp((char*)chunk, "Success", len) == 0){
		registered = true;
	} else
		etimer_set(&wait_registration, CLOCK_SECOND* REGISTRATION_TRY_INTERVAL);
}

PROCESS_THREAD(water_pump_server, ev, data)
{
  PROCESS_BEGIN();

  LOG_INFO("Smart swimming pool Water Pump server COAP Server\n");
  
  static coap_endpoint_t server_ep;
  static coap_message_t request[1]; 

  PROCESS_PAUSE();

  LOG_INFO("Starting Water Pump CoAP-Server\n"); 
  coap_activate_resource(&res_water_pump_switch, "water-pump/switch"); 
 
  // try to connect to the border router
  etimer_set(&connectivity_timer, CLOCK_SECOND * INTERVAL_BETWEEN_CONNECTION_TESTS);
  PROCESS_WAIT_UNTIL(etimer_expired(&connectivity_timer));
  while(!is_connected())
  {
	etimer_reset(&connectivity_timer);
	PROCESS_WAIT_UNTIL(etimer_expired(&connectivity_timer));
  }
  
  while(!registered) {
        LOG_INFO("Sending registration message\n");
        coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);
        // Prepare the message
        coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
        coap_set_header_uri_path(request, service_url);
        coap_set_payload(request, (uint8_t *)ACTUATOR_TYPE, sizeof(ACTUATOR_TYPE) - 1);

        COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);

        PROCESS_WAIT_UNTIL(etimer_expired(&wait_registration));
  }                          
  PROCESS_END();
}
