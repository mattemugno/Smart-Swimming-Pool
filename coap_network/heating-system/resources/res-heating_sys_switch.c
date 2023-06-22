#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "heating-sys"
#define LOG_LEVEL LOG_LEVEL_APP

static void heating_sys_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_heating_sys_switch,
         "title=\"Heating System Switch\";rt=\"Control\"",
         NULL,
         heating_sys_post_handler,
         NULL,
         NULL);

bool heating_sys_on = false;

static void heating_sys_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
	size_t len = 0;
	const char *text = NULL;
	
	int mode_success = 1;
	
	len = coap_get_query_variable(request, "mode", &text);
	if(len > 0 && len < 4) {
		if(strncmp(text, "INC", len) == 0) {
			heating_sys_on = true;
			LOG_INFO("Heating system INC mode \n");
		} else if(strncmp(text, "DEC", len) == 0) {
			heating_sys_on = true;
			LOG_INFO("Heating Systemos DEC mode \n");
		} else if(strncmp(text, "OFF", len) == 0) {
			heating_sys_on = false;
			LOG_INFO("Heating Systemos OFF\n");
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
