#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "water-pump"
#define LOG_LEVEL LOG_LEVEL_APP

#include "global-variables.h"

static void water_pump_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_water_pump_switch,
         "title=\"Heating System Switch\";rt=\"Control\"",
         NULL,
         water_pump_post_handler,
         NULL,
         NULL);

bool water_pump_on = false;

static void water_pump_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
	size_t len = 0;
	const char *text = NULL;
	
	int mode_success = 1;
	
	len = coap_get_query_variable(request, "mode", &text);
	if(len > 0 && len < 4) {
		if(strncmp(text, "INC", len) == 0) {
			water_pump_on = true;
			LOG_INFO("Water Pump INC mode\n");
		} else if(strncmp(text, "DEC", len) == 0) {
			water_pump_on = true;
			LOG_INFO("Water Pump DEC mode\n");
		} else if(strncmp(text, "OFF", len) == 0) {
			water_pump_on = false;
			LOG_INFO("Water Pump OFF\n");
		} else {
			mode_success = 0;
		}
	} else {
		mode_success = 0;
	}

	
	if(!mode_success) {
    		coap_set_status_code(response, BAD_REQUEST_4_00);
 	}
}
