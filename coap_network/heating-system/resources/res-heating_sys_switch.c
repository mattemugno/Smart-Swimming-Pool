#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "heating-sys"
#define LOG_LEVEL LOG_LEVEL_APP

static void heating_sys_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_heating_sys_switch,
         "title=\"Heating System Switch\";rt=\"Control\"",
         NULL,
         NULL,
         heating_sys_put_handler,
         NULL);

bool heating_sys_on = false;

static void heating_sys_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
	size_t len = 0;
	const char *text = NULL;
	char mode[4];
	memset(mode, 0, 3);
	
	int mode_success = 1;
	
	len = coap_get_post_variable(request, "mode", &text);
	memcpy(mode, text, len);

	if(len > 0 && len < 10) {
		if(strncmp(mode, "INC", len) == 0) {
			heating_sys_on = true;
			leds_set(LEDS_GREEN);
			LOG_INFO("Heating system INC mode \n");
		} else if(strncmp(mode, "DEC", len) == 0) {
			heating_sys_on = true;
			leds_set(LEDS_GREEN);
			LOG_INFO("Heating System DEC mode \n");
		} else if(strncmp(mode, "OFF", len) == 0) {
			heating_sys_on = false;
			leds_set(LEDS_RED);
			LOG_INFO("Heating System OFF\n");
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
