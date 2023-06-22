#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "chlorine-dispenser"
#define LOG_LEVEL LOG_LEVEL_APP

static void chlorine_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_chlorine_switch,
         "title=\"Chlorine dispenser Switch\";rt=\"Control\"",
         NULL,
         chlorine_post_handler,
         NULL,
         NULL);

bool chlorine_on = false;

static void chlorine_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
	size_t len = 0;
	const char *text = NULL;
	
	int mode_success = 1;
	
	len = coap_get_query_variable(request, "mode", &text);
	if(len > 0 && len < 4) {
		if(strncmp(text, "ON", len) == 0) {
			chlorine_on = true;
			LOG_INFO("Chlorine dispenser ON \n");
		} else if(strncmp(text, "OFF", len) == 0) {
			chlorine_on = true;
			LOG_INFO("Chlorine dispenser OFF \n");
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
